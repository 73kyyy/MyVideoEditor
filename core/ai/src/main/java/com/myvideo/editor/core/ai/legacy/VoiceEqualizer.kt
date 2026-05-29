package com.myvideo.editor.core.ai.legacy

class VoiceEqualizer {
    data class Band(val freqHz: Float, var gain: Float = 0f, val q: Float = 1f)

    private val bands = mutableListOf(
        Band(60f), Band(170f), Band(350f), Band(1000f),
        Band(3500f), Band(5000f), Band(10000f), Band(14000f)
    )

    fun setGain(bandIndex: Int, gain: Float) {
        if (bandIndex in bands.indices) bands[bandIndex].gain = gain.coerceIn(-12f, 12f)
    }

    fun apply(pcmData: FloatArray, sampleRate: Int = 44100): FloatArray {
        var result = pcmData.copyOf()
        bands.forEach { band ->
            if (band.gain != 0f) result = applyBand(result, band, sampleRate)
        }
        return result
    }

    fun getBands(): List<Band> = bands.toList()

    private fun applyBand(data: FloatArray, band: Band, sampleRate: Int): FloatArray {
        val sr: Double = sampleRate.toDouble()
        val omega: Double = 2.0 * Math.PI * band.freqHz.toDouble() / sr
        val sinVal: Double = Math.sin(omega)
        val cosVal: Double = Math.cos(omega)
        val alpha: Float = (sinVal / (2.0 * band.q.toDouble())).toFloat()
        val g: Float = Math.pow(10.0, band.gain.toDouble() / 20.0).toFloat()
        val a0: Float = 1f + alpha / g
        val b0: Float = (1f + alpha * g) / a0
        val b1: Float = (-2f * cosVal.toFloat()) / a0
        val b2: Float = (1f - alpha * g) / a0
        val a1: Float = b1
        val a2: Float = (1f - alpha / g) / a0
        val result = FloatArray(data.size)
        var x1 = 0f; var x2 = 0f; var y1 = 0f; var y2 = 0f
        for (i in data.indices) {
            result[i] = b0 * data[i] + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2
            x2 = x1; x1 = data[i]; y2 = y1; y1 = result[i]
        }
        return result
    }
}
