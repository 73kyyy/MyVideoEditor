#include <jni.h>
#include <android/log.h>
#include <vector>
#include <cstring>

// OpenCV headers
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/features2d.hpp>
#include <opencv2/video/tracking.hpp>

#define TAG "OpenCVBridge"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

static bool g_opencv_initialized = false;

extern "C" JNIEXPORT jboolean JNICALL
Java_com_myvideo_editor_core_vision_OpenCVManager_nativeInit(JNIEnv *env, jobject thiz) {
    if (g_opencv_initialized) return JNI_TRUE;
    // OpenCV static init is handled by the Android SDK package
    g_opencv_initialized = true;
    LOGD("OpenCV initialized: %s", cv::getBuildInformation().c_str());
    return JNI_TRUE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_myvideo_editor_core_vision_OpenCVManager_nativeRelease(JNIEnv *env, jobject thiz) {
    g_opencv_initialized = false;
    LOGD("OpenCV released");
}

// Convert Android int[] pixels (ARGB) to OpenCV Mat (BGR)
static cv::Mat pixelsToMat(JNIEnv *env, jintArray pixels, int w, int h) {
    jint* data = env->GetIntArrayElements(pixels, nullptr);
    cv::Mat rgba(h, w, CV_8UC4, data);
    cv::Mat bgr;
    cv::cvtColor(rgba, bgr, cv::COLOR_RGBA2BGR);
    env->ReleaseIntArrayElements(pixels, data, JNI_ABORT);
    return bgr;
}

// Convert OpenCV Mat (BGR) to Android int[] pixels (ARGB)
static jintArray matToPixels(JNIEnv *env, const cv::Mat& bgr) {
    cv::Mat rgba;
    cv::cvtColor(bgr, rgba, cv::COLOR_BGR2RGBA);
    int size = rgba.rows * rgba.cols;
    jintArray result = env->NewIntArray(size);
    env->SetIntArrayRegion(result, 0, size, (jint*)rgba.data);
    return result;
}

extern "C" JNIEXPORT jintArray JNICALL
Java_com_myvideo_editor_core_vision_ImageProcessor_nativeCanny(JNIEnv *env, jobject thiz,
    jintArray pixels, jint w, jint h, jfloat t1, jfloat t2) {
    cv::Mat bgr = pixelsToMat(env, pixels, w, h);
    cv::Mat gray, edges;
    cv::cvtColor(bgr, gray, cv::COLOR_BGR2GRAY);
    cv::Canny(gray, edges, t1, t2);
    cv::Mat bgrEdges;
    cv::cvtColor(edges, bgrEdges, cv::COLOR_GRAY2BGR);
    return matToPixels(env, bgrEdges);
}

extern "C" JNIEXPORT jintArray JNICALL
Java_com_myvideo_editor_core_vision_ImageProcessor_nativeBlur(JNIEnv *env, jobject thiz,
    jintArray pixels, jint w, jint h, jint radius) {
    cv::Mat bgr = pixelsToMat(env, pixels, w, h);
    cv::Mat blurred;
    cv::GaussianBlur(bgr, blurred, cv::Size(2*radius+1, 2*radius+1), 0);
    return matToPixels(env, blurred);
}

extern "C" JNIEXPORT jintArray JNICALL
Java_com_myvideo_editor_core_vision_ImageProcessor_nativeThreshold(JNIEnv *env, jobject thiz,
    jintArray pixels, jint w, jint h, jfloat threshold) {
    cv::Mat bgr = pixelsToMat(env, pixels, w, h);
    cv::Mat gray, binary;
    cv::cvtColor(bgr, gray, cv::COLOR_BGR2GRAY);
    cv::threshold(gray, binary, threshold * 255, 255, cv::THRESH_BINARY);
    cv::Mat bgrResult;
    cv::cvtColor(binary, bgrResult, cv::COLOR_GRAY2BGR);
    return matToPixels(env, bgrResult);
}

// Additional OpenCV functions

extern "C" JNIEXPORT jintArray JNICALL
Java_com_myvideo_editor_core_vision_ImageProcessor_nativeAdaptiveThreshold(JNIEnv *env, jobject thiz,
    jintArray pixels, jint w, jint h, jdouble maxValue, jint blockSize, jdouble c) {
    cv::Mat bgr = pixelsToMat(env, pixels, w, h);
    cv::Mat gray, binary;
    cv::cvtColor(bgr, gray, cv::COLOR_BGR2GRAY);
    cv::adaptiveThreshold(gray, binary, maxValue, cv::ADAPTIVE_THRESH_GAUSSIAN_C, cv::THRESH_BINARY, blockSize, c);
    cv::Mat bgrResult;
    cv::cvtColor(binary, bgrResult, cv::COLOR_GRAY2BGR);
    return matToPixels(env, bgrResult);
}

extern "C" JNIEXPORT jintArray JNICALL
Java_com_myvideo_editor_core_vision_ImageProcessor_nativeResize(JNIEnv *env, jobject thiz,
    jintArray pixels, jint w, jint h, jint newW, jint newH) {
    cv::Mat bgr = pixelsToMat(env, pixels, w, h);
    cv::Mat resized;
    cv::resize(bgr, resized, cv::Size(newW, newH), 0, 0, cv::INTER_LINEAR);
    return matToPixels(env, resized);
}
