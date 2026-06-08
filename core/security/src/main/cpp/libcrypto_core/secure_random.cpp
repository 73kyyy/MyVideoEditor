#include <jni.h>
#include <android/log.h>

// OpenSSL headers
#include <openssl/rand.h>
#include <openssl/err.h>

#define TAG "SecureRandom"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeSecureRandom(JNIEnv *env, jobject thiz, jint len) {
    if (len <= 0 || len > 1024 * 1024) {
        LOGE("SecureRandom: invalid length %d", len);
        return nullptr;
    }

    unsigned char* buf = new unsigned char[len];

    // Use OpenSSL's cryptographically secure random number generator
    if (RAND_bytes(buf, len) != 1) {
        LOGE("SecureRandom: RAND_bytes failed, falling back to /dev/urandom");
        ERR_print_errors_fp(stderr);
        // Fallback to /dev/urandom
        FILE* f = fopen("/dev/urandom", "rb");
        if (f) {
            fread(buf, 1, len, f);
            fclose(f);
        } else {
            LOGE("SecureRandom: both RAND_bytes and /dev/urandom failed!");
            delete[] buf;
            return nullptr;
        }
    }

    jbyteArray result = env->NewByteArray(len);
    env->SetByteArrayRegion(result, 0, len, (jbyte*)buf);

    // Securely wipe the buffer
    OPENSSL_cleanse(buf, len);
    delete[] buf;

    LOGD("SecureRandom: generated %d bytes via OpenSSL RAND_bytes", len);
    return result;
}
