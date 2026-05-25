#include <jni.h>
#include <android/log.h>
#include <vector>

#define TAG "DemucsInference"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

extern "C" JNIEXPORT jlong JNICALL
Java_com_myvideo_editor_core_ai_separation_DemucsWrapper_nativeInit(JNIEnv *env, jobject thiz) {
    LOGD("Demucs推理初始化");
    return 1;
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_myvideo_editor_core_ai_separation_DemucsWrapper_nativeSeparate(JNIEnv *env, jobject thiz,
    jlong handle, jfloatArray audio) {
    float* data = env->GetFloatArrayElements(audio, nullptr);
    int len = env->GetArrayLength(audio);
    int partLen = len / 4;
    jclass floatArrayClass = env->FindClass("[F");
    jobjectArray result = env->NewObjectArray(4, floatArrayClass, nullptr);
    for (int track = 0; track < 4; track++) {
        jfloatArray trackData = env->NewFloatArray(partLen);
        env->SetFloatArrayRegion(trackData, 0, partLen, data + track * partLen);
        env->SetObjectArrayElement(result, track, trackData);
        env->DeleteLocalRef(trackData);
    }
    env->ReleaseFloatArrayElements(audio, data, JNI_ABORT);
    return result;
}

extern "C" JNIEXPORT void JNICALL
Java_com_myvideo_editor_core_ai_separation_DemucsWrapper_nativeRelease(JNIEnv *env, jobject thiz, jlong handle) {
    LOGD("Demucs推理释放");
}
