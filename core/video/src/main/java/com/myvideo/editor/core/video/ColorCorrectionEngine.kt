package com.myvideo.editor.core.video

import com.myvideo.editor.core.video.color.ColorGradingEngine
import com.myvideo.editor.core.video.color.model.ColorGradingPreset

class ColorCorrectionEngine {
    private val gradingEngine = ColorGradingEngine()
    private var currentParams = ColorGradingEngine.GradingParams()

    fun setParams(params: ColorGradingEngine.GradingParams) { currentParams = params }
    fun getParams(): ColorGradingEngine.GradingParams = currentParams
    fun applyPreset(preset: ColorGradingPreset) { currentParams = ColorGradingEngine.GradingParams() }

    fun adjustContrast(v: Float) { currentParams = currentParams.copy(contrast = v) }
    fun adjustBrightness(v: Float) { currentParams = currentParams.copy(brightness = v) }
    fun adjustSaturation(v: Float) { currentParams = currentParams.copy(saturation = v) }
    fun reset() { currentParams = ColorGradingEngine.GradingParams() }
    fun applyToPixels(pixels: IntArray): IntArray = gradingEngine.apply(pixels, currentParams)
}
