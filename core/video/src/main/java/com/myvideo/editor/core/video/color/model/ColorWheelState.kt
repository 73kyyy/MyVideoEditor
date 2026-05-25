package com.myvideo.editor.core.video.color.model

data class ColorWheelState(
    var hue: Float = 0f,
    var saturation: Float = 0f,
    var lightness: Float = 0.5f
) {
    fun reset() { hue = 0f; saturation = 0f; lightness = 0.5f }
    fun toRgb(): Triple<Int, Int, Int> {
        val h = hue / 60f
        val s = saturation.coerceIn(0f, 1f)
        val l = lightness.coerceIn(0f, 1f)
        val c = (1f - kotlin.math.abs(2f * l - 1f)) * s
        val x = c * (1f - kotlin.math.abs(h % 2f - 1f))
        val m = l - c / 2f
        val (r, g, b) = when (h.toInt()) {
            0 -> Triple(c, x, 0f); 1 -> Triple(x, c, 0f); 2 -> Triple(0f, c, x)
            3 -> Triple(0f, x, c); 4 -> Triple(x, 0f, c); else -> Triple(c, 0f, x)
        }
        return Triple(((r+m)*255).toInt().coerceIn(0,255), ((g+m)*255).toInt().coerceIn(0,255), ((b+m)*255).toInt().coerceIn(0,255))
    }
}
