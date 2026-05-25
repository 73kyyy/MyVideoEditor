#include <jni.h>
#include <android/log.h>

#define TAG "AntiSniff"

extern "C" JNIEXPORT jboolean JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeCheckSniff(JNIEnv *env, jobject thiz) {
    return JNI_FALSE;
}
