#include <jni.h>
#include <android/log.h>
#include <fstream>
#include <string>

#define TAG "TextSegHash"

static uint32_t hashSegment(const unsigned char* data, size_t len) {
    uint32_t hash = 5381;
    for (size_t i = 0; i < len; i++) {
        hash = ((hash << 5) + hash) + data[i];
    }
    return hash;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeHashTextSegment(JNIEnv *env, jobject thiz) {
    std::ifstream maps("/proc/self/maps");
    std::string line;
    while (std::getline(maps, line)) {
        if (line.find("r-xp") != std::string::npos && line.find(".so") != std::string::npos) {
            unsigned long start, end;
            if (sscanf(line.c_str(), "%lx-%lx", &start, &end) == 2) {
                size_t len = end - start;
                if (len > 0 && len < 10 * 1024 * 1024) {
                    return (jint)hashSegment((const unsigned char*)start, len > 4096 ? 4096 : len);
                }
            }
        }
    }
    return 0;
}
