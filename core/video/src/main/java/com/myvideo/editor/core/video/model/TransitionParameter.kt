package com.myvideo.editor.core.video.model

data class TransitionParameter(
    val name: String, val label: String, val type: ParamType,
    val defaultValue: Float, val minValue: Float = 0f,
    val maxValue: Float = 1f, var currentValue: Float = defaultValue
)

enum class ParamType { Slider, Toggle, Color, Dropdown }

object TransitionDefaults {
    fun getParams(type: TransitionType): List<TransitionParameter> = when (type) {
        TransitionType.CrossFade -> listOf(TransitionParameter("opacity", "不透明度", ParamType.Slider, 1f))
        TransitionType.SlideLeft, TransitionType.SlideRight -> listOf(
            TransitionParameter("distance", "距离", ParamType.Slider, 1f),
            TransitionParameter("bounce", "弹性", ParamType.Slider, 0f)
        )
        TransitionType.ZoomIn, TransitionType.ZoomOut -> listOf(
            TransitionParameter("scale", "缩放", ParamType.Slider, 2f, 1f, 5f)
        )
        TransitionType.Blur -> listOf(
            TransitionParameter("strength", "模糊强度", ParamType.Slider, 20f, 0f, 50f)
        )
        else -> emptyList()
    }
}
