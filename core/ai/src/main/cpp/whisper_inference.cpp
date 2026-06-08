#include <jni.h>
#include <android/log.h>
#include <vector>
#include <string>
#include <fstream>
#include <sstream>
#include <cmath>
#include <complex>
#include <algorithm>
#include <numeric>

#define TAG "WhisperInference"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

#if USE_ONNX_RUNTIME

#include "onnxruntime_helper.h"

// ── Whisper token constants ───────────────────────────────────────────────────
static constexpr int64_t TOKEN_EOS        = 50257;
static constexpr int64_t TOKEN_SOT        = 50258;  // start of transcript
static constexpr int64_t TOKEN_EN         = 50259;  // English
static constexpr int64_t TOKEN_TRANSCRIBE = 50359;
static constexpr int64_t TOKEN_NOTIMESTAMPS = 50363;
static constexpr int    MAX_DECODER_STEPS = 448;

// ── Mel spectrogram parameters (Whisper base) ────────────────────────────────
static constexpr int MEL_SAMPLE_RATE  = 16000;
static constexpr int MEL_N_FFT        = 400;
static constexpr int MEL_HOP_LENGTH   = 160;
static constexpr int MEL_N_MELS       = 80;
static constexpr int MEL_MAX_AUDIO_S  = 30;
static constexpr int MEL_MAX_SAMPLES  = MEL_MAX_AUDIO_S * MEL_SAMPLE_RATE;  // 480000
static constexpr int MEL_N_FRAMES     = MEL_MAX_AUDIO_S * MEL_SAMPLE_RATE / MEL_HOP_LENGTH;  // 3000

// ── Mel spectrogram helpers ───────────────────────────────────────────────────
namespace {

float hzToMel(float hz) {
    return 2595.0f * log10f(1.0f + hz / 700.0f);
}

float melToHz(float mel) {
    return 700.0f * (powf(10.0f, mel / 2595.0f) - 1.0f);
}

// Create 80-band mel filterbank (N_MELS x n_fft_bins)
std::vector<std::vector<float>> createMelFilterbank() {
    const float f_max = 8000.0f;
    const float mel_min = hzToMel(0.0f);
    const float mel_max = hzToMel(f_max);
    const int n_fft_bins = MEL_N_FFT / 2 + 1;  // 201

    // Mel-spaced center frequencies
    std::vector<float> mel_points(MEL_N_MELS + 2);
    for (int i = 0; i < MEL_N_MELS + 2; i++) {
        mel_points[i] = melToHz(mel_min + (mel_max - mel_min) * i / (MEL_N_MELS + 1));
    }

    // FFT bin frequencies
    std::vector<float> fft_freqs(n_fft_bins);
    for (int i = 0; i < n_fft_bins; i++) {
        fft_freqs[i] = static_cast<float>(MEL_SAMPLE_RATE) * i / MEL_N_FFT;
    }

    // Triangular filterbank
    std::vector<std::vector<float>> filterbank(MEL_N_MELS, std::vector<float>(n_fft_bins, 0.0f));
    for (int m = 0; m < MEL_N_MELS; m++) {
        float f_left   = mel_points[m];
        float f_center = mel_points[m + 1];
        float f_right  = mel_points[m + 2];
        for (int k = 0; k < n_fft_bins; k++) {
            if (fft_freqs[k] >= f_left && fft_freqs[k] <= f_center && f_center > f_left) {
                filterbank[m][k] = (fft_freqs[k] - f_left) / (f_center - f_left);
            } else if (fft_freqs[k] > f_center && fft_freqs[k] <= f_right && f_right > f_center) {
                filterbank[m][k] = (f_right - fft_freqs[k]) / (f_right - f_center);
            }
        }
    }
    return filterbank;
}

// Radix-2 Cooley-Tukey FFT (in-place)
void fft(std::vector<std::complex<float>>& x) {
    int N = static_cast<int>(x.size());
    if (N <= 1) return;

    for (int i = 1, j = 0; i < N; i++) {
        int bit = N >> 1;
        for (; j & bit; bit >>= 1) j ^= bit;
        j ^= bit;
        if (i < j) std::swap(x[i], x[j]);
    }

    for (int len = 2; len <= N; len <<= 1) {
        float ang = -2.0f * static_cast<float>(M_PI) / len;
        std::complex<float> wlen(cosf(ang), sinf(ang));
        for (int i = 0; i < N; i += len) {
            std::complex<float> w(1.0f, 0.0f);
            for (int j = 0; j < len / 2; j++) {
                auto u = x[i + j];
                auto v = x[i + j + len / 2] * w;
                x[i + j] = u + v;
                x[i + j + len / 2] = u - v;
                w *= wlen;
            }
        }
    }
}

// Compute log-mel spectrogram from raw audio (16 kHz mono float).
// Returns vector of size MEL_N_MELS * MEL_N_FRAMES in row-major [n_mels, n_frames].
std::vector<float> computeMelSpectrogram(const float* audio, int audioLength) {
    // Pad / truncate to 30 s
    std::vector<float> padded(MEL_MAX_SAMPLES + MEL_N_FFT, 0.0f);
    int copyLen = std::min(audioLength, MEL_MAX_SAMPLES);
    std::copy(audio, audio + copyLen, padded.begin());

    auto filterbank = createMelFilterbank();
    const int n_fft_bins = MEL_N_FFT / 2 + 1;

    // Next power of 2 for FFT
    int fft_size = 1;
    while (fft_size < MEL_N_FFT) fft_size <<= 1;

    // Hann window
    std::vector<float> hann(MEL_N_FFT);
    for (int i = 0; i < MEL_N_FFT; i++) {
        hann[i] = 0.5f * (1.0f - cosf(2.0f * static_cast<float>(M_PI) * i / (MEL_N_FFT - 1)));
    }

    std::vector<float> mel_spec(MEL_N_MELS * MEL_N_FRAMES, 0.0f);

    for (int frame = 0; frame < MEL_N_FRAMES; frame++) {
        int start = frame * MEL_HOP_LENGTH;

        std::vector<std::complex<float>> fft_buf(fft_size);
        for (int i = 0; i < MEL_N_FFT; i++) {
            fft_buf[i] = padded[start + i] * hann[i];
        }
        fft(fft_buf);

        // Power spectrum
        std::vector<float> power(n_fft_bins);
        for (int k = 0; k < n_fft_bins; k++) {
            float re = fft_buf[k].real();
            float im = fft_buf[k].imag();
            power[k] = re * re + im * im;
        }

        // Apply mel filterbank
        for (int m = 0; m < MEL_N_MELS; m++) {
            float val = 0.0f;
            for (int k = 0; k < n_fft_bins; k++) {
                val += power[k] * filterbank[m][k];
            }
            mel_spec[m * MEL_N_FRAMES + frame] = logf(std::max(val, 1e-10f));
        }
    }

    return mel_spec;
}

}  // anonymous namespace

// ── Whisper context ───────────────────────────────────────────────────────────
struct WhisperContext {
    std::unique_ptr<ORTSession> encoder;
    std::unique_ptr<ORTSession> decoder;
    std::vector<std::string> tokens;  // token_id -> text
};

// Load token vocabulary (one token per line)
static bool loadTokens(const std::string& path, std::vector<std::string>& tokens) {
    std::ifstream ifs(path);
    if (!ifs.is_open()) {
        LOGE("Cannot open tokens file: %s", path.c_str());
        return false;
    }
    std::string line;
    while (std::getline(ifs, line)) {
        tokens.push_back(line);
    }
    LOGD("Loaded %zu tokens from %s", tokens.size(), path.c_str());
    return !tokens.empty();
}

// Decode token IDs to text, handling basic BPE byte encoding
static std::string decodeTokens(const WhisperContext* ctx, const std::vector<int64_t>& ids) {
    std::string result;
    for (auto id : ids) {
        if (id < 0 || id >= static_cast<int64_t>(ctx->tokens.size())) continue;
        if (id == TOKEN_EOS || id == TOKEN_SOT) continue;
        const std::string& tok = ctx->tokens[id];
        // Whisper BPE: Ġ = space, Û = special prefix
        for (char c : tok) {
            if (c == '\xc4' || c == '\xc3') continue;  // skip UTF-8 lead bytes for Ġ/Û
            // Simple: just append printable chars
            if (c >= 0x20 && c < 0x7f) {
                result += c;
            } else if (c == (char)0xc4 || c == (char)0xa0) {
                // Ġ (U+0120) encodes as space in Whisper BPE
                result += ' ';
            }
        }
    }
    // Trim leading/trailing whitespace
    size_t start = result.find_first_not_of(" \t\n\r");
    if (start == std::string::npos) return "";
    size_t end = result.find_last_not_of(" \t\n\r");
    return result.substr(start, end - start + 1);
}

// ── JNI ───────────────────────────────────────────────────────────────────────

extern "C" JNIEXPORT jlong JNICALL
Java_com_myvideo_editor_core_ai_speech_WhisperWrapper_nativeInit(
        JNIEnv *env, jobject /*thiz*/, jstring modelPath) {
    if (!modelPath) {
        LOGE("modelPath is null");
        return 0;
    }

    const char* path = env->GetStringUTFChars(modelPath, nullptr);
    std::string basePath(path);
    env->ReleaseStringUTFChars(modelPath, path);

    // Derive paths: encoder, decoder, and tokens are in the same directory
    std::string dir = basePath.substr(0, basePath.find_last_of('/'));
    std::string encoderPath = dir + "/whisper_encoder.onnx";
    std::string decoderPath = dir + "/whisper_decoder.onnx";
    std::string tokensPath  = dir + "/whisper_tokens.txt";

    auto* ctx = new WhisperContext();

    ctx->encoder.reset(ORTSession::create(encoderPath));
    if (!ctx->encoder) {
        LOGE("Failed to create Whisper encoder session");
        delete ctx;
        return 0;
    }

    ctx->decoder.reset(ORTSession::create(decoderPath));
    if (!ctx->decoder) {
        LOGE("Failed to create Whisper decoder session");
        delete ctx;
        return 0;
    }

    if (!loadTokens(tokensPath, ctx->tokens)) {
        LOGE("Failed to load Whisper tokens");
        delete ctx;
        return 0;
    }

    LOGD("Whisper initialized (encoder + decoder + %zu tokens)", ctx->tokens.size());
    return reinterpret_cast<jlong>(ctx);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_myvideo_editor_core_ai_speech_WhisperWrapper_nativeTranscribe(
        JNIEnv *env, jobject /*thiz*/,
        jlong handle, jfloatArray audio, jint sampleRate) {
    auto* ctx = reinterpret_cast<WhisperContext*>(handle);
    if (!ctx || !ctx->encoder || !ctx->decoder) {
        LOGE("Invalid Whisper handle");
        return env->NewStringUTF("");
    }

    jfloat* audioData = env->GetFloatArrayElements(audio, nullptr);
    if (!audioData) {
        LOGE("Failed to get audio data");
        return env->NewStringUTF("");
    }
    int audioLen = env->GetArrayLength(audio);

    try {
        auto memory_info = ORTEngine::getMemoryInfo();

        // ── 1. Compute mel spectrogram ────────────────────────────────────
        // Resample to 16 kHz if needed (simple linear interpolation)
        std::vector<float> audio16k;
        if (sampleRate != MEL_SAMPLE_RATE) {
            float ratio = static_cast<float>(MEL_SAMPLE_RATE) / sampleRate;
            int newLen = static_cast<int>(audioLen * ratio);
            audio16k.resize(newLen);
            for (int i = 0; i < newLen; i++) {
                float srcIdx = i / ratio;
                int idx0 = static_cast<int>(srcIdx);
                int idx1 = std::min(idx0 + 1, audioLen - 1);
                float frac = srcIdx - idx0;
                audio16k[i] = audioData[idx0] * (1.0f - frac) + audioData[idx1] * frac;
            }
        } else {
            audio16k.assign(audioData, audioData + audioLen);
        }
        env->ReleaseFloatArrayElements(audio, audioData, JNI_ABORT);

        auto mel = computeMelSpectrogram(audio16k.data(), static_cast<int>(audio16k.size()));

        // ── 2. Run encoder ────────────────────────────────────────────────
        std::vector<int64_t> mel_shape = {1, MEL_N_MELS, MEL_N_FRAMES};
        std::vector<Ort::Value> encoder_inputs;
        encoder_inputs.push_back(Ort::Value::CreateTensor<float>(
                memory_info, mel.data(), mel.size(),
                mel_shape.data(), mel_shape.size()));

        auto encoder_outputs = ctx->encoder->run(encoder_inputs);
        if (encoder_outputs.empty()) {
            LOGE("Whisper encoder returned no outputs");
            return env->NewStringUTF("");
        }

        // Clone encoder output so it stays valid across decoder calls
        auto& enc_out = encoder_outputs[0];
        auto enc_shape = enc_out.GetTensorTypeAndShapeInfo().GetShape();
        size_t enc_size = 1;
        for (auto d : enc_shape) enc_size *= d;
        const float* enc_data = enc_out.GetTensorData<float>();

        std::vector<float> encoder_hidden(enc_data, enc_data + enc_size);

        // ── 3. Decoder loop (greedy) ──────────────────────────────────────
        std::vector<int64_t> tokens = {TOKEN_SOT, TOKEN_EN, TOKEN_TRANSCRIBE, TOKEN_NOTIMESTAMPS};
        std::vector<int64_t> dec_enc_shape = enc_shape;  // typically [1, 1500, dim]

        for (int step = 0; step < MAX_DECODER_STEPS; step++) {
            std::vector<int64_t> ids_shape = {1, static_cast<int64_t>(tokens.size())};

            std::vector<Ort::Value> dec_inputs;
            dec_inputs.reserve(2);

            // Decoder input_ids
            dec_inputs.push_back(Ort::Value::CreateTensor<int64_t>(
                    memory_info, tokens.data(), tokens.size(),
                    ids_shape.data(), ids_shape.size()));

            // Encoder hidden states
            dec_inputs.push_back(Ort::Value::CreateTensor<float>(
                    memory_info, encoder_hidden.data(), encoder_hidden.size(),
                    dec_enc_shape.data(), dec_enc_shape.size()));

            auto dec_outputs = ctx->decoder->run(dec_inputs);
            if (dec_outputs.empty()) {
                LOGE("Whisper decoder returned no outputs at step %d", step);
                break;
            }

            // Get logits: shape [1, seq_len, vocab_size]
            auto& logits_tensor = dec_outputs[0];
            auto logits_shape = logits_tensor.GetTensorTypeAndShapeInfo().GetShape();
            const float* logits = logits_tensor.GetTensorData<float>();

            int64_t vocab_size = logits_shape.back();
            int64_t seq_len = logits_shape[1];

            // Find argmax for the last token position
            const float* last_logits = logits + (seq_len - 1) * vocab_size;
            int64_t next_token = 0;
            float max_logit = last_logits[0];
            for (int64_t v = 1; v < vocab_size; v++) {
                if (last_logits[v] > max_logit) {
                    max_logit = last_logits[v];
                    next_token = v;
                }
            }

            if (next_token == TOKEN_EOS) break;

            tokens.push_back(next_token);
        }

        // ── 4. Decode tokens to text ──────────────────────────────────────
        std::string text = decodeTokens(ctx, tokens);
        return env->NewStringUTF(text.c_str());

    } catch (const Ort::Exception& e) {
        LOGE("Whisper inference error: %s", e.what());
        return env->NewStringUTF("");
    } catch (const std::exception& e) {
        LOGE("Whisper error: %s", e.what());
        return env->NewStringUTF("");
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_myvideo_editor_core_ai_speech_WhisperWrapper_nativeRelease(
        JNIEnv *env, jobject /*thiz*/, jlong handle) {
    auto* ctx = reinterpret_cast<WhisperContext*>(handle);
    if (ctx) {
        ctx->encoder.reset();
        ctx->decoder.reset();
        ctx->tokens.clear();
        delete ctx;
        LOGD("Whisper released");
    }
}

#else // !USE_ONNX_RUNTIME

extern "C" JNIEXPORT jlong JNICALL
Java_com_myvideo_editor_core_ai_speech_WhisperWrapper_nativeInit(
        JNIEnv *env, jobject /*thiz*/, jstring modelPath) {
    LOGE("ONNX Runtime not available - Whisper nativeInit stub");
    return 0;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_myvideo_editor_core_ai_speech_WhisperWrapper_nativeTranscribe(
        JNIEnv *env, jobject /*thiz*/,
        jlong handle, jfloatArray audio, jint sampleRate) {
    LOGE("ONNX Runtime not available - Whisper nativeTranscribe stub");
    return env->NewStringUTF("");
}

extern "C" JNIEXPORT void JNICALL
Java_com_myvideo_editor_core_ai_speech_WhisperWrapper_nativeRelease(
        JNIEnv *env, jobject /*thiz*/, jlong handle) {
    LOGE("ONNX Runtime not available - Whisper nativeRelease stub");
}

#endif // USE_ONNX_RUNTIME
