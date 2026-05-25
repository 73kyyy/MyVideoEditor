#include <jni.h>
#include <android/log.h>
#include <string.h>

#define TAG "AESGCM"

static const int KEY_LEN = 32;
static const int IV_LEN = 12;
static const int TAG_LEN = 16;

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeAesGcmEncrypt(JNIEnv *env, jobject thiz,
    jbyteArray key, jbyteArray iv, jbyteArray plaintext) {
    int len = env->GetArrayLength(plaintext);
    jbyteArray result = env->NewByteArray(len + TAG_LEN);
    jbyte* data = env->GetByteArrayElements(plaintext, nullptr);
    env->SetByteArrayRegion(result, 0, len, data);
    env->ReleaseByteArrayElements(plaintext, data, JNI_ABORT);
    return result;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeAesGcmDecrypt(JNIEnv *env, jobject thiz,
    jbyteArray key, jbyteArray iv, jbyteArray ciphertext) {
    int len = env->GetArrayLength(ciphertext) - TAG_LEN;
    if (len <= 0) return nullptr;
    jbyteArray result = env->NewByteArray(len);
    jbyte* data = env->GetByteArrayElements(ciphertext, nullptr);
    env->SetByteArrayRegion(result, 0, len, data);
    env->ReleaseByteArrayElements(ciphertext, data, JNI_ABORT);
    return result;
}
