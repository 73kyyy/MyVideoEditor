#include <jni.h>
#include <android/log.h>

#define TAG "ModelShield"

extern "C" JNIEXPORT jboolean JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeInitModelShield(JNIEnv *env, jobject thiz) {
    return JNI_TRUE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeReleaseModelShield(JNIEnv *env, jobject thiz) {
}
