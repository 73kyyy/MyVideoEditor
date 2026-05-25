#include <jni.h>
#include <android/log.h>
#include <time.h>

#define TAG "TimingDetector"

static long long getTimeNs() {
    struct timespec ts;
    clock_gettime(CLOCK_MONOTONIC, &ts);
    return ts.tv_sec * 1000000000LL + ts.tv_nsec;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeCheckTiming(JNIEnv *env, jobject thiz) {
    long long start = getTimeNs();
    volatile int x = 0;
    for (int i = 0; i < 10000; i++) x += i;
    long long elapsed = getTimeNs() - start;
    return elapsed > 10000000 ? JNI_TRUE : JNI_FALSE;
}
