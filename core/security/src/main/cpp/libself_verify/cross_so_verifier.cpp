#include <jni.h>
#include <android/log.h>
#include <fstream>
#include <string>
#include <vector>

#define TAG "CrossSO"

extern "C" JNIEXPORT jboolean JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeVerifyCrossSO(JNIEnv *env, jobject thiz) {
    std::ifstream maps("/proc/self/maps");
    std::string line;
    std::vector<std::string> libraries;
    while (std::getline(maps, line)) {
        if (line.find(".so") != std::string::npos && line.find("r-xp") != std::string::npos) {
            size_t pos = line.rfind('/');
            if (pos != std::string::npos) {
                std::string name = line.substr(pos + 1);
                if (name.find("libmyvideo") != std::string::npos) {
                    libraries.push_back(name);
                }
            }
        }
    }
    return libraries.empty() ? JNI_FALSE : JNI_TRUE;
}
