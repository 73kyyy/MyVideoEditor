package com.myvideo.editor.feature.subtitle

data class SubtitleItem(
    val id: String, val text: String, val startMs: Long, val endMs: Long,
    val style: SubtitleStyle = SubtitleStyle(), val position: SubtitlePosition = SubtitlePosition()
)

data class SubtitleStyle(
    val fontSize: Int = 24, val fontColor: Long = 0xFFFFFFFF, val bgColor: Long = 0x80000000,
    val fontFamily: String = "default", val isBold: Boolean = false, val isItalic: Boolean = false,
    val outlineColor: Long = 0xFF000000, val outlineWidth: Float = 1f,
    val shadowColor: Long = 0x80000000, val shadowDx: Float = 2f, val shadowDy: Float = 2f,
    val alignment: Int = 2
)

data class SubtitlePosition(val x: Float = 0.5f, val y: Float = 0.9f, val width: Float = 0.8f)
