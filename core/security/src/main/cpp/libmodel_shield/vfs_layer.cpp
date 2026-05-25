#include <jni.h>
#include <android/log.h>
#include <string.h>
#include <stdlib.h>

#define TAG "VFSLayer"

struct VFile {
    char name[256];
    unsigned char* data;
    int size;
};

static VFile vfsFiles[32];
static int vfsCount = 0;

extern "C" JNIEXPORT jboolean JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeVfsRegister(JNIEnv *env, jobject thiz,
    jstring name, jbyteArray data) {
    if (vfsCount >= 32) return JNI_FALSE;
    const char* n = env->GetStringUTFChars(name, nullptr);
    strncpy(vfsFiles[vfsCount].name, n, 255);
    int len = env->GetArrayLength(data);
    vfsFiles[vfsCount].data = (unsigned char*)malloc(len);
    vfsFiles[vfsCount].size = len;
    env->GetByteArrayRegion(data, 0, len, (jbyte*)vfsFiles[vfsCount].data);
    env->ReleaseStringUTFChars(name, n);
    vfsCount++;
    return JNI_TRUE;
}
