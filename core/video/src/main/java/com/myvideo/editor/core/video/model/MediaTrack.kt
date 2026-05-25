package com.myvideo.editor.core.video.model

data class MediaTrack(
    val id: String,
    val index: Int,
    val name: String,
    val type: TrackType = TrackType.Video,
    val clips: MutableList<TrackClip> = mutableListOf(),
    var isVisible: Boolean = true,
    var isLocked: Boolean = false,
    var isMuted: Boolean = false,
    var isSolo: Boolean = false,
    var volume: Float = 1f,
    var opacity: Float = 1f
)

enum class TrackType { Video, Audio, Subtitle, Effect, Adjustment }

data class TrackClip(
    val id: String,
    val name: String,
    var startMs: Long,
    var endMs: Long,
    var trimStartMs: Long = 0,
    var trimEndMs: Long = 0,
    var trackIndex: Int = 0,
    val mediaPath: String = "",
    var speed: Float = 1f,
    var isReversed: Boolean = false,
    var opacity: Float = 1f,
    var volume: Float = 1f,
    val keyframes: MutableList<Keyframe> = mutableListOf(),
    val effects: MutableList<String> = mutableListOf(),
    var blendMode: String = "正常"
) {
    val durationMs: Long get() = endMs - startMs
}
