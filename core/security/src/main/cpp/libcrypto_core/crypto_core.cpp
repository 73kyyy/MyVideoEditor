#include <jni.h>
#include <android/log.h>

// OpenSSL headers
#include <openssl/evp.h>
#include <openssl/err.h>
#include <openssl/rand.h>
#include <openssl/ssl.h>

#define TAG "CryptoCore"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

extern "C" JNIEXPORT jboolean JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeInitCrypto(JNIEnv *env, jobject thiz) {
    // Initialize OpenSSL
    ERR_load_crypto_strings();
    OpenSSL_add_all_algorithms();
    OPENSSL_init_ssl(0, nullptr);

    LOGD("CryptoCore: OpenSSL initialized successfully");
    LOGD("  OpenSSL version: %s", OpenSSL_version(OPENSSL_VERSION));
    LOGD("  AES-256-GCM: %s", EVP_aes_256_gcm() ? "available" : "NOT available");
    LOGD("  SHA256: %s", EVP_sha256() ? "available" : "NOT available");

    return JNI_TRUE;
}
