#include <jni.h>
#include <android/log.h>
#include <vector>
#include <string>
#include <cmath>

#define TAG "RIFEInference"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

#if USE_ONNX_RUNTIME

#include "onnxruntime_helper.h"

struct RIFEContext {
    std::unique_ptr<ORTSession> session;
};

extern "C" JNIEXPORT jlong JNICALL
Java_com_myvideo_editor_core_ai_interpolation_RIFEWrapper_nativeInit(
        JNIEnv *env, jobject /*thiz*/, jstring modelPath) {
    if (!modelPath) {
        LOGE("modelPath is null");
        return 0;
    }

    const char* path = env->GetStringUTFChars(modelPath, nullptr);
    std::string pathStr(path);
    env->ReleaseStringUTFChars(modelPath, path);

    auto* ctx = new RIFEContext();
    ctx->session.reset(ORTSession::create(pathStr));
    if (!ctx->session) {
        LOGE("Failed to create RIFE session");
        delete ctx;
        return 0;
    }

    LOGD("RIFE initialized with model: %s", pathStr.c_str());
    return reinterpret_cast<jlong>(ctx);
}

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_myvideo_editor_core_ai_interpolation_RIFEWrapper_nativeInterpolate(
        JNIEnv *env, jobject /*thiz*/,
        jlong handle, jfloatArray frame1, jfloatArray frame2,
        jint width, jint height, jfloat t) {
    auto* ctx = reinterpret_cast<RIFEContext*>(handle);
    if (!ctx || !ctx->session) {
        LOGE("Invalid RIFE handle");
        return nullptr;
    }

    const int pixelCount = width * height;
    const int frameSize = 3 * pixelCount;  // CHW: 3 channels

    jfloat* f1 = env->GetFloatArrayElements(frame1, nullptr);
    jfloat* f2 = env->GetFloatArrayElements(frame2, nullptr);
    if (!f1 || !f2) {
        LOGE("Failed to get frame data");
        if (f1) env->ReleaseFloatArrayElements(frame1, f1, JNI_ABORT);
        if (f2) env->ReleaseFloatArrayElements(frame2, f2, JNI_ABORT);
        return nullptr;
    }

    try {
        auto memory_info = ORTEngine::getMemoryInfo();

        // Input tensors: img0 [1,3,H,W], img1 [1,3,H,W], timestep [1]
        std::vector<int64_t> frame_shape = {1, 3, static_cast<int64_t>(height), static_cast<int64_t>(width)};
        std::vector<int64_t> timestep_shape = {1};

        std::vector<Ort::Value> input_tensors;
        input_tensors.reserve(3);

        input_tensors.push_back(Ort::Value::CreateTensor<float>(
                memory_info, f1, frameSize, frame_shape.data(), frame_shape.size()));
        input_tensors.push_back(Ort::Value::CreateTensor<float>(
                memory_info, f2, frameSize, frame_shape.data(), frame_shape.size()));

        float timestep_val = t;
        input_tensors.push_back(Ort::Value::CreateTensor<float>(
                memory_info, &timestep_val, 1, timestep_shape.data(), timestep_shape.size()));

        // Run inference
        auto output_tensors = ctx->session->run(input_tensors);

        if (output_tensors.empty()) {
            LOGE("RIFE inference returned no outputs");
            env->ReleaseFloatArrayElements(frame1, f1, JNI_ABORT);
            env->ReleaseFloatArrayElements(frame2, f2, JNI_ABORT);
            return nullptr;
        }

        // Get output data
        auto& output = output_tensors[0];
        auto output_shape = output.GetTensorTypeAndShapeInfo().GetShape();
        size_t output_size = 1;
        for (auto dim : output_shape) output_size *= dim;

        const float* output_data = output.GetTensorData<float>();

        // Create result array
        jfloatArray result = env->NewFloatArray(static_cast<jsize>(output_size));
        if (!result) {
            LOGE("Failed to allocate output array");
            env->ReleaseFloatArrayElements(frame1, f1, JNI_ABORT);
            env->ReleaseFloatArrayElements(frame2, f2, JNI_ABORT);
            return nullptr;
        }

        env->SetFloatArrayRegion(result, 0, static_cast<jsize>(output_size), output_data);

        env->ReleaseFloatArrayElements(frame1, f1, JNI_ABORT);
        env->ReleaseFloatArrayElements(frame2, f2, JNI_ABORT);

        return result;
    } catch (const Ort::Exception& e) {
        LOGE("RIFE inference error: %s", e.what());
        env->ReleaseFloatArrayElements(frame1, f1, JNI_ABORT);
        env->ReleaseFloatArrayElements(frame2, f2, JNI_ABORT);
        return nullptr;
    } catch (const std::exception& e) {
        LOGE("RIFE error: %s", e.what());
        env->ReleaseFloatArrayElements(frame1, f1, JNI_ABORT);
        env->ReleaseFloatArrayElements(frame2, f2, JNI_ABORT);
        return nullptr;
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_myvideo_editor_core_ai_interpolation_RIFEWrapper_nativeRelease(
        JNIEnv *env, jobject /*thiz*/, jlong handle) {
    auto* ctx = reinterpret_cast<RIFEContext*>(handle);
    if (ctx) {
        ctx->session.reset();
        delete ctx;
        LOGD("RIFE released");
    }
}

#else // !USE_ONNX_RUNTIME

extern "C" JNIEXPORT jlong JNICALL
Java_com_myvideo_editor_core_ai_interpolation_RIFEWrapper_nativeInit(
        JNIEnv *env, jobject /*thiz*/, jstring modelPath) {
    LOGE("ONNX Runtime not available - RIFE nativeInit stub");
    return 0;
}

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_myvideo_editor_core_ai_interpolation_RIFEWrapper_nativeInterpolate(
        JNIEnv *env, jobject /*thiz*/,
        jlong handle, jfloatArray frame1, jfloatArray frame2,
        jint width, jint height, jfloat t) {
    LOGE("ONNX Runtime not available - RIFE nativeInterpolate stub");
    return nullptr;
}

extern "C" JNIEXPORT void JNICALL
Java_com_myvideo_editor_core_ai_interpolation_RIFEWrapper_nativeRelease(
        JNIEnv *env, jobject /*thiz*/, jlong handle) {
    LOGE("ONNX Runtime not available - RIFE nativeRelease stub");
}

#endif // USE_ONNX_RUNTIME
