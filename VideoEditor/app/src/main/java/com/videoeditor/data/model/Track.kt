package com.videoeditor.data.model

import java.util.UUID

data class VideoTrack(
    val id: String = UUID.randomUUID().toString(),
    val clips: List<VideoClip> = emptyList(),
    val isMuted: Boolean = false,
    val volume: Float = 1.0f
)

data class AudioTrack(
    val id: String = UUID.randomUUID().toString(),
    val clips: List<AudioClip> = emptyList(),
    val volume: Float = 1.0f,
    val isMuted: Boolean = false
)
