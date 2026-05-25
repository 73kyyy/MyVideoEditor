package com.myvideo.editor.core.video.model

data class MaskData(
    val id: String, val points: List<MaskPoint> = emptyList(),
    var isClosed: Boolean = false, var feather: Float = 0f,
    var opacity: Float = 1f, var invert: Boolean = false
)

data class MaskPoint(
    var x: Float, var y: Float,
    var handleInX: Float = 0f, var handleInY: Float = 0f,
    var handleOutX: Float = 0f, var handleOutY: Float = 0f
)

enum class MaskShape { Pen, Circle, Rectangle, Star }
