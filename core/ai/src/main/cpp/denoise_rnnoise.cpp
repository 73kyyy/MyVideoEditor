#include <jni.h>
#include <android/log.h>
#include <vector>
#include <string>
#include <cmath>
#include <complex>
#include <algorithm>

#include "onnxruntime_helper.h"

#define TAG "RNNoise"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

// RNNoise frame parameters
static constexpr int FRAME_SIZE = 480;      // 10 ms at 48 kHz
static constexpr int FEATURE_SIZE = 22;     // features per frame
static constexpr int SAMPLE_RATE = 48000;
static constexpr int BANDS = 6;             // number of frequency bands
static constexpr int FFT_SIZE = 512;        // next power of 2 >= FRAME_SIZE

struct RNNoiseContext {
    std::unique_ptr<ORTSession> session;
};

// ── Audio feature extraction helpers ──────────────────────────────────────────
namespace {

// Simple radix-2 FFT
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

// Compute 22-element feature vector for one frame
std::vector<float> computeFrameFeatures(const float* frame, int frameLen) {
    std::vector<float> features(FEATURE_SIZE, 0.0f);

    // Apply Hann window and compute FFT
    std::vector<std::complex<float>> fft_buf(FFT_SIZE);
    for (int i = 0; i < std::min(frameLen, FRAME_SIZE); i++) {
        float w = 0.5f * (1.0f - cosf(2.0f * static_cast<float>(M_PI) * i / (FRAME_SIZE - 1)));
        fft_buf[i] = frame[i] * w;
    }
    fft(fft_buf);

    // Compute power spectrum
    int n_bins = FFT_SIZE / 2 + 1;
    std::vector<float> power(n_bins);
    for (int k = 0; k < n_bins; k++) {
        float re = fft_buf[k].real();
        float im = fft_buf[k].imag();
        power[k] = re * re + im * im;
    }

    // Band energy features (6 bands)
    int band_bins = n_bins / BANDS;
    for (int b = 0; b < BANDS; b++) {
        float energy = 0.0f;
        int start = b * band_bins;
        int end = std::min(start + band_bins, n_bins);
        for (int k = start; k < end; k++) {
            energy += power[k];
        }
        features[b] = logf(std::max(energy / (end - start), 1e-10f));
    }

    // Band energy deltas (6 features)
    for (int b = 0; b < BANDS; b++) {
        features[BANDS + b] = 0.0f;  // Will be computed across frames
    }

    // Pitch-related features (6 features)
    // Simplified: use autocorrelation-based pitch estimation
    float max_corr = 0.0f;
    int pitch_period = 0;
    for (int lag = 40; lag < FRAME_SIZE && lag < frameLen; lag++) {
        float corr = 0.0f;
        for (int i = 0; i < frameLen - lag; i++) {
            corr += frame[i] * frame[i + lag];
        }
        if (corr > max_corr) {
            max_corr = corr;
            pitch_period = lag;
        }
    }
    features[12] = static_cast<float>(pitch_period) / FRAME_SIZE;  // Normalized pitch period
    features[13] = max_corr > 0.0f ? 1.0f : 0.0f;                // Voiced/unvoiced
    features[14] = logf(std::max(max_corr, 1e-10f));               // Pitch correlation
    features[15] = features[0];                                     // Low band energy
    features[16] = features[5];                                     // High band energy
    features[17] = features[0] - features[5];                       // Spectral tilt

    // Additional features (4 features)
    float total_energy = 0.0f;
    for (int k = 0; k < n_bins; k++) total_energy += power[k];
    features[18] = logf(std::max(total_energy, 1e-10f));           // Total energy
    features[19] = 0.0f;                                            // Spectral centroid placeholder
    features[20] = 0.0f;                                            // Spectral spread placeholder
    features[21] = 0.0f;                                            // Additional feature

    return features;
}

}  // anonymous namespace

// ── JNI ───────────────────────────────────────────────────────────────────────

extern "C" JNIEXPORT jlong JNICALL
Java_com_myvideo_editor_core_ai_denoise_RNNoiseWrapper_nativeInit(
        JNIEnv *env, jobject /*thiz*/, jstring modelPath) {
    if (!modelPath) {
        LOGE("modelPath is null");
        return 0;
    }

    const char* path = env->GetStringUTFChars(modelPath, nullptr);
    std::string pathStr(path);
    env->ReleaseStringUTFChars(modelPath, path);

    auto* ctx = new RNNoiseContext();
    ctx->session.reset(ORTSession::create(pathStr));
    if (!ctx->session) {
        LOGE("Failed to create RNNoise session");
        delete ctx;
        return 0;
    }

    LOGD("RNNoise initialized with model: %s", pathStr.c_str());
    return reinterpret_cast<jlong>(ctx);
}

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_myvideo_editor_core_ai_denoise_RNNoiseWrapper_nativeProcess(
        JNIEnv *env, jobject /*thiz*/,
        jlong handle, jfloatArray pcm) {
    auto* ctx = reinterpret_cast<RNNoiseContext*>(handle);
    if (!ctx || !ctx->session) {
        LOGE("Invalid RNNoise handle");
        return nullptr;
    }

    jfloat* pcmData = env->GetFloatArrayElements(pcm, nullptr);
    if (!pcmData) {
        LOGE("Failed to get PCM data");
        return nullptr;
    }
    int pcmLen = env->GetArrayLength(pcm);

    try {
        auto memory_info = ORTEngine::getMemoryInfo();

        // ── 1. Frame the audio ────────────────────────────────────────────
        int num_frames = pcmLen / FRAME_SIZE;
        if (num_frames == 0) {
            env->ReleaseFloatArrayElements(pcm, pcmData, JNI_ABORT);
            // Return a copy of the input for short audio
            jfloatArray result = env->NewFloatArray(pcmLen);
            env->SetFloatArrayRegion(result, 0, pcmLen, pcmData);
            return result;
        }

        // ── 2. Compute features for each frame ────────────────────────────
        std::vector<float> all_features(num_frames * FEATURE_SIZE);
        for (int f = 0; f < num_frames; f++) {
            auto feat = computeFrameFeatures(pcmData + f * FRAME_SIZE, FRAME_SIZE);
            std::copy(feat.begin(), feat.end(), all_features.begin() + f * FEATURE_SIZE);
        }

        // ── 3. Run ONNX model ────────────────────────────────────────────
        std::vector<int64_t> input_shape = {1, static_cast<int64_t>(num_frames), FEATURE_SIZE};

        std::vector<Ort::Value> input_tensors;
        input_tensors.push_back(Ort::Value::CreateTensor<float>(
                memory_info, all_features.data(), all_features.size(),
                input_shape.data(), input_shape.size()));

        auto output_tensors = ctx->session->run(input_tensors);

        // ── 4. Apply gains to audio ───────────────────────────────────────
        std::vector<float> denoised(pcmLen, 0.0f);

        if (!output_tensors.empty()) {
            auto& gains_out = output_tensors[0];
            auto gains_shape = gains_out.GetTensorTypeAndShapeInfo().GetShape();
            const float* gains_data = gains_out.GetTensorData<float>();

            int gains_feature_size = FEATURE_SIZE;
            if (gains_shape.size() == 3) {
                gains_feature_size = static_cast<int>(gains_shape[2]);
            }

            // Apply band gains using overlap-add
            for (int f = 0; f < num_frames; f++) {
                const float* frame_pcm = pcmData + f * FRAME_SIZE;
                const float* frame_gains = gains_data + f * gains_feature_size;

                // Compute frame energy in each band
                // Apply the first BANDS gains as suppression factors
                float suppression = 1.0f;
                for (int b = 0; b < std::min(BANDS, gains_feature_size); b++) {
                    // gains represent how much of each band to keep
                    float g = frame_gains[b];
                    // Sigmoid-like mapping if gains are logits
                    if (g < 0.0f || g > 1.0f) {
                        g = 1.0f / (1.0f + expf(-g));
                    }
                    suppression = std::min(suppression, g);
                }

                // Apply suppression with Hann window for overlap-add
                for (int i = 0; i < FRAME_SIZE; i++) {
                    float w = 0.5f * (1.0f - cosf(2.0f * static_cast<float>(M_PI) * i / (FRAME_SIZE - 1)));
                    denoised[f * FRAME_SIZE + i] += frame_pcm[i] * suppression * w;
                }
            }
        } else {
            // Fallback: simple passthrough
            std::copy(pcmData, pcmData + pcmLen, denoised.begin());
        }

        env->ReleaseFloatArrayElements(pcm, pcmData, JNI_ABORT);

        // ── 5. Return denoised audio ──────────────────────────────────────
        jfloatArray result = env->NewFloatArray(pcmLen);
        if (!result) {
            LOGE("Failed to allocate output array");
            return nullptr;
        }
        env->SetFloatArrayRegion(result, 0, pcmLen, denoised.data());
        return result;

    } catch (const Ort::Exception& e) {
        LOGE("RNNoise inference error: %s", e.what());
        env->ReleaseFloatArrayElements(pcm, pcmData, JNI_ABORT);
        return nullptr;
    } catch (const std::exception& e) {
        LOGE("RNNoise error: %s", e.what());
        env->ReleaseFloatArrayElements(pcm, pcmData, JNI_ABORT);
        return nullptr;
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_myvideo_editor_core_ai_denoise_RNNoiseWrapper_nativeRelease(
        JNIEnv *env, jobject /*thiz*/, jlong handle) {
    auto* ctx = reinterpret_cast<RNNoiseContext*>(handle);
    if (ctx) {
        ctx->session.reset();
        delete ctx;
        LOGD("RNNoise released");
    }
}
