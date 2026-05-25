package com.myvideo.editor.feature.effects.filter

class FilterManager {
    private var currentFilter = FilterParams()

    fun setFilter(type: FilterType) { currentFilter = currentFilter.copy(type = type) }
    fun setIntensity(v: Float) { currentFilter = currentFilter.copy(intensity = v.coerceIn(0f, 1f)) }
    fun setBrightness(v: Float) { currentFilter = currentFilter.copy(brightness = v.coerceIn(-1f, 1f)) }
    fun setContrast(v: Float) { currentFilter = currentFilter.copy(contrast = v.coerceIn(0f, 3f)) }
    fun setSaturation(v: Float) { currentFilter = currentFilter.copy(saturation = v.coerceIn(0f, 3f)) }
    fun setTemperature(v: Float) { currentFilter = currentFilter.copy(temperature = v.coerceIn(-100f, 100f)) }
    fun setVignette(v: Float) { currentFilter = currentFilter.copy(vignette = v.coerceIn(0f, 1f)) }
    fun setGrain(v: Float) { currentFilter = currentFilter.copy(grain = v.coerceIn(0f, 1f)) }
    fun getCurrent(): FilterParams = currentFilter
    fun reset() { currentFilter = FilterParams() }
    fun getFfmpegFilter(): String {
        val parts = mutableListOf<String>()
        if (currentFilter.brightness != 0f || currentFilter.contrast != 1f || currentFilter.saturation != 1f)
            parts.add("eq=brightness=${currentFilter.brightness}:contrast=${currentFilter.contrast}:saturation=${currentFilter.saturation}")
        if (currentFilter.temperature > 0) parts.add("colorbalance=rs=${currentFilter.temperature/200f}:rm=${currentFilter.temperature/400f}")
        if (currentFilter.temperature < 0) parts.add("colorbalance=bs=${-currentFilter.temperature/200f}:bm=${-currentFilter.temperature/400f}")
        if (currentFilter.vignette > 0) parts.add("vignette=PI/${(4/currentFilter.vignette).toInt().coerceAtLeast(1)}")
        if (currentFilter.grain > 0) parts.add("noise=alls=${(currentFilter.grain*50).toInt()}:allf=t+u")
        return if (parts.isEmpty()) "null" else parts.joinToString(",")
    }
}
