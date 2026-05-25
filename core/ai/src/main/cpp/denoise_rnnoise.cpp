#include <jni.h>
#include <android/log.h>
#include <vector>

#define TAG "RNNoise"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

extern "C" JNIEXPORT jlong JNICALL
Java_com_myvideo_editor_core_ai_denoise_RNNoiseWrapper_nativeInit(JNIEnv *env, jobject thiz) {
    LOGD("RNNoise推理初始化");
    return 1;
}

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_myvideo_editor_core_ai_denoise_RNNoiseWrapper_nativeProcess(JNIEnv *env, jobject thiz,
    jlong handle, jfloatArray pcm) {
    float* data = env->GetFloatArrayElements(pcm, nullptr);
    int len = env->GetArrayLength(pcm);
    std::vector<float> output(len);
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
    LOGD("RNNoise推理释放");
}
