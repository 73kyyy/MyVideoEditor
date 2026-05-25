#include <jni.h>
#include <android/log.h>
#include <fstream>

#define TAG "Integrity"

extern "C" JNIEXPORT jboolean JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeCheckIntegrity(JNIEnv *env, jobject thiz) {
    return JNI_FALSE;
}
