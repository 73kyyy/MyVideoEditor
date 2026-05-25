package com.myvideo.editor.core.video.codec

import android.media.MediaCodecInfo
import android.media.MediaCodecList

class CodecCapabilityDetector {

    data class CodecInfo(
        val name: String, val mimeType: String,
        val isHardware: Boolean, val maxWidth: Int, val maxHeight: Int,
        val maxFps: Int, val supportedColorFormats: List<Int>
    )

    fun getEncoderCapabilities(mimeType: String): List<CodecInfo> {
        val result = mutableListOf<CodecInfo>()
        val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        for (info in codecList.codecInfos) {
            if (!info.isEncoder) continue
            try {
                val caps = info.getCapabilitiesForType(mimeType)
                val videoCaps = caps.videoCapabilities ?: continue
                result.add(CodecInfo(
                    info.name, mimeType, !info.name.contains("sw"),
                    videoCaps.supportedWidths.upper, videoCaps.supportedHeights.upper,
                    videoCaps.getSupportedFrameRatesFor(videoCaps.supportedWidths.upper, videoCaps.supportedHeights.upper).upper.toInt(),
                    caps.colorFormats.toList()
                ))
            } catch (_: Exception) {}
        }
        return result
    }

    fun getBestEncoder(mimeType: String): CodecInfo? {
        return getEncoderCapabilities(mimeType).filter { it.isHardware }.maxByOrNull { it.maxWidth * it.maxHeight }
    }

    fun supportsHdr(mimeType: String): Boolean {
        val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        for (info in codecList.codecInfos) {
            if (!info.isEncoder) continue
            try {
                val caps = info.getCapabilitiesForType(mimeType)
                if (caps.profileLevels.any { it.profile in listOf(0x1000, 0x2000, 0x4000) }) return true
            } catch (_: Exception) {}
        }
        return false
    }
}
