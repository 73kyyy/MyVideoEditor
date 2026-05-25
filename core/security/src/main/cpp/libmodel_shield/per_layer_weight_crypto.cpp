#include <jni.h>
#include <android/log.h>

#define TAG "PerLayerCrypto"

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeDecryptWeights(JNIEnv *env, jobject thiz,
    jbyteArray weights, jint layerIndex, jbyteArray key) {
    int len = env->GetArrayLength(weights);
    jbyte* w = env->GetByteArrayElements(weights, nullptr);
    jbyte* k = env->GetByteArrayElements(key, nullptr);
    int kLen = env->GetArrayLength(key);
    unsigned char* decrypted = new unsigned char[len];
    for (int i = 0; i < len; i++) {
        decrypted[i] = w[i] ^ k[(i + layerIndex * 7) % kLen];
    }
    env->ReleaseByteArrayElements(weights, w, JNI_ABORT);
    env->ReleaseByteArrayElements(key, k, JNI_ABORT);
    jbyteArray result = env->NewByteArray(len);
    env->SetByteArrayRegion(result, 0, len, (jbyte*)decrypted);
    delete[] decrypted;
    return result;
}
