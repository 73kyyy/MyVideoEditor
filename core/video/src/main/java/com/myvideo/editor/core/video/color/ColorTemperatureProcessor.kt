package com.myvideo.editor.core.video.color

class ColorTemperatureProcessor {
    data class Params(var temperature: Float = 0f, var tint: Float = 0f)

    fun apply(pixels: IntArray, params: Params): IntArray {
        val tempShift = params.temperature / 100f
        val tintShift = params.tint / 100f
        return IntArray(pixels.size) { i ->
            val p = pixels[i]
            var r = (p shr 16 and 0xFF) + (tempShift * 30).toInt()
            var g = (p shr 8 and 0xFF) + (tintShift * 15).toInt()
            var b = (p and 0xFF) - (tempShift * 30).toInt()
            r = r.coerceIn(0, 255); g = g.coerceIn(0, 255); b = b.coerceIn(0, 255)
            (p and 0xFF000000.toInt()) or (r shl 16) or (g shl 8) or b
        }
    }
}
