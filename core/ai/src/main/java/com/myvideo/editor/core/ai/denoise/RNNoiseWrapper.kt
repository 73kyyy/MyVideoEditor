package com.myvideo.editor.core.ai.denoise

class RNNoiseWrapper {
    private var nativePtr: Long = 0
    var isReady = false; private set

    external fun nativeInit(): Long
    external fun nativeProcess(ptr: Long, pcm: FloatArray): FloatArray
    external fun nativeRelease(ptr: Long)

    fun init(): Boolean {
        return try {
            nativePtr = nativeInit()
            isReady = nativePtr != 0L
            isReady
        } catch (e: Exception) { false }
    }

    fun denoise(pcmData: FloatArray): FloatArray? {
        if (!isReady || nativePtr == 0L) return null
        return try { nativeProcess(nativePtr, pcmData) }
        catch (e: Exception) { null }
    }

    fun denoiseChunked(pcmData: FloatArray, chunkSize: Int = 480): FloatArray {
        val result = FloatArray(pcmData.size)
        var offset = 0
        while (offset < pcmData.size) {
            val end = (offset + chunkSize).coerceAtMost(pcmData.size)
            val chunk = pcmData.copyOfRange(offset, end)
            val denoised = denoise(chunk) ?: chunk
            System.arraycopy(denoised, 0, result, offset, denoised.size)
            offset += chunkSize
        }
        return result
    }

    fun release() { if (nativePtr != 0L) { nativeRelease(nativePtr); nativePtr = 0 }; isReady = false }
}
