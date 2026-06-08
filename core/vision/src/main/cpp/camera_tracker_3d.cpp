#include <jni.h>
#include <android/log.h>
#include <vector>
#include <cmath>
#include <algorithm>

// OpenCV headers for feature tracking
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/video/tracking.hpp>
#include <opencv2/calib3d.hpp>

#define TAG "CameraTracker3D"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

// Camera intrinsic parameters (default for typical phone camera)
static cv::Mat K;  // Camera matrix
static bool initialized = false;

// State
static cv::Mat prevGray;
static std::vector<cv::Point2f> prevPoints;
static cv::Mat R_curr;  // Current rotation
static cv::Mat t_curr;  // Current translation

extern "C" JNIEXPORT jlong JNICALL
Java_com_myvideo_editor_core_vision_CameraTracker3D_nativeInit(JNIEnv *env, jobject thiz,
    jfloat fx, jfloat fy, jfloat cx, jfloat cy) {
    // Initialize camera matrix
    K = (cv::Mat_<double>(3, 3) << fx, 0, cx, 0, fy, cy, 0, 0, 1);
    R_curr = cv::Mat::eye(3, 3, CV_64F);
    t_curr = cv::Mat::zeros(3, 1, CV_64F);
    initialized = true;
    LOGD("CameraTracker3D initialized: fx=%.1f fy=%.1f cx=%.1f cy=%.1f", fx, fy, cx, cy);
    return reinterpret_cast<jlong>(1);
}

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_myvideo_editor_core_vision_CameraTracker3D_nativeTrackFrame(JNIEnv *env, jobject thiz,
    jlong handle, jintArray pixels, jint w, jint h) {
    if (!initialized) {
        LOGE("CameraTracker3D not initialized");
        return nullptr;
    }

    // Convert to grayscale
    jint* data = env->GetIntArrayElements(pixels, nullptr);
    cv::Mat rgba(h, w, CV_8UC4, data);
    cv::Mat gray;
    cv::cvtColor(rgba, gray, cv::COLOR_RGBA2GRAY);
    env->ReleaseIntArrayElements(pixels, data, JNI_ABORT);

    if (prevGray.empty()) {
        // First frame: detect features
        cv::goodFeaturesToTrack(gray, prevPoints, 500, 0.01, 10);
        prevGray = gray.clone();
        // Return identity pose [R00,R01,R02,R10,R11,R12,R20,R21,R22,tx,ty,tz]
        float identity[12] = {1,0,0, 0,1,0, 0,0,1, 0,0,0};
        jfloatArray result = env->NewFloatArray(12);
        env->SetFloatArrayRegion(result, 0, 12, identity);
        return result;
    }

    // Track features using Lucas-Kanade
    std::vector<cv::Point2f> currPoints;
    std::vector<uchar> status;
    std::vector<float> err;
    cv::calcOpticalFlowPyrLK(prevGray, gray, prevPoints, currPoints, status, err);

    // Filter good matches
    std::vector<cv::Point2f> prevGood, currGood;
    for (size_t i = 0; i < status.size(); i++) {
        if (status[i]) {
            prevGood.push_back(prevPoints[i]);
            currGood.push_back(currPoints[i]);
        }
    }

    float pose[12] = {1,0,0, 0,1,0, 0,0,1, 0,0,0};

    if (prevGood.size() >= 8) {
        // Find essential matrix and recover pose
        cv::Mat mask;
        cv::Mat E = cv::findEssentialMatrix(prevGood, currGood, K, cv::RANSAC, 0.999, 1.0, mask);

        if (!E.empty()) {
            cv::Mat R, t;
            int inliers = cv::recoverPose(E, prevGood, currGood, K, R, t, mask);

            if (inliers >= 8) {
                // Accumulate pose
                R_curr = R * R_curr;
                t_curr = R * t_curr + t;

                // Copy pose data
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        pose[i*3+j] = (float)R_curr.at<double>(i, j);
                    }
                    pose[9+i] = (float)t_curr.at<double>(i, 0);
                }
            }
        }
    }

    // Update state for next frame
    // Re-detect features if too few tracked
    if (currGood.size() < 50) {
        cv::goodFeaturesToTrack(gray, prevPoints, 500, 0.01, 10);
    } else {
        prevPoints = currGood;
    }
    prevGray = gray.clone();

    jfloatArray result = env->NewFloatArray(12);
    env->SetFloatArrayRegion(result, 0, 12, pose);
    return result;
}

extern "C" JNIEXPORT void JNICALL
Java_com_myvideo_editor_core_vision_CameraTracker3D_nativeReset(JNIEnv *env, jobject thiz, jlong handle) {
    prevGray.release();
    prevPoints.clear();
    R_curr = cv::Mat::eye(3, 3, CV_64F);
    t_curr = cv::Mat::zeros(3, 1, CV_64F);
    LOGD("CameraTracker3D reset");
}

extern "C" JNIEXPORT void JNICALL
Java_com_myvideo_editor_core_vision_CameraTracker3D_nativeRelease(JNIEnv *env, jobject thiz, jlong handle) {
    prevGray.release();
    prevPoints.clear();
    initialized = false;
    LOGD("CameraTracker3D released");
}
