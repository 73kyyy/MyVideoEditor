#include <jni.h>
#include <android/log.h>
#include <stdlib.h>

#define TAG "OutputPerturb"

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_myvideo_editor_core_security_SecurityChecker_nativePerturbOutput(JNIEnv *env, jobject thiz,
    jfloatArray output, jfloat epsilon) {
    int len = env->GetArrayLength(output);
    float* data = env->GetFloatArrayElements(output, nullptr);
    float* perturbed = new float[len];
    for (int i = 0; i < len; i++) {
        float noise = ((rand() % 1000) / 1000.0f - 0.5f) * 2.0f * epsilon;
        perturbed[i] = data[i] + noise;
    }
    env->ReleaseFloatArrayElements(output, data, JNI_ABORT);
    jfloatArray result = env->NewFloatArray(len);
    env->SetFloatArrayRegion(result, 0, len, perturbed);
    delete[] perturbed;
    return result;
}
