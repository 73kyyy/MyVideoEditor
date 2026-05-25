#include <jni.h>
#include <android/log.h>

#define TAG "OpenCVBridge"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

extern "C" JNIEXPORT jboolean JNICALL
Java_com_myvideo_editor_core_vision_OpenCVManager_nativeInit(JNIEnv *env, jobject thiz) {
    LOGD("OpenCV初始化");
    return JNI_TRUE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_myvideo_editor_core_vision_OpenCVManager_nativeRelease(JNIEnv *env, jobject thiz) {
    LOGD("OpenCV释放");
}

extern "C" JNIEXPORT jintArray JNICALL
Java_com_myvideo_editor_core_vision_ImageProcessor_nativeCanny(JNIEnv *env, jobject thiz,
    jintArray pixels, jint w, jint h, jfloat t1, jfloat t2) {
    int* data = env->GetIntArrayElements(pixels, nullptr);
    int size = w * h;
    jintArray result = env->NewIntArray(size);
    int* out = new int[size];
    for (int y = 1; y < h - 1; y++) for (int x = 1; x < w - 1; x++) {
        int gx = ((data[y*w+x+1] & 0xFF) - (data[y*w+x-1] & 0xFF));
        int gy = (data[(y+1)*w+x] & 0xFF) - (data[(y-1)*w+x] & 0xFF);
        int mag = abs(gx) + abs(gy);
        int v = mag > (int)t1 ? 255 : 0;
        out[y*w+x] = 0xFF000000 | (v << 16) | (v << 8) | v;
    }
    env->SetIntArrayRegion(result, 0, size, out);
    env->ReleaseIntArrayElements(pixels, data, JNI_ABORT);
    delete[] out;
    return result;
}

extern "C" JNIEXPORT jintArray JNICALL
Java_com_myvideo_editor_core_vision_ImageProcessor_nativeBlur(JNIEnv *env, jobject thiz,
    jintArray pixels, jint w, jint h, jint radius) {
    int* data = env->GetIntArrayElements(pixels, nullptr);
    int size = w * h;
    int* out = new int[size];
    for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
        int r = 0, g = 0, b = 0, count = 0;
        for (int dy = -radius; dy <= radius; dy++) for (int dx = -radius; dx <= radius; dx++) {
            int ny = y + dy, nx = x + dx;
            if (ny >= 0 && ny < h && nx >= 0 && nx < w) {
                int p = data[ny * w + nx];
                r += (p >> 16) & 0xFF; g += (p >> 8) & 0xFF; b += p & 0xFF; count++;
            }
        }
        out[y*w+x] = 0xFF000000 | ((r/count) << 16) | ((g/count) << 8) | (b/count);
    }
    jintArray result = env->NewIntArray(size);
    env->SetIntArrayRegion(result, 0, size, out);
    env->ReleaseIntArrayElements(pixels, data, JNI_ABORT);
    delete[] out;
    return result;
}

extern "C" JNIEXPORT jintArray JNICALL
Java_com_myvideo_editor_core_vision_ImageProcessor_nativeThreshold(JNIEnv *env, jobject thiz,
    jintArray pixels, jint w, jint h, jfloat threshold) {
    int* data = env->GetIntArrayElements(pixels, nullptr);
    int size = w * h;
    int* out = new int[size];
    int t = (int)(threshold * 255);
    for (int i = 0; i < size; i++) {
        int gray = ((data[i] >> 16) & 0xFF + (data[i] >> 8) & 0xFF + (data[i] & 0xFF)) / 3;
        int v = gray > t ? 255 : 0;
        out[i] = 0xFF000000 | (v << 16) | (v << 8) | v;
    }
    jintArray result = env->NewIntArray(size);
    env->SetIntArrayRegion(result, 0, size, out);
    env->ReleaseIntArrayElements(pixels, data, JNI_ABORT);
    delete[] out;
    return result;
}
