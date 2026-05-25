#include <jni.h>
#include <android/log.h>
#include <vector>

#define TAG "RIFEInference"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

extern "C" JNIEXPORT jlong JNICALL
Java_com_myvideo_editor_core_ai_interpolation_RIFEWrapper_nativeInit(JNIEnv *env, jobject thiz) {
    LOGD("RIFE推理初始化");
    return 1;
}

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_myvideo_editor_core_ai_interpolation_RIFEWrapper_nativeInterpolate(JNIEnv *env, jobject thiz,
    jlong handle, jfloatArray frame1, jfloatArray frame2, jint width, jint height, jfloat t) {
    int size = width * height * 3;
    float* f1 = env->GetFloatArrayElements(frame1, nullptr);
    float* f2 = env->GetFloatArrayElements(frame2, nullptr);
    jfloatArray result = env->NewFloatArray(size);
    std::vector<float> output(size);
    for (int i = 0; i < size; i++) {
        output[i] = f1[i] * (1.0f - t) + f2[i] * t;
    }
    env->ReleaseFloatArrayElements(frame1, f1, JNI_ABORT);
    env->ReleaseFloatArrayElements(frame2, f2, JNI_ABORT);
    env->SetFloatArrayRegion(result, 0, size, output.data());
    return result;
}

extern "C" JNIEXPORT void JNICALL
Java_com_myvideo_editor_core_ai_interpolation_RIFEWrapper_nativeRelease(JNIEnv *env, jobject thiz, jlong handle) {
    LOGD("RIFE推理释放");
}
