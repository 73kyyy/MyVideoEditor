#include <jni.h>
#include <android/log.h>
#include <fstream>
#include <string>

#define TAG "XposedDetector"

static bool checkXposedMaps() {
    std::ifstream maps("/proc/self/maps");
    std::string line;
    while (std::getline(maps, line)) {
        if (line.find("xposed") != std::string::npos || line.find("lsposed") != std::string::npos)
            return true;
    }
    return false;
}

static bool checkXposedClass() {
    std::ifstream maps("/proc/self/maps");
    std::string line;
    while (std::getline(maps, line)) {
        if (line.find("XposedBridge") != std::string::npos)
            return true;
    }
    return false;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeCheckXposed(JNIEnv *env, jobject thiz) {
    return (checkXposedMaps() || checkXposedClass()) ? JNI_TRUE : JNI_FALSE;
}
