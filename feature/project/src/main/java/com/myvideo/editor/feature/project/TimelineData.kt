package com.myvideo.editor.feature.project

data class TimelineData(
    val tracks: MutableList<TrackData> = mutableListOf(),
    var totalDurationMs: Long = 0,
    var zoomLevel: Float = 1f,
    var scrollPosition: Float = 0f
)

data class TrackData(
    val id: String,
    val name: String,
    val type: String = "video",
    val clips: MutableList<ClipData> = mutableListOf(),
    var isMuted: Boolean = false,
    var isLocked: Boolean = false,
    var volume: Float = 1f
)

data class ClipData(
    val id: String,
    val mediaPath: String,
    var startMs: Long,
    var endMs: Long,
    var trackIndex: Int = 0,
    var speed: Float = 1f,
    var volume: Float = 1f
) {
    val durationMs: Long get() = endMs - startMs
}
