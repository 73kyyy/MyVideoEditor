package com.myvideo.editor.core.video.color

import com.myvideo.editor.core.video.color.model.ColorGradingPreset
import com.myvideo.editor.core.video.color.model.ColorWheelState
import com.myvideo.editor.core.video.color.model.HSLAdjustment

class ColorGradingEngine {
    private val colorWheel = ColorWheelProcessor()
    private val tempProcessor = ColorTemperatureProcessor()
    private val threeWay = ThreeWayColorCorrector()
    private val hslSecondary = HSLSecondaryProcessor()
    private val lutProcessor = LUTProcessor()

    data class GradingParams(
        val colorWheel: ColorWheelState = ColorWheelState(),
        val temperature: ColorTemperatureProcessor.Params = ColorTemperatureProcessor.Params(),
        val threeWay: ThreeWayColorCorrector.Params = ThreeWayColorCorrector.Params(),
        val hslAdjustments: List<HSLAdjustment> = emptyList(),
        val contrast: Float = 1f,
        val brightness: Float = 0f,
        val saturation: Float = 1f,
        val gamma: Float = 1f
    )

    fun apply(pixels: IntArray, params: GradingParams): IntArray {
        var result = pixels
        result = tempProcessor.apply(result, params.temperature)
        result = colorWheel.apply(result, params.colorWheel)
        result = threeWay.apply(result, params.threeWay)
        if (params.hslAdjustments.isNotEmpty()) result = hslSecondary.apply(result, params.hslAdjustments)
        if (params.contrast != 1f || params.brightness != 0f || params.saturation != 1f)
            result = applyBasic(result, params.contrast, params.brightness, params.saturation)
        return result
    }

    fun applyPreset(pixels: IntArray, preset: ColorGradingPreset): IntArray {
        return apply(pixels, GradingParams(
            temperature = ColorTemperatureProcessor.Params(preset.temperature, preset.tint),
            contrast = preset.contrast, brightness = preset.brightness, saturation = preset.saturation
        ))
    }

    private fun applyBasic(pixels: IntArray, contrast: Float, brightness: Float, saturation: Float): IntArray {
        return IntArray(pixels.size) { i ->
            val p = pixels[i]
            var r = (p shr 16 and 0xFF).toFloat()
            var g = (p shr 8 and 0xFF).toFloat()
            var b = (p and 0xFF).toFloat()
            r = (r*contrast + brightness*255).coerceIn(0f, 255f)
            g = (g*contrast + brightness*255).coerceIn(0f, 255f)
            b = (b*contrast + brightness*255).coerceIn(0f, 255f)
            val gray = 0.299f*r + 0.587f*g + 0.114f*b
            r = gray + (r - gray) * saturation
            g = gray + (g - gray) * saturation
            b = gray + (b - gray) * saturation
            (p and 0xFF000000.toInt()) or (r.toInt().coerceIn(0,255) shl 16) or (g.toInt().coerceIn(0,255) shl 8) or b.toInt().coerceIn(0,255)
        }
    }
}
