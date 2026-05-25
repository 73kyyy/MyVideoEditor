package com.myvideo.editor.core.video

class CanvasRatioManager {
    data class CanvasRatio(val label: String, val width: Int, val height: Int)

    val RATIOS = listOf(
        CanvasRatio("16:9", 1920, 1080), CanvasRatio("9:16", 1080, 1920),
        CanvasRatio("1:1", 1080, 1080), CanvasRatio("4:5", 1080, 1350),
        CanvasRatio("21:9", 2560, 1080), CanvasRatio("4:3", 1440, 1080),
        CanvasRatio("3:4", 1080, 1440), CanvasRatio("自由", 0, 0)
    )

    private var current = RATIOS[0]
    fun getCurrent(): CanvasRatio = current
    fun setRatio(label: String) { current = RATIOS.find { it.label == label } ?: current }
    fun setCustom(w: Int, h: Int) { current = CanvasRatio("自定义", w, h) }
    fun getRatioValue(): Float = if (current.height > 0) current.width.toFloat() / current.height else 1f
    fun isPortrait(): Boolean = current.height > current.width
    fun isSquare(): Boolean = current.width == current.height
}
