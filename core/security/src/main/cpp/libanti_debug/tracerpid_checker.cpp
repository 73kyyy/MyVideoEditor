#include <jni.h>
#include <android/log.h>
#include <fstream>
#include <string>

#define TAG "TracerPid"

extern "C" JNIEXPORT jboolean JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeCheckTracerPid(JNIEnv *env, jobject thiz) {
    std::ifstream status("/proc/self/status");
    std::string line;
    while (std::getline(status, line)) {
        if (line.find("TracerPid:") == 0) {
            int pid = std::stoi(line.substr(10));
            return pid > 0 ? JNI_TRUE : JNI_FALSE;
        }
    }
    return JNI_FALSE;
}
