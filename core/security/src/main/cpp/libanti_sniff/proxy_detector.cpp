#include <jni.h>
#include <android/log.h>
#include <fstream>
#include <string>

#define TAG "ProxyDetector"

extern "C" JNIEXPORT jboolean JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeCheckProxy(JNIEnv *env, jobject thiz) {
    std::ifstream props("/proc/net/tcp");
    std::string line;
    while (std::getline(props, line)) {
        if (line.find("1F90") != std::string::npos) return JNI_TRUE;
    }
    return JNI_FALSE;
}
