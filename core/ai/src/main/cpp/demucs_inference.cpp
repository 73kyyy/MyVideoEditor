#include <jni.h>
#include <android/log.h>
#include <vector>
#include <string>
#include <cstring>

#include "onnxruntime_helper.h"

#define TAG "DemucsInference"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

struct DemucsContext {
    std::unique_ptr<ORTSession> session;
};

extern "C" JNIEXPORT jlong JNICALL
Java_com_myvideo_editor_core_ai_separation_DemucsWrapper_nativeInit(
        JNIEnv *env, jobject /*thiz*/, jstring modelPath) {
    if (!modelPath) {
        LOGE("modelPath is null");
        return 0;
    }

    const char* path = env->GetStringUTFChars(modelPath, nullptr);
    std::string pathStr(path);
    env->ReleaseStringUTFChars(modelPath, path);

    auto* ctx = new DemucsContext();
    ctx->session.reset(ORTSession::create(pathStr));
    if (!ctx->session) {
        LOGE("Failed to create Demucs session");
        delete ctx;
        return 0;
    }

    LOGD("Demucs initialized with model: %s", pathStr.c_str());
    return reinterpret_cast<jlong>(ctx);
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_myvideo_editor_core_ai_separation_DemucsWrapper_nativeSeparate(
        JNIEnv *env, jobject /*thiz*/,
        jlong handle, jfloatArray audio) {
    auto* ctx = reinterpret_cast<DemucsContext*>(handle);
    if (!ctx || !ctx->session) {
        LOGE("Invalid Demucs handle");
        return nullptr;
    }

    jfloat* audioData = env->GetFloatArrayElements(audio, nullptr);
    if (!audioData) {
        LOGE("Failed to get audio data");
        return nullptr;
    }
    int audioLen = env->GetArrayLength(audio);

    try {
        auto memory_info = ORTEngine::getMemoryInfo();

        // Input shape: [1, 2, samples] for stereo or [1, 1, samples] for mono
        // Determine channels from input size heuristic: if even length, assume stereo interleaved
        int channels = 2;
        int samples = audioLen / channels;

        std::vector<int64_t> input_shape = {1, channels, samples};
        std::vector<float> planar(channels * samples);

        // Convert interleaved to planar
        for (int s = 0; s < samples; s++) {
            for (int c = 0; c < channels; c++) {
                planar[c * samples + s] = audioData[s * channels + c];
            }
        }
        env->ReleaseFloatArrayElements(audio, audioData, JNI_ABORT);

        std::vector<Ort::Value> input_tensors;
        input_tensors.push_back(Ort::Value::CreateTensor<float>(
                memory_info, planar.data(), planar.size(),
                input_shape.data(), input_shape.size()));

        // Run inference
        auto output_tensors = ctx->session->run(input_tensors);

        if (output_tensors.empty()) {
            LOGE("Demucs inference returned no outputs");
            return nullptr;
        }

        // Output shape: typically [1, 4, 2, samples] for 4 stems x stereo
        auto& output = output_tensors[0];
        auto output_shape = output.GetTensorTypeAndShapeInfo().GetShape();
        const float* output_data = output.GetTensorData<float>();

        // Determine number of stems and output layout
        int num_stems = 4;  // drums, bass, other, vocals
        int out_channels = channels;
        int out_samples = samples;

        if (output_shape.size() == 4) {
            num_stems = static_cast<int>(output_shape[1]);
            out_channels = static_cast<int>(output_shape[2]);
            out_samples = static_cast<int>(output_shape[3]);
        } else if (output_shape.size() == 3) {
            num_stems = static_cast<int>(output_shape[1]);
            out_samples = static_cast<int>(output_shape[2]);
            out_channels = 1;
        }

        // Create result: jobjectArray of 4 jfloatArray (interleaved stereo per stem)
        jclass floatArrayClass = env->FindClass("[F");
        jobjectArray result = env->NewObjectArray(num_stems, floatArrayClass, nullptr);
        if (!result) {
            LOGE("Failed to create result array");
            return nullptr;
        }

        for (int stem = 0; stem < num_stems; stem++) {
            int stemSampleCount = out_channels * out_samples;
            jfloatArray stemData = env->NewFloatArray(stemSampleCount);
            if (!stemData) {
                LOGE("Failed to allocate stem %d array", stem);
                continue;
            }

            std::vector<float> interleaved(stemSampleCount);
            const float* stem_ptr = output_data + stem * out_channels * out_samples;

            // Convert planar to interleaved
            for (int s = 0; s < out_samples; s++) {
                for (int c = 0; c < out_channels; c++) {
                    interleaved[s * out_channels + c] = stem_ptr[c * out_samples + s];
                }
            }

            env->SetFloatArrayRegion(stemData, 0, stemSampleCount, interleaved.data());
            env->SetObjectArrayElement(result, stem, stemData);
            env->DeleteLocalRef(stemData);
        }

        return result;
    } catch (const Ort::Exception& e) {
        LOGE("Demucs inference error: %s", e.what());
        env->ReleaseFloatArrayElements(audio, audioData, JNI_ABORT);
        return nullptr;
    } catch (const std::exception& e) {
        LOGE("Demucs error: %s", e.what());
        env->ReleaseFloatArrayElements(audio, audioData, JNI_ABORT);
        return nullptr;
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_myvideo_editor_core_ai_separation_DemucsWrapper_nativeRelease(
        JNIEnv *env, jobject /*thiz*/, jlong handle) {
    auto* ctx = reinterpret_cast<DemucsContext*>(handle);
    if (ctx) {
        ctx->session.reset();
        delete ctx;
        LOGD("Demucs released");
    }
}
