#include <jni.h>
#include <android/log.h>

#define TAG "DexFragLoader"

extern "C" JNIEXPORT jboolean JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeLoadDexFragment(JNIEnv *env, jobject thiz,
    jbyteArray fragment, jint index) {
    return JNI_TRUE;
}
