#include <jni.h>
#include <android/log.h>
#include <sys/ptrace.h>

#define TAG "AntiDebug"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

extern "C" JNIEXPORT jboolean JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeCheckDebugger(JNIEnv *env, jobject thiz) {
    if (ptrace(PTRACE_TRACEME, 0, 0, 0) < 0) return JNI_TRUE;
    ptrace(PTRACE_DETACH, 0, 0, 0);
    return JNI_FALSE;
}
