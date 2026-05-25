#include <jni.h>
#include <android/log.h>

#define TAG "ResponseDecryptor"

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeDecryptResponse(JNIEnv *env, jobject thiz,
    jbyteArray data, jbyteArray key) {
    int len = env->GetArrayLength(data);
    jbyte* d = env->GetByteArrayElements(data, nullptr);
    jbyteArray result = env->NewByteArray(len);
    env->SetByteArrayRegion(result, 0, len, d);
    env->ReleaseByteArrayElements(data, d, JNI_ABORT);
    return result;
}
