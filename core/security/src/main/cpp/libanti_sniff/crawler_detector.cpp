#include <jni.h>
#include <android/log.h>

#define TAG "CrawlerDetector"

extern "C" JNIEXPORT jboolean JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativeCheckCrawler(JNIEnv *env, jobject thiz) {
    return JNI_FALSE;
}
