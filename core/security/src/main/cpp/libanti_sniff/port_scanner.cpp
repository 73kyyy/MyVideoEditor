#include <jni.h>
#include <android/log.h>
#include <fstream>
#include <string>

#define TAG "PortScanner"

extern "C" JNIEXPORT jboolean JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeCheckPorts(JNIEnv *env, jobject thiz) {
    std::ifstream tcp("/proc/net/tcp");
    std::string line;
    while (std::getline(tcp, line)) {
        if (line.find("1F90") != std::string::npos || line.find("1F91") != std::string::npos)
            return JNI_TRUE;
    }
    return JNI_FALSE;
}
