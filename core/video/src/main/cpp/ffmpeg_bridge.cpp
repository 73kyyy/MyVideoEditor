#include <jni.h>
#include <string>
#include <android/log.h>

#define TAG "FFmpegBridge"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

extern "C" {
    #include <libavcodec/avcodec.h>
    #include <libavformat/avformat.h>
    #include <libavutil/avutil.h>
    #include <libswscale/swscale.h>
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_myvideo_editor_core_video_FFmpegBridge_getVersion(JNIEnv *env, jobject thiz) {
    return env->NewStringUTF(av_version_info());
}

extern "C" JNIEXPORT jint JNICALL
Java_com_myvideo_editor_core_video_FFmpegBridge_decodeFrame(JNIEnv *env, jobject thiz,
                                                           jstring inputPath, jint frameIndex) {
    const char *path = env->GetStringUTFChars(inputPath, nullptr);

    AVFormatContext *formatCtx = nullptr;
    if (avformat_open_input(&formatCtx, path, nullptr, nullptr) != 0) {
        LOGE("无法打开输入文件: %s", path);
        env->ReleaseStringUTFChars(inputPath, path);
        return -1;
    }

    if (avformat_find_stream_info(formatCtx, nullptr) < 0) {
        LOGE("无法获取流信息");
        avformat_close_input(&formatCtx);
        env->ReleaseStringUTFChars(inputPath, path);
        return -1;
    }

    avformat_close_input(&formatCtx);
    env->ReleaseStringUTFChars(inputPath, path);
    return 0;
}

extern "C" JNIEXPORT void JNICALL
Java_com_myvideo_editor_core_video_FFmpegBridge_init(JNIEnv *env, jobject thiz) {
    av_register_all();
    avformat_network_init();
    LOGD("FFmpeg初始化完成: %s", av_version_info());
}
