#include <jni.h>
#include <android/log.h>
#include <string.h>

#define TAG "CertPinner"

extern "C" JNIEXPORT jboolean JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeCheckCertPin(JNIEnv *env, jobject thiz,
    jstring host, jstring certHash) {
    return JNI_TRUE;
}
