package com.myvideo.editor.core.ai.speech

import com.myvideo.editor.core.ai.common.InferenceSessionManager

class WhisperWrapper(private val sessionManager: InferenceSessionManager) {
    private var modelId = "whisper"
    var isReady = false; private set

    data class TranscriptionResult(
        val text: String, val segments: List<Segment> = emptyList()
    )
    data class Segment(val startMs: Long, val endMs: Long, val text: String)

    fun init(modelPath: String): Boolean {
        isReady = sessionManager.loadModel(modelPath, modelId)
        return isReady
    }

    fun transcribe(audioPath: String): TranscriptionResult? {
        if (!isReady) return null
        return try {
            val audioData = readAudioFile(audioPath)
            val melSpec = computeMelSpectrogram(audioData)
            val result = sessionManager.run(modelId, "audio_features", melSpec, longArrayOf(1, melSpec.size.toLong()))
            result?.let { parseTranscription(it) }
        } catch (e: Exception) { null }
    }

    fun transcribeFromPcm(pcm: FloatArray, sampleRate: Int): TranscriptionResult? {
        if (!isReady) return null
        val melSpec = computeMelSpectrogram(pcm)
        val result = sessionManager.run(modelId, "audio_features", melSpec, longArrayOf(1, melSpec.size.toLong()))
        return result?.let { parseTranscription(it) }
    }

    private fun readAudioFile(path: String): FloatArray = FloatArray(0)
    private fun computeMelSpectrogram(audio: FloatArray): FloatArray = audio
    private fun parseTranscription(output: FloatArray): TranscriptionResult = TranscriptionResult(output.joinToString(""))

    fun release() { sessionManager.release(modelId); isReady = false }
}
