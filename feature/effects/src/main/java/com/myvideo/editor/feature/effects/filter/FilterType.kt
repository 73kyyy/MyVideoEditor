package com.myvideo.editor.feature.effects.filter

enum class FilterType(val label: String) {
    None("无"), Natural("自然"), BnW("黑白"), Vintage("复古"),
    Cold("冷色"), Warm("暖色"), HighContrast("高对比"), Soft("柔和"),
    Vivid("鲜艳"), Nostalgic("怀旧"), Film("胶片"), HDR("HDR"),
    Cinema("电影"), Blur("模糊"), Sharpen("锐化"), Glow("发光"),
    Mosaic("马赛克"), Pixelate("像素化"), Emboss("浮雕"), OldFilm("老电影"),
    Glitch("故障"), Neon("霓虹"), OilPaint("油画"), Sketch("素描"), Cartoon("卡通")
}

data class FilterParams(
    val type: FilterType = FilterType.None,
    val intensity: Float = 1f,
    val brightness: Float = 0f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val temperature: Float = 0f,
    val vignette: Float = 0f,
    val grain: Float = 0f
)
