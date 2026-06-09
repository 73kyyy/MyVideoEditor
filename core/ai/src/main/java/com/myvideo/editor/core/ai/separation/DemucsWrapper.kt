package com.myvideo.editor.core.ai.separation

import com.myvideo.editor.core.ai.common.InferenceSessionManager

class DemucsWrapper(private val sessionManager: InferenceSessionManager) {
    private var modelId = "demucs"
    var isReady = false; private set

    data class SeparatedTracks(
        val drums: FloatArray, val bass: FloatArray,
        val other: FloatArray, val vocals: FloatArray,
        val sampleRate: Int = 44100
    )

    fun init(modelPath: String): Boolean {
        isReady = sessionManager.loadModel(modelPath, modelId)
        return isReady
    }

    fun initFromSession(modelId: String = "demucs"): Boolean {
        this.modelId = modelId
        isReady = sessionManager.isLoaded(modelId)
        return isReady
    }

    fun separate(monoAudio: FloatArray, sampleRate: Int = 44100): SeparatedTracks? {
        if (!isReady) return null
        return separateStereo(monoToStereo(monoAudio), sampleRate)
    }

    fun separateStereo(stereoAudio: FloatArray, sampleRate: Int = 44100): SeparatedTracks? {
        if (!isReady) return null
        // stereoAudio layout: [2 * samples] interleaved or [left, right] concatenated
        val numSamples = stereoAudio.size / 2
        // Demucs expects [1, 2, samples] in NCHW format: [left_channel, right_channel]
        val input = FloatArray(2 * numSamples)
        System.arraycopy(stereoAudio, 0, input, 0, numSamples)           // left channel
        System.arraycopy(stereoAudio, numSamples, input, numSamples, numSamples)  // right channel

        val result = sessionManager.run(modelId, "audio", input, longArrayOf(1, 2, numSamples.toLong()))
        return result?.let { parseStems(it, numSamples, sampleRate) }
    }

    fun extractVocals(monoAudio: FloatArray, sampleRate: Int = 44100): FloatArray? {
        return separate(monoAudio, sampleRate)?.vocals
    }

    fun extractDrums(monoAudio: FloatArray, sampleRate: Int = 44100): FloatArray? {
        return separate(monoAudio, sampleRate)?.drums
    }

    fun extractBass(monoAudio: FloatArray, sampleRate: Int = 44100): FloatArray? {
        return separate(monoAudio, sampleRate)?.bass
    }

    private fun monoToStereo(mono: FloatArray): FloatArray {
        // Duplicate mono to stereo: [left, right] = [mono, mono]
        val stereo = FloatArray(mono.size * 2)
        System.arraycopy(mono, 0, stereo, 0, mono.size)           // left = mono
        System.arraycopy(mono, 0, stereo, mono.size, mono.size)   // right = mono
        return stereo
    }

    private fun parseStems(output: FloatArray, numSamples: Int, sampleRate: Int): SeparatedTracks {
        // Output layout: [4 stems, 2 channels, samples] flattened
        // Stem order: drums, bass, other, vocals
        val stemSize = 2 * numSamples
        return SeparatedTracks(
            drums = stereoToMono(output, 0, numSamples, stemSize),
            bass = stereoToMono(output, 1, numSamples, stemSize),
            other = stereoToMono(output, 2, numSamples, stemSize),
            vocals = stereoToMono(output, 3, numSamples, stemSize),
            sampleRate = sampleRate
        )
    }

    private fun stereoToMono(data: FloatArray, stemIndex: Int, numSamples: Int, stemSize: Int): FloatArray {
        val offset = stemIndex * stemSize
        val mono = FloatArray(numSamples)
        for (i in 0 until numSamples) {
            val left = if (offset + i < data.size) data[offset + i] else 0f
            val right = if (offset + numSamples + i < data.size) data[offset + numSamples + i] else 0f
            mono[i] = (left + right) / 2f
        }
        return mono
    }

    fun release() { sessionManager.release(modelId); isReady = false }
}
