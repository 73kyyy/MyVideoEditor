#include <jni.h>
#include <android/log.h>
#include <fstream>
#include <string>

#define TAG "VPNDetector"

extern "C" JNIEXPORT jboolean JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeCheckVPN(JNIEnv *env, jobject thiz) {
    std::ifstream routes("/proc/net/route");
    std::string line;
    while (std::getline(routes, line)) {
        if (line.find("tun0") != std::string::npos || line.find("ppp0") != std::string::npos)
            return JNI_TRUE;
    }
    return JNI_FALSE;
}
