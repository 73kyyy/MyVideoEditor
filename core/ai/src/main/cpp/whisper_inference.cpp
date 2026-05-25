#include <jni.h>
#include <android/log.h>
#include <vector>
#include <string>

#define TAG "WhisperInference"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

extern "C" JNIEXPORT jlong JNICALL
Java_com_myvideo_editor_core_ai_speech_WhisperWrapper_nativeInit(JNIEnv *env, jobject thiz) {
    LOGD("Whisper推理初始化");
    return 1;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_myvideo_editor_core_ai_speech_WhisperWrapper_nativeTranscribe(JNIEnv *env, jobject thiz,
    jlong handle, jfloatArray audio, jint sampleRate) {
    float* data = env->GetFloatArrayElements(audio, nullptr);
    int len = env->GetArrayLength(audio);
    std::string result = "AI transcription placeholder";
    env->ReleaseFloatArrayElements(audio, data, JNI_ABORT);
    return env->NewStringUTF(result.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_myvideo_editor_core_ai_speech_WhisperWrapper_nativeRelease(JNIEnv *env, jobject thiz, jlong handle) {
    LOGD("Whisper推理释放");
}
