package com.myvideo.editor.feature.effects.transition

enum class TransitionType(val label: String, val ffmpegXfade: String) {
    Fade("淡入淡出", "fade"),
    SlideLeft("向左滑动", "slideleft"),
    SlideRight("向右滑动", "slideright"),
    SlideUp("向上滑动", "slideup"),
    SlideDown("向下滑动", "slidedown"),
    WipeLeft("向左擦除", "wipeleft"),
    WipeRight("向右擦除", "wiperight"),
    ZoomIn("放大", "zoomin"),
    ZoomOut("缩小", "zoomout"),
    Rotate("旋转", "circleopen"),
    Dissolve("溶解", "dissolve"),
    FlashWhite("闪白", "fadeblack"),
    Blur("模糊", "fadeblur"),
    Mosaic("马赛克", "radial")
}

data class TransitionConfig(
    val type: TransitionType = TransitionType.Fade,
    val durationMs: Long = 500,
    val easing: String = "linear"
)
