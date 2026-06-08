#include <jni.h>
#include <android/log.h>
#include <vector>

#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/video/tracking.hpp>

#define TAG "PlaneTracker"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

static cv::Mat prevGray;
static std::vector<cv::Point2f> prevPoints;

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_myvideo_editor_core_vision_PlaneTracker_nativeTrack(JNIEnv *env, jobject thiz,
    jintArray prevPixels, jintArray currPixels, jint w, jint h) {
    // Convert both frames to grayscale OpenCV Mats
    jint* prevData = env->GetIntArrayElements(prevPixels, nullptr);
    jint* currData = env->GetIntArrayElements(currPixels, nullptr);

    cv::Mat prevRGBA(h, w, CV_8UC4, prevData);
    cv::Mat currRGBA(h, w, CV_8UC4, currData);

    cv::Mat prevG, currG;
    cv::cvtColor(prevRGBA, prevG, cv::COLOR_RGBA2GRAY);
    cv::cvtColor(currRGBA, currG, cv::COLOR_RGBA2GRAY);

    env->ReleaseIntArrayElements(prevPixels, prevData, JNI_ABORT);
    env->ReleaseIntArrayElements(currPixels, currData, JNI_ABORT);

    // Detect good features to track if no previous points
    if (prevPoints.empty() || prevGray.empty()) {
        cv::goodFeaturesToTrack(prevG, prevPoints, 200, 0.01, 10);
        prevGray = prevG.clone();
    }

    // Lucas-Kanade optical flow
    std::vector<cv::Point2f> currPoints;
    std::vector<uchar> status;
    std::vector<float> err;
    cv::calcOpticalFlowPyrLK(prevGray, currG, prevPoints, currPoints, status, err);

    // Collect tracked points
    std::vector<float> result;
    for (size_t i = 0; i < currPoints.size(); i++) {
        if (status[i]) {
            result.push_back(currPoints[i].x);
            result.push_back(currPoints[i].y);
        }
    }

    // Update state
    prevGray = currG.clone();
    prevPoints = currPoints;

    jfloatArray out = env->NewFloatArray(result.size());
    if (result.size() > 0) {
        env->SetFloatArrayRegion(out, 0, result.size(), result.data());
    }
    return out;
}
