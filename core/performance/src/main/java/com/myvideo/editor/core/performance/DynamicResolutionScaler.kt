package com.myvideo.editor.core.performance

class DynamicResolutionScaler {
    var baseWidth = 1920
    var baseHeight = 1080
    var scale = 1f
    private var targetFps = 30
    private var currentFps = 30

    fun updateFps(fps: Float) {
        currentFps = fps.toInt()
        scale = when {
            currentFps < targetFps * 0.5f -> 0.25f
            currentFps < targetFps * 0.7f -> 0.5f
            currentFps < targetFps * 0.9f -> 0.75f
            else -> 1f
        }
    }

    fun getScaledWidth(): Int = (baseWidth * scale).toInt()
    fun getScaledHeight(): Int = (baseHeight * scale).toInt()
    fun setBaseResolution(w: Int, h: Int) { baseWidth = w; baseHeight = h }
    fun setTargetFps(fps: Int) { targetFps = fps }
    fun reset() { scale = 1f }
    fun isDownscaled(): Boolean = scale < 1f
}
