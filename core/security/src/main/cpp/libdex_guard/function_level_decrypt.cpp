#include <jni.h>
#include <android/log.h>
#include <string.h>

#define TAG "FuncDecrypt"

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeDecryptFunction(JNIEnv *env, jobject thiz,
    jbyteArray encrypted, jint funcIndex) {
    int len = env->GetArrayLength(encrypted);
    jbyte* data = env->GetByteArrayElements(encrypted, nullptr);
    unsigned char* decrypted = new unsigned char[len];
    unsigned char key = (unsigned char)(funcIndex & 0xFF);
    for (int i = 0; i < len; i++) {
        decrypted[i] = data[i] ^ key;
    }
    env->ReleaseByteArrayElements(encrypted, data, JNI_ABORT);
    jbyteArray result = env->NewByteArray(len);
    env->SetByteArrayRegion(result, 0, len, (jbyte*)decrypted);
    delete[] decrypted;
    return result;
}
