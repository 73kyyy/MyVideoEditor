#include <jni.h>
#include <android/log.h>
#include <vector>

#define TAG "ESRGANInference"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

extern "C" JNIEXPORT jlong JNICALL
Java_com_myvideo_editor_core_ai_enhancement_ESRGANWrapper_nativeInit(JNIEnv *env, jobject thiz) {
    LOGD("ESRGAN推理初始化");
    return 1;
}

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_myvideo_editor_core_ai_enhancement_ESRGANWrapper_nativeUpscale(JNIEnv *env, jobject thiz,
    jlong handle, jfloatArray input, jint inW, jint inH, jint scale) {
    int outW = inW * scale; int outH = inH * scale;
    int inSize = inW * inH; int outSize = outW * outH;
    float* inData = env->GetFloatArrayElements(input, nullptr);
    jfloatArray result = env->NewFloatArray(outSize * 3);
    std::vector<float> output(outSize * 3);
    for (int c = 0; c < 3; c++) for (int y = 0; y < outH; y++) for (int x = 0; x < outW; x++) {
        int srcX = x / scale; int srcY = y / scale;
        output[c * outSize + y * outW + x] = inData[c * inSize + srcY * inW + srcX];
    }
    env->ReleaseFloatArrayElements(input, inData, JNI_ABORT);
    env->SetFloatArrayRegion(result, 0, outSize * 3, output.data());
    return result;
}

extern "C" JNIEXPORT void JNICALL
Java_com_myvideo_editor_core_ai_enhancement_ESRGANWrapper_nativeRelease(JNIEnv *env, jobject thiz, jlong handle) {
    LOGD("ESRGAN推理释放");
}
