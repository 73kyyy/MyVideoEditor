#include <jni.h>
#include <android/log.h>
#include <fstream>

#define TAG "SecureRandom"

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeSecureRandom(JNIEnv *env, jobject thiz, jint len) {
    unsigned char* buf = new unsigned char[len];
    std::ifstream urandom("/dev/urandom", std::ios::binary);
    urandom.read((char*)buf, len);
    jbyteArray result = env->NewByteArray(len);
    env->SetByteArrayRegion(result, 0, len, (jbyte*)buf);
    delete[] buf;
    return result;
}
