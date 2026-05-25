#include <jni.h>
#include <android/log.h>
#include <string.h>

#define TAG "ChunkDecryptor"

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeDecryptChunk(JNIEnv *env, jobject thiz,
    jbyteArray encrypted, jint chunkIndex, jbyteArray key) {
    int len = env->GetArrayLength(encrypted);
    jbyte* data = env->GetByteArrayElements(encrypted, nullptr);
    jbyte* k = env->GetByteArrayElements(key, nullptr);
    int kLen = env->GetArrayLength(key);
    unsigned char* decrypted = new unsigned char[len];
    for (int i = 0; i < len; i++) {
        decrypted[i] = data[i] ^ k[(i + chunkIndex) % kLen];
    }
    env->ReleaseByteArrayElements(encrypted, data, JNI_ABORT);
    env->ReleaseByteArrayElements(key, k, JNI_ABORT);
    jbyteArray result = env->NewByteArray(len);
    env->SetByteArrayRegion(result, 0, len, (jbyte*)decrypted);
    delete[] decrypted;
    return result;
}
