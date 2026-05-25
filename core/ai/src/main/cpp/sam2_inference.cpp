#include <jni.h>
#include <android/log.h>
#include <vector>

#define TAG "SAM2Inference"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

extern "C" JNIEXPORT jlong JNICALL
Java_com_myvideo_editor_core_ai_segmentation_SAM2Wrapper_nativeInit(JNIEnv *env, jobject thiz) {
    LOGD("SAM2推理初始化");
    return 1;
}

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_myvideo_editor_core_ai_segmentation_SAM2Wrapper_nativeSegment(JNIEnv *env, jobject thiz,
    jlong handle, jfloatArray input, jint width, jint height, jfloat pointX, jfloat pointY) {
    int size = width * height;
    jfloatArray result = env->NewFloatArray(size);
    std::vector<float> mask(size, 0.0f);
    float cx = pointX / width; float cy = pointY / height;
    for (int y = 0; y < height; y++) for (int x = 0; x < width; x++) {
        float dx = (float)x / width - cx; float dy = (float)y / height - cy;
        float dist = sqrtf(dx * dx + dy * dy);
        mask[y * width + x] = dist < 0.3f ? 1.0f : 0.0f;
    }
    env->SetFloatArrayRegion(result, 0, size, mask.data());
    return result;
}

extern "C" JNIEXPORT void JNICALL
Java_com_myvideo_editor_core_ai_segmentation_SAM2Wrapper_nativeRelease(JNIEnv *env, jobject thiz, jlong handle) {
    LOGD("SAM2推理释放");
}
