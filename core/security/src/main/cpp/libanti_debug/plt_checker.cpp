#include <jni.h>
#include <android/log.h>
#include <dlfcn.h>

#define TAG "PltChecker"

extern "C" JNIEXPORT jboolean JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeCheckPlt(JNIEnv *env, jobject thiz) {
    void* handle = dlopen("libc.so", RTLD_NOW);
    if (!handle) return JNI_FALSE;
    void* func = dlsym(handle, "open");
    dlclose(handle);
    if (!func) return JNI_FALSE;
    unsigned char* bytes = (unsigned char*)func;
    if (bytes[0] == 0xE9 || bytes[0] == 0xFF) return JNI_TRUE;
    return JNI_FALSE;
}
