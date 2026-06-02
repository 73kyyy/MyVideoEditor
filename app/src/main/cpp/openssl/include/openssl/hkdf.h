/*
 * hkdf.h - OpenSSL 3.x compatibility shim
 * 提供旧版HKDF()函数兼容
 */
#ifndef OPENSSL_HKDF_H
#define OPENSSL_HKDF_H

#include <openssl/evp.h>
#include <openssl/kdf.h>
#include <openssl/params.h>
#include <openssl/core_names.h>

/* 旧版 OpenSSL 1.x HKDF() 函数 - native_data_protect.c 调用的 */
static inline int HKDF(unsigned char *out_key, size_t out_len,
                        const EVP_MD *digest,
                        const unsigned char *secret, size_t secret_len,
                        const unsigned char *salt, size_t salt_len,
                        const unsigned char *info, size_t info_len) {
    EVP_KDF *kdf = EVP_KDF_fetch(NULL, "HKDF", NULL);
    EVP_KDF_CTX *ctx = EVP_KDF_CTX_new(kdf);
    OSSL_PARAM params[6], *p = params;
    int ret;

    *p++ = OSSL_PARAM_construct_utf8_string(OSSL_KDF_PARAM_DIGEST,
                                             (char *)EVP_MD_get0_name(digest), 0);
    *p++ = OSSL_PARAM_construct_octet_string(OSSL_KDF_PARAM_KEY,
                                              (void *)secret, secret_len);
    if (salt && salt_len > 0) {
        *p++ = OSSL_PARAM_construct_octet_string(OSSL_KDF_PARAM_SALT,
                                                  (void *)salt, salt_len);
    }
    if (info && info_len > 0) {
        *p++ = OSSL_PARAM_construct_octet_string(OSSL_KDF_PARAM_INFO,
                                                  (void *)info, info_len);
    }
    *p = OSSL_PARAM_construct_end();

    ret = EVP_KDF_derive(ctx, out_key, out_len, params);
    EVP_KDF_CTX_free(ctx);
    EVP_KDF_free(kdf);
    return ret == 1 ? 1 : 0;
}

static inline int HKDF_extract(unsigned char *prk, size_t *prk_len,
                                const EVP_MD *md,
                                const unsigned char *salt, size_t salt_len,
                                const unsigned char *key, size_t key_len) {
    EVP_KDF *kdf = EVP_KDF_fetch(NULL, "HKDF", NULL);
    EVP_KDF_CTX *ctx = EVP_KDF_CTX_new(kdf);
    OSSL_PARAM params[5], *p = params;
    size_t outlen = EVP_MD_get_size(md);
    int ret;
    *p++ = OSSL_PARAM_construct_utf8_string(OSSL_KDF_PARAM_DIGEST,
                                             (char *)EVP_MD_get0_name(md), 0);
    *p++ = OSSL_PARAM_construct_octet_string(OSSL_KDF_PARAM_KEY, (void *)key, key_len);
    *p++ = OSSL_PARAM_construct_octet_string(OSSL_KDF_PARAM_SALT, (void *)salt, salt_len);
    *p++ = OSSL_PARAM_construct_utf8_string(OSSL_KDF_PARAM_MODE, "EXTRACT_AND_EXPAND", 0);
    *p = OSSL_PARAM_construct_end();
    ret = EVP_KDF_derive(ctx, prk, outlen, params);
    if (prk_len) *prk_len = outlen;
    EVP_KDF_CTX_free(ctx);
    EVP_KDF_free(kdf);
    return ret == 1 ? 1 : 0;
}

static inline int HKDF_expand(unsigned char *okm, size_t okm_len,
                               const EVP_MD *md,
                               const unsigned char *prk, size_t prk_len,
                               const unsigned char *info, size_t info_len) {
    EVP_KDF *kdf = EVP_KDF_fetch(NULL, "HKDF", NULL);
    EVP_KDF_CTX *ctx = EVP_KDF_CTX_new(kdf);
    OSSL_PARAM params[5], *p = params;
    int ret;
    *p++ = OSSL_PARAM_construct_utf8_string(OSSL_KDF_PARAM_DIGEST,
                                             (char *)EVP_MD_get0_name(md), 0);
    *p++ = OSSL_PARAM_construct_octet_string(OSSL_KDF_PARAM_KEY, (void *)prk, prk_len);
    *p++ = OSSL_PARAM_construct_octet_string(OSSL_KDF_PARAM_INFO, (void *)info, info_len);
    *p++ = OSSL_PARAM_construct_utf8_string(OSSL_KDF_PARAM_MODE, "EXPAND_ONLY", 0);
    *p = OSSL_PARAM_construct_end();
    ret = EVP_KDF_derive(ctx, okm, okm_len, params);
    EVP_KDF_CTX_free(ctx);
    EVP_KDF_free(kdf);
    return ret == 1 ? 1 : 0;
}

#endif
