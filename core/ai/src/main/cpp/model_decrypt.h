#ifndef MODEL_DECRYPT_H
#define MODEL_DECRYPT_H

#include <cstdint>
#include <cstddef>

struct ModelHeader {
    uint32_t magic;
    uint32_t version;
    uint32_t dataSize;
    uint32_t checksum;
    uint8_t iv[16];
    uint8_t keyId[32];
};

int model_decrypt_init(const uint8_t* master_key, size_t key_len);
int model_decrypt_chunk(const uint8_t* encrypted, size_t enc_len, uint8_t* output, size_t* out_len);
int model_verify_checksum(const uint8_t* data, size_t len, uint32_t expected);
void model_decrypt_cleanup();

#endif
