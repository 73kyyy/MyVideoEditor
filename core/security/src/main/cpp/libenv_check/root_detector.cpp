#include <jni.h>
#include <android/log.h>
#include <fstream>
#include <string>
#include <sys/stat.h>

#define TAG "RootDetector"

static bool fileExists(const char* path) {
    struct stat st;
    return stat(path, &st) == 0;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeCheckRoot(JNIEnv *env, jobject thiz) {
    const char* paths[] = {"/system/bin/su", "/system/xbin/su", "/sbin/su", "/system/app/Superuser.apk",
        "/system/app/SuperSU.apk", "/data/local/xbin/su", "/data/local/bin/su"};
    for (auto p : paths) { if (fileExists(p)) return JNI_TRUE; }

    std::ifstream props("/proc/self/maps");
    std::string line;
    while (std::getline(props, line)) {
        if (line.find("magisk") != std::string::npos) return JNI_TRUE;
    }
    return JNI_FALSE;
}
