#include <jni.h>
#include <android/log.h>
#include <fstream>

#define TAG "CrcChecker"

static uint32_t crc32(const unsigned char* data, size_t len) {
    uint32_t crc = 0xFFFFFFFF;
    for (size_t i = 0; i < len; i++) {
        crc ^= data[i];
        for (int j = 0; j < 8; j++) crc = (crc >> 1) ^ (0xEDB88320 & (-(crc & 1)));
    }
    return crc ^ 0xFFFFFFFF;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeCheckCrc(JNIEnv *env, jobject thiz,
    jstring filePath, jint expectedCrc) {
    const char* path = env->GetStringUTFChars(filePath, nullptr);
    std::ifstream file(path, std::ios::binary | std::ios::ate);
    if (!file.is_open()) { env->ReleaseStringUTFChars(filePath, path); return JNI_FALSE; }
    size_t size = file.tellg();
    file.seekg(0);
    unsigned char* buffer = new unsigned char[size];
    file.read((char*)buffer, size);
    uint32_t crc = crc32(buffer, size);
    delete[] buffer;
    env->ReleaseStringUTFChars(filePath, path);
    return (crc == (uint32_t)expectedCrc) ? JNI_TRUE : JNI_FALSE;
}
