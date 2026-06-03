#include <jni.h>
#include <android/log.h>
#include <sys/mman.h>
#include <cstdio>
#include <cstring>

#define TAG "AntiDump"

extern "C" JNIEXPORT jboolean JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeAntiDump(JNIEnv *env, jobject thiz) {
    FILE* maps = fopen("/proc/self/maps", "r");
    if (!maps) return JNI_FALSE;
    char line[512];
    while (fgets(line, sizeof(line), maps)) {
        unsigned long start, end;
        char perms[5];
        if (sscanf(line, "%lx-%lx %4s", &start, &end, perms) == 3) {
            if (strstr(perms, "x") && end - start > 4096) {
                mprotect((void*)start, end - start, PROT_READ);
            }
        }
    }
    fclose(maps);
    return JNI_TRUE;
}
