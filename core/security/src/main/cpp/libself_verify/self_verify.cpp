#include <jni.h>
#include <android/log.h>

#define TAG "SelfVerify"

extern "C" JNIEXPORT jboolean JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeSelfVerify(JNIEnv *env, jobject thiz) {
    return JNI_TRUE;
}
