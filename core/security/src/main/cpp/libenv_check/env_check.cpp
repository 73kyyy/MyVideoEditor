#include <jni.h>
#include <android/log.h>
#include <sys/system_properties.h>

#define TAG "EnvCheck"

extern "C" JNIEXPORT jboolean JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeCheckEnv(JNIEnv *env, jobject thiz) {
    char sdk[PROP_VALUE_MAX];
    __system_property_get("ro.build.version.sdk", sdk);
    int api = atoi(sdk);
    return (api < 21) ? JNI_TRUE : JNI_FALSE;
}
