package com.myvideo.editor.core.video.color

import com.myvideo.editor.core.video.color.model.ColorWheelState

class ColorWheelProcessor {

    fun apply(pixels: IntArray, state: ColorWheelState): IntArray {
        if (kotlin.math.abs(state.hue) < 0.01f && kotlin.math.abs(state.saturation) < 0.01f) return pixels
        return IntArray(pixels.size) { i ->
            val p = pixels[i]
            var r = (p shr 16 and 0xFF) / 255f
            var g = (p shr 8 and 0xFF) / 255f
            var b = (p and 0xFF) / 255f
            val max = maxOf(r, g, b); val min = minOf(r, g, b)
            val l = (max + min) / 2f
            var s = 0f; var h = 0f
            if (max != min) {
                val d = max - min
                s = if (l > 0.5f) d / (2f - max - min) else d / (max + min)
                h = when (max) { r -> ((g - b) / d + (if (g < b) 6 else 0)) * 60f; g -> ((b - r) / d + 2) * 60f; else -> ((r - g) / d + 4) * 60f }
            }
            h = (h + state.hue + 360f) % 360f
            s = (s + state.saturation).coerceIn(0f, 1f)
            val nl = (l + state.lightness - 0.5f).coerceIn(0f, 1f)
            hslToPixel(h, s, nl, p shr 24 and 0xFF)
        }
    }

    private fun hslToPixel(h: Float, s: Float, l: Float, a: Int): Int {
        if (s == 0f) { val v = (l * 255).toInt().coerceIn(0, 255); return (a shl 24) or (v shl 16) or (v shl 8) or v }
        val q = if (l < 0.5f) l * (1f + s) else l + s - l * s
        val p = 2f * l - q
        val hue = h / 360f
        val r = (hue2rgb(p, q, hue + 1f/3f) * 255).toInt().coerceIn(0, 255)
        val g = (hue2rgb(p, q, hue) * 255).toInt().coerceIn(0, 255)
        val b = (hue2rgb(p, q, hue - 1f/3f) * 255).toInt().coerceIn(0, 255)
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }

    private fun hue2rgb(p: Float, q: Float, t: Float): Float {
        var tt = t; if (tt < 0f) tt += 1f; if (tt > 1f) tt -= 1f
        return when { tt < 1f/6f -> p + (q - p) * 6f * tt; tt < 0.5f -> q; tt < 2f/3f -> p + (q - p) * (2f/3f - tt) * 6f; else -> p }
    }
}
