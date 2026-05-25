package com.myvideo.editor.core.performance

class AdaptiveFrameRateController {
    var targetFps = 30
    var currentFps = 30
    private val frameTimes = mutableListOf<Long>()
    private var lastFrameTime = 0L
    private val maxHistory = 60

    fun onFrame() {
        val now = System.nanoTime()
        if (lastFrameTime > 0) frameTimes.add(now - lastFrameTime)
        lastFrameTime = now
        if (frameTimes.size > maxHistory) frameTimes.removeAt(0)
    }

    fun getMeasuredFps(): Float {
        if (frameTimes.size < 2) return targetFps.toFloat()
        val avgNs = frameTimes.average().toLong()
        return if (avgNs > 0) 1_000_000_000f / avgNs else targetFps.toFloat()
    }

    fun shouldDropFrame(): Boolean = getMeasuredFps() < targetFps * 0.8f

    fun adjustTarget(loadPercent: Float) {
        targetFps = when {
            loadPercent > 90f -> 15
            loadPercent > 75f -> 24
            loadPercent > 50f -> 30
            else -> 60
        }
    }

    fun reset() { frameTimes.clear(); lastFrameTime = 0L }
}
