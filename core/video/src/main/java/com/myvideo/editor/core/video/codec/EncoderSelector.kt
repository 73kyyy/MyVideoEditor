package com.myvideo.editor.core.video.codec

class EncoderSelector(private val detector: CodecCapabilityDetector) {

    data class EncodeConfig(
        val codec: String, val width: Int, val height: Int,
        val fps: Int, val bitrate: Int, val useHardware: Boolean,
        val hdr: Boolean = false
    )

    fun select(width: Int, height: Int, fps: Int, bitrate: Int, preferHdr: Boolean = false): EncodeConfig {
        val mime = if (preferHdr && detector.supportsHdr("video/hevc")) "video/hevc" else "video/avc"
        val best = detector.getBestEncoder(mime)
        return EncodeConfig(
            codec = mime, width = width, height = height, fps = fps,
            bitrate = bitrate, useHardware = best != null, hdr = preferHdr && mime == "video/hevc"
        )
    }

    fun selectForResolution(resolution: String): EncodeConfig = when (resolution) {
        "4K" -> select(3840, 2160, 60, 50_000_000, true)
        "2K" -> select(2560, 1440, 60, 30_000_000)
        "1080p" -> select(1920, 1080, 30, 16_000_000)
        "720p" -> select(1280, 720, 30, 8_000_000)
        else -> select(854, 480, 24, 4_000_000)
    }
}
