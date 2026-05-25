package com.myvideo.editor.core.video.color.model

data class ColorGradingPreset(
    val id: String, val name: String, val category: String = "General",
    val temperature: Float = 0f, val tint: Float = 0f,
    val contrast: Float = 1f, val saturation: Float = 1f,
    val brightness: Float = 0f, val highlights: Float = 0f,
    val shadows: Float = 0f, val whites: Float = 0f, val blacks: Float = 0f,
    val vibrance: Float = 0f, val hueShift: Float = 0f,
    val shadowsColor: Triple<Float, Float, Float> = Triple(0f, 0f, 0f),
    val midtonesColor: Triple<Float, Float, Float> = Triple(0f, 0f, 0f),
    val highlightsColor: Triple<Float, Float, Float> = Triple(0f, 0f, 0f)
)

object PresetLibrary {
    val PRESETS = listOf(
        ColorGradingPreset("cinema", "电影", "电影", -5f, 0f, 1.15f, 0.85f, -0.05f, 0.1f, -0.1f, 0.05f, -0.1f, 0.1f),
        ColorGradingPreset("vintage", "复古", "风格", 10f, 5f, 0.9f, 0.7f, 0.05f, -0.1f, 0.1f, -0.05f, 0.05f),
        ColorGradingPreset("bw", "黑白", "风格", 0f, 0f, 1.2f, 0f, 0f, 0f, 0f, 0f, 0f),
        ColorGradingPreset("warm", "暖色", "色温", 15f, 0f, 1.05f, 1.1f, 0.02f, 0f, 0f, 0f, 0f),
        ColorGradingPreset("cool", "冷色", "色温", -15f, 0f, 1.05f, 1.05f, 0f, 0f, 0f, 0f, 0f),
        ColorGradingPreset("hdr", "HDR", "增强", 0f, 0f, 1.3f, 1.3f, 0.1f, 0.2f, -0.15f, 0.1f, -0.1f),
        ColorGradingPreset("neon", "霓虹", "风格", 0f, 0f, 1.5f, 1.8f, 0.05f, 0f, 0f, 0f, 0f, 0.3f)
    )
    fun get(id: String): ColorGradingPreset? = PRESETS.find { it.id == id }
    fun getByCategory(cat: String): List<ColorGradingPreset> = PRESETS.filter { it.category == cat }
}
