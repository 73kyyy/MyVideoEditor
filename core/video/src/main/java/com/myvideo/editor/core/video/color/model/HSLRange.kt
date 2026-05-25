package com.myvideo.editor.core.video.color.model

data class HSLRange(
    val hueMin: Float = 0f, val hueMax: Float = 360f,
    val satMin: Float = 0f, val satMax: Float = 1f,
    val lightMin: Float = 0f, val lightMax: Float = 1f
)

data class HSLAdjustment(
    val hueShift: Float = 0f,
    val saturation: Float = 0f,
    val lightness: Float = 0f,
    val range: HSLRange = HSLRange()
)

object HSLSamples {
    val RED = HSLRange(345f, 15f, 0.3f, 1f, 0.2f, 0.8f)
    val ORANGE = HSLRange(15f, 45f, 0.3f, 1f, 0.3f, 0.8f)
    val YELLOW = HSLRange(45f, 75f, 0.3f, 1f, 0.4f, 0.9f)
    val GREEN = HSLRange(75f, 165f, 0.2f, 1f, 0.2f, 0.7f)
    val CYAN = HSLRange(165f, 195f, 0.2f, 1f, 0.3f, 0.8f)
    val BLUE = HSLRange(195f, 255f, 0.2f, 1f, 0.2f, 0.7f)
    val PURPLE = HSLRange(255f, 285f, 0.2f, 1f, 0.2f, 0.6f)
    val MAGENTA = HSLRange(285f, 345f, 0.3f, 1f, 0.3f, 0.7f)
}
