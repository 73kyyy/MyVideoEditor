package com.videoeditor.data.model

import java.util.UUID

data class Filter(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val type: FilterType = FilterType.NONE,
    val intensity: Float = 1.0f,
    val params: Map<String, Float> = emptyMap()
)

enum class FilterType(val displayName: String) {
    NONE("原片"),
    BRIGHTNESS("亮度"),
    CONTRAST("对比度"),
    SATURATION("饱和度"),
    HUE("色相"),
    WARM("暖色"),
    COOL("冷色"),
    VINTAGE("复古"),
    BLACK_WHITE("黑白"),
    SEPIA("怀旧"),
    VIGNETTE("暗角"),
    BLUR("模糊"),
    SHARPEN("锐化"),
    GLITCH("故障"),
    FILM("胶片")
}
