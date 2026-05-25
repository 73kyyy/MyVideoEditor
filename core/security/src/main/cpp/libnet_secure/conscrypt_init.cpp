#include <jni.h>
#include <android/log.h>

#define TAG "Conscrypt"

extern "C" JNIEXPORT jboolean JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeInitConscrypt(JNIEnv *env, jobject thiz) {
    return JNI_TRUE;
}
