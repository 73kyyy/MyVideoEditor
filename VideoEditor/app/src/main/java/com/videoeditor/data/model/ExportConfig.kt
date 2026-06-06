package com.videoeditor.data.model

data class ExportConfig(
    val width: Int = 1080,
    val height: Int = 1920,
    val frameRate: Int = 30,
    val videoBitrate: Int = 8_000_000, // 8 Mbps
    val audioBitrate: Int = 128_000,   // 128 Kbps
    val audioSampleRate: Int = 44100,
    val audioChannels: Int = 2,
    val format: ExportFormat = ExportFormat.MP4,
    val codec: VideoCodec = VideoCodec.H264
)

enum class ExportFormat(val extension: String) {
    MP4("mp4"),
    MOV("mov"),
    WEBM("webm")
}

enum class VideoCodec(val ffmpegName: String) {
    H264("libx264"),
    H265("libx265"),
    VP9("libvpx-vp9")
}
