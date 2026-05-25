package com.myvideo.editor.core.export.model

data class ExportConfig(
    val outputPath: String,
    val width: Int = 1920,
    val height: Int = 1080,
    val fps: Int = 30,
    val videoBitrate: Int = 16_000_000,
    val audioBitrate: Int = 128_000,
    val codec: String = "video/avc",
    val preset: String = "fast",
    val crf: Int = 23,
    val format: String = "mp4",
    val includeAudio: Boolean = true,
    val hardwareAccel: Boolean = true
)

enum class ExportQuality(val label: String, val crf: Int, val bitrate: Int) {
    Low("流畅", 28, 4_000_000),
    Medium("标清", 25, 8_000_000),
    High("高清", 23, 16_000_000),
    Ultra("超清", 18, 30_000_000),
    FourK("4K", 18, 50_000_000)
}
