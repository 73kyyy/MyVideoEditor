package com.videoeditor.data.model

import java.util.UUID

data class Project(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "未命名项目",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val width: Int = 1080,
    val height: Int = 1920,
    val frameRate: Int = 30,
    val videoTracks: List<VideoTrack> = emptyList(),
    val audioTracks: List<AudioTrack> = emptyList(),
    val textClips: List<TextClip> = emptyList(),
    val durationUs: Long = 0L
) {
    val durationMs: Long get() = durationUs / 1000
    val durationSec: Float get() = durationUs / 1_000_000f

    fun totalDurationUs(): Long {
        val videoMax = videoTracks.flatMap { it.clips }.maxOfOrNull { it.endUs } ?: 0L
        val audioMax = audioTracks.flatMap { it.clips }.maxOfOrNull { it.endUs } ?: 0L
        val textMax = textClips.maxOfOrNull { it.endUs } ?: 0L
        return maxOf(videoMax, audioMax, textMax)
    }
}
