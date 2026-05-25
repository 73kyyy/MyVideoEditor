#include <jni.h>
#include <android/log.h>
#include <string.h>

#define TAG "ClassObfuscator"

static char* obfuscate(const char* name) {
    int len = strlen(name);
    char* result = new char[len + 1];
    for (int i = 0; i < len; i++) {
        result[i] = name[i] ^ 0x5A;
    }
    result[len] = '\0';
    return result;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeObfuscateClass(JNIEnv *env, jobject thiz,
    jstring className) {
    const char* name = env->GetStringUTFChars(className, nullptr);
    char* obf = obfuscate(name);
    jstring result = env->NewStringUTF(obf);
    env->ReleaseStringUTFChars(className, name);
    delete[] obf;
    return result;
}
