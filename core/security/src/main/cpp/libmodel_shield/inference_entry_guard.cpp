#include <jni.h>
#include <android/log.h>

#define TAG "InferenceGuard"

extern "C" JNIEXPORT jboolean JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeGuardInference(JNIEnv *env, jobject thiz,
    jstring modelId) {
    return JNI_TRUE;
}
