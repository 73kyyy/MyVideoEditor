package com.myvideo.editor.core.video.color

class ThreeWayColorCorrector {
    data class ColorShift(var r: Float = 0f, var g: Float = 0f, var b: Float = 0f)
    data class Params(
        val shadows: ColorShift = ColorShift(), val midtones: ColorShift = ColorShift(),
        val highlights: ColorShift = ColorShift(), val shadowSat: Float = 1f, val highlightSat: Float = 1f
    )

    fun apply(pixels: IntArray, params: Params): IntArray {
        return IntArray(pixels.size) { i ->
            val p = pixels[i]
            var r = (p shr 16 and 0xFF).toFloat(); var g = (p shr 8 and 0xFF).toFloat(); var b = (p and 0xFF).toFloat()
            val lum = 0.299f*r + 0.587f*g + 0.114f*b
            val sw = (1f - lum/255f).coerceIn(0f, 1f)
            val hw = (lum/255f).coerceIn(0f, 1f)
            val mw = 1f - kotlin.math.abs(lum/255f - 0.5f) * 2f
            r += params.shadows.r*sw*params.shadowSat + params.midtones.r*mw + params.highlights.r*hw*params.highlightSat
            g += params.shadows.g*sw*params.shadowSat + params.midtones.g*mw + params.highlights.g*hw*params.highlightSat
            b += params.shadows.b*sw*params.shadowSat + params.midtones.b*mw + params.highlights.b*hw*params.highlightSat
            (p and 0xFF000000.toInt()) or (r.toInt().coerceIn(0,255) shl 16) or (g.toInt().coerceIn(0,255) shl 8) or b.toInt().coerceIn(0,255)
        }
    }
}
