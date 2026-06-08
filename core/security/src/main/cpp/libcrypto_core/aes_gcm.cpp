#include <jni.h>
#include <android/log.h>
#include <string.h>

// OpenSSL headers
#include <openssl/evp.h>
#include <openssl/rand.h>
#include <openssl/err.h>
#include <openssl/aes.h>
#include <openssl/gcm.h>

#define TAG "AESGCM"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

static const int KEY_LEN = 32;
static const int IV_LEN = 12;
static const int TAG_LEN = 16;

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeAesGcmEncrypt(JNIEnv *env, jobject thiz,
    jbyteArray key, jbyteArray iv, jbyteArray plaintext) {
    if (!key || !iv || !plaintext) {
        LOGE("Encrypt: null input");
        return nullptr;
    }

    int keyLen = env->GetArrayLength(key);
    int ivLen = env->GetArrayLength(iv);
    int ptLen = env->GetArrayLength(plaintext);

    if (keyLen != KEY_LEN) { LOGE("Encrypt: invalid key length %d", keyLen); return nullptr; }
    if (ivLen != IV_LEN) { LOGE("Encrypt: invalid iv length %d", ivLen); return nullptr; }

    // Get input data
    jbyte* keyData = env->GetByteArrayElements(key, nullptr);
    jbyte* ivData = env->GetByteArrayElements(iv, nullptr);
    jbyte* ptData = env->GetByteArrayElements(plaintext, nullptr);

    // Allocate output: ciphertext + tag
    int ctLen = ptLen + TAG_LEN;
    jbyteArray result = env->NewByteArray(ctLen);
    jbyte* outBuf = env->GetByteArrayElements(result, nullptr);

    // Real OpenSSL AES-256-GCM encryption
    EVP_CIPHER_CTX* ctx = EVP_CIPHER_CTX_new();
    if (!ctx) {
        LOGE("Encrypt: failed to create cipher context");
        env->ReleaseByteArrayElements(key, keyData, JNI_ABORT);
        env->ReleaseByteArrayElements(iv, ivData, JNI_ABORT);
        env->ReleaseByteArrayElements(plaintext, ptData, JNI_ABORT);
        return nullptr;
    }

    int outLen = 0, totalLen = 0;

    // Initialize encryption operation
    if (EVP_EncryptInit_ex(ctx, EVP_aes_256_gcm(), nullptr, nullptr, nullptr) != 1) {
        LOGE("Encrypt: init failed");
        ERR_print_errors_fp(stderr);
        goto cleanup;
    }

    // Set IV length
    if (EVP_CIPHER_CTX_ctrl(ctx, EVP_CTRL_GCM_SET_IVLEN, IV_LEN, nullptr) != 1) {
        LOGE("Encrypt: set ivlen failed");
        goto cleanup;
    }

    // Set key and IV
    if (EVP_EncryptInit_ex(ctx, nullptr, nullptr,
            (const unsigned char*)keyData,
            (const unsigned char*)ivData) != 1) {
        LOGE("Encrypt: set key/iv failed");
        goto cleanup;
    }

    // Encrypt data
    if (EVP_EncryptUpdate(ctx, (unsigned char*)outBuf, &outLen,
            (const unsigned char*)ptData, ptLen) != 1) {
        LOGE("Encrypt: update failed");
        goto cleanup;
    }
    totalLen += outLen;

    // Finalize
    if (EVP_EncryptFinal_ex(ctx, (unsigned char*)(outBuf + totalLen), &outLen) != 1) {
        LOGE("Encrypt: final failed");
        goto cleanup;
    }
    totalLen += outLen;

    // Get authentication tag
    if (EVP_CIPHER_CTX_ctrl(ctx, EVP_CTRL_GCM_GET_TAG, TAG_LEN, outBuf + ptLen) != 1) {
        LOGE("Encrypt: get tag failed");
        goto cleanup;
    }

cleanup:
    EVP_CIPHER_CTX_free(ctx);
    env->ReleaseByteArrayElements(key, keyData, JNI_ABORT);
    env->ReleaseByteArrayElements(iv, ivData, JNI_ABORT);
    env->ReleaseByteArrayElements(plaintext, ptData, JNI_ABORT);
    env->ReleaseByteArrayElements(result, outBuf, 0);
    return result;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeAesGcmDecrypt(JNIEnv *env, jobject thiz,
    jbyteArray key, jbyteArray iv, jbyteArray ciphertext) {
    if (!key || !iv || !ciphertext) {
        LOGE("Decrypt: null input");
        return nullptr;
    }

    int keyLen = env->GetArrayLength(key);
    int ivLen = env->GetArrayLength(iv);
    int ctTotalLen = env->GetArrayLength(ciphertext);

    if (keyLen != KEY_LEN) { LOGE("Decrypt: invalid key length %d", keyLen); return nullptr; }
    if (ivLen != IV_LEN) { LOGE("Decrypt: invalid iv length %d", ivLen); return nullptr; }
    if (ctTotalLen <= TAG_LEN) { LOGE("Decrypt: ciphertext too short %d", ctTotalLen); return nullptr; }

    int ctLen = ctTotalLen - TAG_LEN;

    // Get input data
    jbyte* keyData = env->GetByteArrayElements(key, nullptr);
    jbyte* ivData = env->GetByteArrayElements(iv, nullptr);
    jbyte* ctData = env->GetByteArrayElements(ciphertext, nullptr);

    // Allocate output for plaintext
    jbyteArray result = env->NewByteArray(ctLen);
    jbyte* outBuf = env->GetByteArrayElements(result, nullptr);

    // Real OpenSSL AES-256-GCM decryption
    EVP_CIPHER_CTX* ctx = EVP_CIPHER_CTX_new();
    if (!ctx) {
        LOGE("Decrypt: failed to create cipher context");
        env->ReleaseByteArrayElements(key, keyData, JNI_ABORT);
        env->ReleaseByteArrayElements(iv, ivData, JNI_ABORT);
        env->ReleaseByteArrayElements(ciphertext, ctData, JNI_ABORT);
        return nullptr;
    }

    int outLen = 0, totalLen = 0;

    // Initialize decryption
    if (EVP_DecryptInit_ex(ctx, EVP_aes_256_gcm(), nullptr, nullptr, nullptr) != 1) {
        LOGE("Decrypt: init failed");
        goto cleanup;
    }

    // Set IV length
    if (EVP_CIPHER_CTX_ctrl(ctx, EVP_CTRL_GCM_SET_IVLEN, IV_LEN, nullptr) != 1) {
        LOGE("Decrypt: set ivlen failed");
        goto cleanup;
    }

    // Set key and IV
    if (EVP_DecryptInit_ex(ctx, nullptr, nullptr,
            (const unsigned char*)keyData,
            (const unsigned char*)ivData) != 1) {
        LOGE("Decrypt: set key/iv failed");
        goto cleanup;
    }

    // Set expected tag value
    if (EVP_CIPHER_CTX_ctrl(ctx, EVP_CTRL_GCM_SET_TAG, TAG_LEN,
            (unsigned char*)(ctData + ctLen)) != 1) {
        LOGE("Decrypt: set tag failed");
        goto cleanup;
    }

    // Decrypt data
    if (EVP_DecryptUpdate(ctx, (unsigned char*)outBuf, &outLen,
            (const unsigned char*)ctData, ctLen) != 1) {
        LOGE("Decrypt: update failed");
        goto cleanup;
    }
    totalLen += outLen;

    // Finalize and verify tag
    int ret = EVP_DecryptFinal_ex(ctx, (unsigned char*)(outBuf + totalLen), &outLen);
    if (ret <= 0) {
        LOGE("Decrypt: verification FAILED - data may have been tampered!");
        // Clear output on failure
        memset(outBuf, 0, ctLen);
        goto cleanup;
    }
    totalLen += outLen;
    LOGD("Decrypt: success, %d bytes decrypted", totalLen);

cleanup:
    EVP_CIPHER_CTX_free(ctx);
    env->ReleaseByteArrayElements(key, keyData, JNI_ABORT);
    env->ReleaseByteArrayElements(iv, ivData, JNI_ABORT);
    env->ReleaseByteArrayElements(ciphertext, ctData, JNI_ABORT);
    env->ReleaseByteArrayElements(result, outBuf, 0);
    return result;
}
