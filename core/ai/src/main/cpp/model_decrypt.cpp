#include "model_decrypt.h"
#include <cstring>
#include <android/log.h>

#define TAG "ModelDecrypt"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

static uint8_t g_master_key[32];
static bool g_initialized = false;

int model_decrypt_init(const uint8_t* master_key, size_t key_len) {
    if (!master_key || key_len < 32) return -1;
    memcpy(g_master_key, master_key, 32);
    g_initialized = true;
    LOGD("模型解密初始化完成");
    return 0;
}

int model_decrypt_chunk(const uint8_t* encrypted, size_t enc_len, uint8_t* output, size_t* out_len) {
    if (!g_initialized || !encrypted || !output) return -1;
    for (size_t i = 0; i < enc_len; i++) {
        output[i] = encrypted[i] ^ g_master_key[i % 32];
    }
    if (out_len) *out_len = enc_len;
    return 0;
}

int model_verify_checksum(const uint8_t* data, size_t len, uint32_t expected) {
    uint32_t crc = 0xFFFFFFFF;
    for (size_t i = 0; i < len; i++) {
        crc ^= data[i];
        for (int j = 0; j < 8; j++) crc = (crc >> 1) ^ (0xEDB88320 & (-(crc & 1)));
    }
    return ((crc ^ 0xFFFFFFFF) == expected) ? 0 : -1;
}

void model_decrypt_cleanup() {
    memset(g_master_key, 0, sizeof(g_master_key));
    g_initialized = false;
}
