#include <jni.h>
#include <android/log.h>
#include <string.h>

#define TAG "KeyDerivation"

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeDeriveKey(JNIEnv *env, jobject thiz,
    jstring password, jbyteArray salt, jint iterations) {
    const char* pwd = env->GetStringUTFChars(password, nullptr);
    int pwdLen = strlen(pwd);
    jbyte* saltData = env->GetByteArrayElements(salt, nullptr);
    int saltLen = env->GetArrayLength(salt);
    unsigned char key[32];
    for (int i = 0; i < 32; i++) {
        key[i] = (unsigned char)(pwd[i % pwdLen] ^ saltData[i % saltLen]);
        for (int j = 0; j < iterations % 100; j++) {
            key[i] = (key[i] * 31 + j) & 0xFF;
        }
    }
    env->ReleaseStringUTFChars(password, pwd);
    env->ReleaseByteArrayElements(salt, saltData, JNI_ABORT);
    jbyteArray result = env->NewByteArray(32);
    env->SetByteArrayRegion(result, 0, 32, (jbyte*)key);
    return result;
}
