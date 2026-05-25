#include <jni.h>
#include <android/log.h>
#include <fstream>

#define TAG "DexIntegrity"

extern "C" JNIEXPORT jboolean JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeCheckDexIntegrity(JNIEnv *env, jobject thiz) {
    std::ifstream dex("/proc/self/maps");
    std::string line;
    int dexCount = 0;
    while (std::getline(dex, line)) {
        if (line.find(".dex") != std::string::npos || line.find(".apk") != std::string::npos) dexCount++;
    }
    return dexCount > 5 ? JNI_TRUE : JNI_FALSE;
}
