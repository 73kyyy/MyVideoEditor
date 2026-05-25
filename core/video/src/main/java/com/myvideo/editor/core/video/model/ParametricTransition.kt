package com.myvideo.editor.core.video.model

data class ParametricTransition(
    val id: String, val name: String, val type: TransitionType,
    val durationMs: Long = 500, val easing: EasingType = EasingType.Linear,
    val parameters: Map<String, Float> = emptyMap()
)

enum class TransitionType {
    CrossFade, SlideLeft, SlideRight, SlideUp, SlideDown,
    WipeLeft, WipeRight, WipeUp, WipeDown,
    ZoomIn, ZoomOut, Rotate, CircleOpen, CircleClose,
    Dissolve, FlashWhite, FlashBlack, Blur, Pixelate
}

enum class EasingType { Linear, EaseIn, EaseOut, EaseInOut, Bounce, Elastic, Back }
