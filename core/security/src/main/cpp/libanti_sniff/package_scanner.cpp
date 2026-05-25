#include <jni.h>
#include <android/log.h>
#include <dirent.h>
#include <string>

#define TAG "PackageScanner"

static bool scanForPackage(const char* packageName) {
    DIR* dir = opendir("/data/data");
    if (!dir) return false;
    struct dirent* entry;
    while ((entry = readdir(dir)) != nullptr) {
        if (entry->d_type == DT_DIR && strstr(entry->d_name, packageName)) {
            closedir(dir);
            return true;
        }
    }
    closedir(dir);
    return false;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeScanPackages(JNIEnv *env, jobject thiz) {
    const char* suspicious[] = {"frida", "xposed", "substrate", "magisk"};
    for (auto pkg : suspicious) {
        if (scanForPackage(pkg)) return JNI_TRUE;
    }
    return JNI_FALSE;
}
