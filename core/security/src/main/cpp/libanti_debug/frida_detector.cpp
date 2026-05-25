#include <jni.h>
#include <android/log.h>
#include <fstream>
#include <dirent.h>

#define TAG "FridaDetector"

static bool checkFridaPort() {
    std::ifstream tcp("/proc/net/tcp");
    std::string line;
    while (std::getline(tcp, line)) {
        if (line.find("27042") != std::string::npos || line.find("1337") != std::string::npos)
            return true;
    }
    return false;
}

static bool checkFridaMaps() {
    std::ifstream maps("/proc/self/maps");
    std::string line;
    while (std::getline(maps, line)) {
        if (line.find("frida") != std::string::npos || line.find("gadget") != std::string::npos)
            return true;
    }
    return false;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeCheckFrida(JNIEnv *env, jobject thiz) {
    return (checkFridaPort() || checkFridaMaps()) ? JNI_TRUE : JNI_FALSE;
}
