#include <jni.h>
#include <android/log.h>
#include <signal.h>
#include <setjmp.h>

#define TAG "SignalHandler"

static jmp_buf jumpBuffer;
static volatile int sigReceived = 0;

static void signalHandler(int sig) {
    sigReceived = 1;
    longjmp(jumpBuffer, 1);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeCheckSignal(JNIEnv *env, jobject thiz) {
    struct sigaction sa, oldSa;
    sa.sa_handler = signalHandler;
    sigemptyset(&sa.sa_mask);
    sa.sa_flags = 0;
    sigaction(SIGTRAP, &sa, &oldSa);
    sigReceived = 0;
    if (setjmp(jumpBuffer) == 0) {
        raise(SIGTRAP);
        sigaction(SIGTRAP, &oldSa, nullptr);
        return sigReceived ? JNI_TRUE : JNI_FALSE;
    }
    sigaction(SIGTRAP, &oldSa, nullptr);
    return JNI_TRUE;
}
