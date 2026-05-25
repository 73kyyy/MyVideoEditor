package com.myvideo.editor.core.video

import com.myvideo.editor.core.video.model.Marker
import com.myvideo.editor.core.video.model.MediaTrack

class SnapEngine {
    var snapEnabled = true
    var snapThresholdMs: Long = 500
    private val snapPoints = mutableListOf<Long>()

    fun updateSnapPoints(tracks: List<MediaTrack>, markers: List<Marker>) {
        snapPoints.clear()
        tracks.forEach { track ->
            track.clips.forEach { clip ->
                snapPoints.add(clip.startMs)
                snapPoints.add(clip.endMs)
                snapPoints.add(clip.startMs + clip.durationMs / 2)
            }
        }
        markers.forEach { snapPoints.add(it.timeMs) }
    }

    fun snap(timeMs: Long): Long {
        if (!snapEnabled) return timeMs
        val nearest = snapPoints.minByOrNull { kotlin.math.abs(it - timeMs) } ?: return timeMs
        return if (kotlin.math.abs(nearest - timeMs) <= snapThresholdMs) nearest else timeMs
    }

    fun findNearestSnap(timeMs: Long): Long? {
        val nearest = snapPoints.minByOrNull { kotlin.math.abs(it - timeMs) } ?: return null
        return if (kotlin.math.abs(nearest - timeMs) <= snapThresholdMs) nearest else null
    }
}
