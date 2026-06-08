#include <jni.h>
#include <android/log.h>
#include <vector>
#include <cmath>

#if USE_ONNX_RUNTIME
#include "onnxruntime_helper.h"

static ORTEngine* g_rnnoise_engine = nullptr;

extern "C" JNIEXPORT jlong JNICALL
Java_com_myvideo_editor_core_ai_denoise_RNNoiseWrapper_nativeInit(JNIEnv *env, jobject thiz, jstring modelPath) {
    const char* path = env->GetStringUTFChars(modelPath, nullptr);
    LOGD("RNNoise: Initializing with model %s", path);

    ORTEngine* engine = new ORTEngine();
    if (!engine->init(path)) {
        LOGE("RNNoise: Failed to initialize ORT engine");
        delete engine;
        env->ReleaseStringUTFChars(modelPath, path);
        return 0;
    }

    env->ReleaseStringUTFChars(modelPath, path);
    g_rnnoise_engine = engine;
    LOGD("RNNoise: Engine initialized successfully");
    return reinterpret_cast<jlong>(engine);
}

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_myvideo_editor_core_ai_denoise_RNNoiseWrapper_nativeProcess(JNIEnv *env, jobject thiz,
    jlong handle, jfloatArray pcm) {
    auto* engine = reinterpret_cast<ORTEngine*>(handle);
    if (!engine) {
        LOGE("RNNoise: Invalid engine handle");
        return nullptr;
    }

    float* data = env->GetFloatArrayElements(pcm, nullptr);
    int len = env->GetArrayLength(pcm);

    // RNNoise processes audio in frames of 480 samples at 48kHz
    // Each frame produces 65 feature bands
    // The model expects: [1, num_frames, 65] -> gains [1, num_frames-4, 32], vad [1, num_frames-4, 1]
    const int FRAME_SIZE = 480;
    const int NUM_BANDS = 65;
    const int SAMPLE_RATE = 48000;

    int num_frames = len / FRAME_SIZE;
    if (num_frames < 5) {
        // Not enough frames for the Conv1d layers (kernel_size=3, 2 layers -> need at least 5 frames)
        env->ReleaseFloatArrayElements(pcm, data, JNI_ABORT);
        jfloatArray result = env->NewFloatArray(len);
        env->SetFloatArrayRegion(result, 0, len, data);
        return result;
    }

    // Extract features from PCM data (simplified band energy computation)
    // In production, this would use proper bark-scale band extraction
    std::vector<float> features(num_frames * NUM_BANDS, 0.0f);
    for (int f = 0; f < num_frames; f++) {
        float energy = 0.0f;
        for (int i = 0; i < FRAME_SIZE && (f * FRAME_SIZE + i) < len; i++) {
            energy += data[f * FRAME_SIZE + i] * data[f * FRAME_SIZE + i];
        }
        energy /= FRAME_SIZE;

        // Simplified feature extraction: distribute energy across bands
        for (int b = 0; b < NUM_BANDS; b++) {
            features[f * NUM_BANDS + b] = std::sqrt(energy) * (0.5f + 0.5f * std::sin(b * 0.1f + f * 0.01f));
        }
    }

    // Run ONNX inference
    std::vector<int64_t> input_shape = {1, num_frames, NUM_BANDS};
    auto gains_opt = engine->run("features", features, input_shape, "gains");
    auto vad_opt = engine->run("features", features, input_shape, "vad");

    std::vector<float> output(len);
    if (gains_opt.has_value()) {
        const auto& gains = gains_opt.value();
        int out_frames = gains.size() / 32;  // 32 output bands

        // Apply gains to PCM data
        // The Conv1d with kernel_size=3 and 2 layers reduces frames by 4
        int frame_offset = 2;  // Due to Conv1d padding='valid'
        for (int f = 0; f < out_frames && (f + frame_offset) * FRAME_SIZE < len; f++) {
            // Average gain across all bands for this frame
            float avg_gain = 0.0f;
            for (int b = 0; b < 32; b++) {
                avg_gain += gains[f * 32 + b];
            }
            avg_gain /= 32.0f;

            // Apply gain to the frame
            int start = (f + frame_offset) * FRAME_SIZE;
            for (int i = 0; i < FRAME_SIZE && (start + i) < len; i++) {
                output[start + i] = data[start + i] * avg_gain;
            }
        }

        // Copy unprocessed frames at boundaries
        for (int i = 0; i < frame_offset * FRAME_SIZE && i < len; i++) {
            output[i] = data[i];
        }
        int last_processed = (out_frames + frame_offset) * FRAME_SIZE;
        for (int i = last_processed; i < len; i++) {
            output[i] = data[i];
        }
    } else {
        // Fallback: copy input to output
        for (int i = 0; i < len; i++) {
            output[i] = data[i];
        }
    }

    env->ReleaseFloatArrayElements(pcm, data, JNI_ABORT);
    jfloatArray result = env->NewFloatArray(len);
    env->SetFloatArrayRegion(result, 0, len, output.data());
    return result;
}

extern "C" JNIEXPORT void JNICALL
Java_com_myvideo_editor_core_ai_denoise_RNNoiseWrapper_nativeRelease(JNIEnv *env, jobject thiz, jlong handle) {
    auto* engine = reinterpret_cast<ORTEngine*>(handle);
    if (engine) {
        delete engine;
        LOGD("RNNoise: Engine released");
    }
    if (g_rnnoise_engine == engine) {
        g_rnnoise_engine = nullptr;
    }
}

#else
// Stub implementation when ONNX Runtime is not available

extern "C" JNIEXPORT jlong JNICALL
Java_com_myvideo_editor_core_ai_denoise_RNNoiseWrapper_nativeInit(JNIEnv *env, jobject thiz, jstring modelPath) {
    return 1;
}

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_myvideo_editor_core_ai_denoise_RNNoiseWrapper_nativeProcess(JNIEnv *env, jobject thiz,
    jlong handle, jfloatArray pcm) {
    float* data = env->GetFloatArrayElements(pcm, nullptr);
    int len = env->GetArrayLength(pcm);
    std::vector<float> output(len);
    // Simple low-pass filter as stub
    output[0] = data[0];
    for (int i = 1; i < len; i++) {
        output[i] = data[i] * 0.9f + data[i-1] * 0.1f;
    }
    env->ReleaseFloatArrayElements(pcm, data, JNI_ABORT);
    jfloatArray result = env->NewFloatArray(len);
    env->SetFloatArrayRegion(result, 0, len, output.data());
    return result;
}

extern "C" JNIEXPORT void JNICALL
Java_com_myvideo_editor_core_ai_denoise_RNNoiseWrapper_nativeRelease(JNIEnv *env, jobject thiz, jlong handle) {
}

#endif
