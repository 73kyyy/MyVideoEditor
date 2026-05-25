#include <jni.h>
#include <android/log.h>
#include <string.h>

#define TAG "DexDecryptor"

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeDecryptDex(JNIEnv *env, jobject thiz,
    jbyteArray encrypted) {
    int len = env->GetArrayLength(encrypted);
    jbyte* data = env->GetByteArrayElements(encrypted, nullptr);
    unsigned char* decrypted = new unsigned char[len];
    for (int i = 0; i < len; i++) {
        decrypted[i] = data[i] ^ 0xAA;
    }
    env->ReleaseByteArrayElements(encrypted, data, JNI_ABORT);
    jbyteArray result = env->NewByteArray(len);
    env->SetByteArrayRegion(result, 0, len, (jbyte*)decrypted);
    delete[] decrypted;
    return result;
}
