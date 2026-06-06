package com.videoeditor.data.model

import java.util.UUID

sealed class Clip {
    abstract val id: String
    abstract val startUs: Long
    abstract val endUs: Long
    abstract val trimStartUs: Long
    abstract val trimEndUs: Long
    abstract val sourcePath: String

    val durationUs: Long get() = endUs - startUs
    val sourceDurationUs: Long get() = trimEndUs - trimStartUs
}

data class VideoClip(
    override val id: String = UUID.randomUUID().toString(),
    override val startUs: Long = 0L,
    override val endUs: Long = 0L,
    override val trimStartUs: Long = 0L,
    override val trimEndUs: Long = 0L,
    override val sourcePath: String = "",
    val width: Int = 0,
    val height: Int = 0,
    val rotation: Int = 0,
    val volume: Float = 1.0f,
    val speed: Float = 1.0f,
    val filters: List<Filter> = emptyList(),
    val opacity: Float = 1.0f
) : Clip()

data class AudioClip(
    override val id: String = UUID.randomUUID().toString(),
    override val startUs: Long = 0L,
    override val endUs: Long = 0L,
    override val trimStartUs: Long = 0L,
    override val trimEndUs: Long = 0L,
    override val sourcePath: String = "",
    val volume: Float = 1.0f,
    val speed: Float = 1.0f,
    val fadeInMs: Long = 0L,
    val fadeOutMs: Long = 0L
) : Clip()

data class TextClip(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val startUs: Long = 0L,
    val endUs: Long = 0L,
    val x: Float = 0.5f,
    val y: Float = 0.5f,
    val fontSize: Float = 24f,
    val fontColor: Long = 0xFFFFFFFF,
    val backgroundColor: Long = 0x00000000,
    val fontFamily: String = "default",
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val animation: TextAnimation = TextAnimation.NONE
)

enum class TextAnimation {
    NONE, FADE_IN, FADE_OUT, TYPING, SCALE
}
