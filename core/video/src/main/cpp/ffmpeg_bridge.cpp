#include <jni.h>
#include <string>
#include <android/log.h>

#define TAG "FFmpegBridge"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

#ifdef HAS_FFMPEG
extern "C" {
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavutil/avutil.h>
#include <libswscale/swscale.h>
}
#endif

extern "C" JNIEXPORT jstring JNICALL
Java_com_myvideo_editor_core_video_FFmpegBridge_getVersion(JNIEnv *env, jobject thiz) {
#ifdef HAS_FFMPEG
    return env->NewStringUTF(av_version_info());
#else
    return env->NewStringUTF("ffmpeg-unavailable");
#endif
}

extern "C" JNIEXPORT jint JNICALL
Java_com_myvideo_editor_core_video_FFmpegBridge_decodeFrame(JNIEnv *env, jobject thiz,
                                                           jstring inputPath, jint frameIndex) {
#ifndef HAS_FFMPEG
    LOGE("FFmpeg not available");
    return -1;
#else
    const char *path = env->GetStringUTFChars(inputPath, nullptr);
    AVFormatContext *formatCtx = nullptr;
    if (avformat_open_input(&formatCtx, path, nullptr, nullptr) != 0) {
        LOGE("Cannot open: %s", path);
        env->ReleaseStringUTFChars(inputPath, path);
        return -1;
    }
    if (avformat_find_stream_info(formatCtx, nullptr) < 0) {
        LOGE("Cannot get stream info");
        avformat_close_input(&formatCtx);
        env->ReleaseStringUTFChars(inputPath, path);
        return -1;
    }
    avformat_close_input(&formatCtx);
    env->ReleaseStringUTFChars(inputPath, path);
    return 0;
#endif
}

extern "C" JNIEXPORT void JNICALL
Java_com_myvideo_editor_core_video_FFmpegBridge_init(JNIEnv *env, jobject thiz) {
#ifdef HAS_FFMPEG
    avformat_network_init();
    LOGD("FFmpeg init: %s", av_version_info());
#else
    LOGD("FFmpeg not available, skip init");
#endif
}
