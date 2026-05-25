package com.myvideo.editor.core.ai.common

class TimeConsistencyHelper {
    private val frameBuffer = mutableListOf<FloatArray>()
    var bufferSize = 5
    var blendWeight = 0.3f

    fun addFrame(frame: FloatArray) {
        frameBuffer.add(frame)
        if (frameBuffer.size > bufferSize) frameBuffer.removeAt(0)
    }

    fun getConsistentFrame(): FloatArray? {
        if (frameBuffer.isEmpty()) return null
        if (frameBuffer.size == 1) return frameBuffer[0]
        val latest = frameBuffer.last()
        val prev = frameBuffer[frameBuffer.size - 2]
        return FloatArray(latest.size) { i ->
            latest[i] * (1f - blendWeight) + prev[i] * blendWeight
        }
    }

    fun clear() { frameBuffer.clear() }
    fun getBuffer(): List<FloatArray> = frameBuffer.toList()
}
