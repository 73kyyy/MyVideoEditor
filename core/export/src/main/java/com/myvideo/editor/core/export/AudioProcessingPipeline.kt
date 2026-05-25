package com.myvideo.editor.core.export

class AudioProcessingPipeline {
    data class AudioParams(
        val sampleRate: Int = 44100, val channels: Int = 2,
        val bitDepth: Int = 16, val normalize: Boolean = true,
        val fadeInMs: Long = 0, val fadeOutMs: Long = 0,
        val volume: Float = 1f, val denoise: Boolean = false
    )

    fun process(pcmData: FloatArray, params: AudioParams): FloatArray {
        var result = pcmData.copyOf()
        if (params.normalize) result = normalize(result)
        if (params.volume != 1f) result = applyVolume(result, params.volume)
        if (params.fadeInMs > 0) result = applyFadeIn(result, params.fadeInMs, params.sampleRate)
        if (params.fadeOutMs > 0) result = applyFadeOut(result, params.fadeOutMs, params.sampleRate)
        return result
    }

    private fun normalize(data: FloatArray): FloatArray {
        val max = data.maxOfOrNull { kotlin.math.abs(it) } ?: 1f
        if (max < 0.01f) return data
        val scale = 0.95f / max
        return FloatArray(data.size) { (data[it] * scale).coerceIn(-1f, 1f) }
    }

    private fun applyVolume(data: FloatArray, volume: Float): FloatArray {
        return FloatArray(data.size) { (data[it] * volume).coerceIn(-1f, 1f) }
    }

    private fun applyFadeIn(data: FloatArray, fadeMs: Long, sampleRate: Int): FloatArray {
        val fadeSamples = ((fadeMs / 1000f) * sampleRate).toInt().coerceAtMost(data.size)
        for (i in 0 until fadeSamples) data[i] *= i.toFloat() / fadeSamples
        return data
    }

    private fun applyFadeOut(data: FloatArray, fadeMs: Long, sampleRate: Int): FloatArray {
        val fadeSamples = ((fadeMs / 1000f) * sampleRate).toInt().coerceAtMost(data.size)
        for (i in 0 until fadeSamples) data[data.size - 1 - i] *= i.toFloat() / fadeSamples
        return data
    }
}
