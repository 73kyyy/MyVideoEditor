package com.myvideo.editor.core.ai.separation

import com.myvideo.editor.core.ai.common.InferenceSessionManager

class DemucsWrapper(private val sessionManager: InferenceSessionManager) {
    private var modelId = "demucs"
    var isReady = false; private set

    data class SeparatedTracks(
        val vocals: FloatArray, val drums: FloatArray,
        val bass: FloatArray, val other: FloatArray
    )

    fun init(modelPath: String): Boolean {
        isReady = sessionManager.loadModel(modelPath, modelId)
        return isReady
    }

    fun separate(audioData: FloatArray, sampleRate: Int = 44100): SeparatedTracks? {
        if (!isReady) return null
        val result = sessionManager.run(modelId, "audio", audioData, longArrayOf(1, 2, audioData.size.toLong()))
        return result?.let {
            val len = it.size / 4
            SeparatedTracks(
                vocals = it.copyOfRange(0, len),
                drums = it.copyOfRange(len, 2 * len),
                bass = it.copyOfRange(2 * len, 3 * len),
                other = it.copyOfRange(3 * len, 4 * len)
            )
        }
    }

    fun extractVocals(audioData: FloatArray): FloatArray? = separate(audioData)?.vocals
    fun extractDrums(audioData: FloatArray): FloatArray? = separate(audioData)?.drums
    fun extractBass(audioData: FloatArray): FloatArray? = separate(audioData)?.bass

    fun release() { sessionManager.release(modelId); isReady = false }
}
