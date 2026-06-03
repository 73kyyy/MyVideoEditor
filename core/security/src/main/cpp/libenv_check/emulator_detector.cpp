#include <jni.h>
#include <android/log.h>
#include <fstream>
#include <string>
#include <sys/system_properties.h>

#define TAG "EmulatorDetector"

static bool checkProperty(const char* prop, const char* value) {
    char buf[PROP_VALUE_MAX];
    __system_property_get(prop, buf);
    return strstr(buf, value) != nullptr;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeCheckEmulator(JNIEnv *env, jobject thiz) {
    if (checkProperty("ro.hardware", "goldfish") || checkProperty("ro.hardware", "ranchu")) return JNI_TRUE;
    if (checkProperty("ro.product.model", "sdk") || checkProperty("ro.product.model", "google_sdk")) return JNI_TRUE;
    if (checkProperty("ro.build.fingerprint", "generic") || checkProperty("ro.build.fingerprint", "sdk")) return JNI_TRUE;
    if (checkProperty("ro.product.brand", "generic") || checkProperty("ro.product.device", "generic")) return JNI_TRUE;
    std::ifstream cpuinfo("/proc/cpuinfo");
    std::string line;
    while (std::getline(cpuinfo, line)) {
        if (line.find("goldfish") != std::string::npos || line.find("ranchu") != std::string::npos) return JNI_TRUE;
    }
    return JNI_FALSE;
}
