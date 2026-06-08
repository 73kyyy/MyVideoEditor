#include <jni.h>
#include <android/log.h>
#include <vector>
#include <string>

#include "onnxruntime_helper.h"

#define TAG "ESRGANInference"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

struct ESRGANContext {
    std::unique_ptr<ORTSession> session;
};

extern "C" JNIEXPORT jlong JNICALL
Java_com_myvideo_editor_core_ai_enhancement_ESRGANWrapper_nativeInit(
        JNIEnv *env, jobject /*thiz*/, jstring modelPath) {
    if (!modelPath) {
        LOGE("modelPath is null");
        return 0;
    }

    const char* path = env->GetStringUTFChars(modelPath, nullptr);
    std::string pathStr(path);
    env->ReleaseStringUTFChars(modelPath, path);

    auto* ctx = new ESRGANContext();
    ctx->session.reset(ORTSession::create(pathStr));
    if (!ctx->session) {
        LOGE("Failed to create ESRGAN session");
        delete ctx;
        return 0;
    }

    LOGD("ESRGAN initialized with model: %s", pathStr.c_str());
    return reinterpret_cast<jlong>(ctx);
}

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_myvideo_editor_core_ai_enhancement_ESRGANWrapper_nativeUpscale(
        JNIEnv *env, jobject /*thiz*/,
        jlong handle, jfloatArray input, jint inW, jint inH, jint scale) {
    auto* ctx = reinterpret_cast<ESRGANContext*>(handle);
    if (!ctx || !ctx->session) {
        LOGE("Invalid ESRGAN handle");
        return nullptr;
    }

    const int inSize = inW * inH;
    const int inTotal = 3 * inSize;

    jfloat* inData = env->GetFloatArrayElements(input, nullptr);
    if (!inData) {
        LOGE("Failed to get input data");
        return nullptr;
    }

    try {
        auto memory_info = ORTEngine::getMemoryInfo();

        // Input: [1, 3, H, W]
        std::vector<int64_t> input_shape = {1, 3, static_cast<int64_t>(inH), static_cast<int64_t>(inW)};

        std::vector<Ort::Value> input_tensors;
        input_tensors.push_back(Ort::Value::CreateTensor<float>(
                memory_info, inData, inTotal, input_shape.data(), input_shape.size()));

        // Run inference
        auto output_tensors = ctx->session->run(input_tensors);

        if (output_tensors.empty()) {
            LOGE("ESRGAN inference returned no outputs");
            env->ReleaseFloatArrayElements(input, inData, JNI_ABORT);
            return nullptr;
        }

        auto& output = output_tensors[0];
        auto output_shape = output.GetTensorTypeAndShapeInfo().GetShape();
        size_t output_size = 1;
        for (auto dim : output_shape) output_size *= dim;

        const float* output_data = output.GetTensorData<float>();

        // Create result array
        jfloatArray result = env->NewFloatArray(static_cast<jsize>(output_size));
        if (!result) {
            LOGE("Failed to allocate output array");
            env->ReleaseFloatArrayElements(input, inData, JNI_ABORT);
            return nullptr;
        }

        env->SetFloatArrayRegion(result, 0, static_cast<jsize>(output_size), output_data);

        env->ReleaseFloatArrayElements(input, inData, JNI_ABORT);
        return result;
    } catch (const Ort::Exception& e) {
        LOGE("ESRGAN inference error: %s", e.what());
        env->ReleaseFloatArrayElements(input, inData, JNI_ABORT);
        return nullptr;
    } catch (const std::exception& e) {
        LOGE("ESRGAN error: %s", e.what());
        env->ReleaseFloatArrayElements(input, inData, JNI_ABORT);
        return nullptr;
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_myvideo_editor_core_ai_enhancement_ESRGANWrapper_nativeRelease(
        JNIEnv *env, jobject /*thiz*/, jlong handle) {
    auto* ctx = reinterpret_cast<ESRGANContext*>(handle);
    if (ctx) {
        ctx->session.reset();
        delete ctx;
        LOGD("ESRGAN released");
    }
}
