#include <jni.h>
#include <android/log.h>
#include <vector>

#define TAG "PlaneTracker"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_myvideo_editor_core_vision_PlaneTracker_nativeTrack(JNIEnv *env, jobject thiz,
    jintArray prevPixels, jintArray currPixels, jint w, jint h) {
    int* prev = env->GetIntArrayElements(prevPixels, nullptr);
    int* curr = env->GetIntArrayElements(currPixels, nullptr);
    std::vector<float> points;
    int blockSize = 16;
    for (int y = blockSize; y < h - blockSize; y += blockSize) {
        for (int x = blockSize; x < w - blockSize; x += blockSize) {
            int bestDx = 0, bestDy = 0, bestDiff = INT32_MAX;
            for (int dy = -8; dy <= 8; dy += 2) {
                for (int dx = -8; dx <= 8; dx += 2) {
                    int diff = 0;
                    for (int by = -2; by <= 2; by++) for (int bx = -2; bx <= 2; bx++) {
                        int py = y + by, px = x + bx;
                        int cy = py + dy, cx = px + dx;
                        if (cy >= 0 && cy < h && cx >= 0 && cx < w) {
                            int p = prev[py*w+px], c = curr[cy*w+cx];
                            diff += abs((p>>16&0xFF)-(c>>16&0xFF))+abs((p>>8&0xFF)-(c>>8&0xFF))+abs((p&0xFF)-(c&0xFF));
                        }
                    }
                    if (diff < bestDiff) { bestDiff = diff; bestDx = dx; bestDy = dy; }
                }
            }
            points.push_back(x + bestDx);
            points.push_back(y + bestDy);
        }
    }
    env->ReleaseIntArrayElements(prevPixels, prev, JNI_ABORT);
    env->ReleaseIntArrayElements(currPixels, curr, JNI_ABORT);
    jfloatArray result = env->NewFloatArray(points.size());
    env->SetFloatArrayRegion(result, 0, points.size(), points.data());
    return result;
}
