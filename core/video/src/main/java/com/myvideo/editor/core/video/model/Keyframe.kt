package com.myvideo.editor.core.video.model

data class Keyframe(
    val timeMs: Long, val property: String, val value: Float,
    val interpolation: Interpolation = Interpolation.Linear
)

enum class Interpolation { Linear, EaseIn, EaseOut, EaseInOut, Bezier, Hold }
