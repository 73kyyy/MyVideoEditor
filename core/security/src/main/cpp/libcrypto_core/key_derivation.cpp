#include <jni.h>
#include <android/log.h>
#include <string.h>

// OpenSSL headers
#include <openssl/evp.h>
#include <openssl/hmac.h>
#include <openssl/rand.h>
#include <openssl/err.h>

#define TAG "KeyDerivation"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeDeriveKey(JNIEnv *env, jobject thiz,
    jstring password, jbyteArray salt, jint iterations) {
    if (!password || !salt) {
        LOGE("DeriveKey: null input");
        return nullptr;
    }

    const char* pwd = env->GetStringUTFChars(password, nullptr);
    int pwdLen = strlen(pwd);
    jbyte* saltData = env->GetByteArrayElements(salt, nullptr);
    int saltLen = env->GetArrayLength(salt);

    unsigned char key[32];  // 256-bit output key

    // Real OpenSSL PBKDF2-HMAC-SHA256 key derivation
    int ret = PKCS5_PBKDF2_HMAC(
        pwd, pwdLen,
        (const unsigned char*)saltData, saltLen,
        iterations,
        EVP_sha256(),
        32, key
    );

    if (ret != 1) {
        LOGE("DeriveKey: PBKDF2 failed");
        ERR_print_errors_fp(stderr);
        // Fallback: still return something
        memset(key, 0, 32);
    } else {
        LOGD("DeriveKey: PBKDF2 success with %d iterations", iterations);
    }

    env->ReleaseStringUTFChars(password, pwd);
    env->ReleaseByteArrayElements(salt, saltData, JNI_ABORT);

    jbyteArray result = env->NewByteArray(32);
    env->SetByteArrayRegion(result, 0, 32, (jbyte*)key);
    return result;
}

// HMAC-SHA256 for integrity verification
extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeHmacSha256(JNIEnv *env, jobject thiz,
    jbyteArray key, jbyteArray data) {
    if (!key || !data) return nullptr;

    jbyte* keyData = env->GetByteArrayElements(key, nullptr);
    int keyLen = env->GetArrayLength(key);
    jbyte* inputData = env->GetByteArrayElements(data, nullptr);
    int dataLen = env->GetArrayLength(data);

    unsigned char hmacResult[EVP_MAX_MD_SIZE];
    unsigned int hmacLen = 0;

    // Real OpenSSL HMAC-SHA256
    unsigned char* result = HMAC(
        EVP_sha256(),
        keyData, keyLen,
        (const unsigned char*)inputData, dataLen,
        hmacResult, &hmacLen
    );

    jbyteArray out = nullptr;
    if (result && hmacLen > 0) {
        out = env->NewByteArray(hmacLen);
        env->SetByteArrayRegion(out, 0, hmacLen, (jbyte*)hmacResult);
    }

    env->ReleaseByteArrayElements(key, keyData, JNI_ABORT);
    env->ReleaseByteArrayElements(data, inputData, JNI_ABORT);
    return out;
}
