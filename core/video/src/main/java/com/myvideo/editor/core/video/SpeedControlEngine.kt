package com.myvideo.editor.core.video

import com.myvideo.editor.core.video.model.SpeedCurve
import com.myvideo.editor.core.video.model.SpeedPoint
import com.myvideo.editor.core.video.model.TrackClip

class SpeedControlEngine {
    private val speedCurves = mutableMapOf<String, SpeedCurve>()

    fun setSpeed(clip: TrackClip, speed: Float) { clip.speed = speed.coerceIn(0.1f, 16f) }
    fun getSpeed(clip: TrackClip): Float = clip.speed

    fun setSpeedCurve(clipId: String) { speedCurves[clipId] = SpeedCurve() }
    fun addSpeedPoint(clipId: String, timeMs: Long, speed: Float) {
        speedCurves.getOrPut(clipId) { SpeedCurve() }.add(SpeedPoint(timeMs, speed))
    }
    fun getSpeedAt(clipId: String, timeMs: Long): Float = speedCurves[clipId]?.getSpeedAt(timeMs) ?: 1f
    fun clearSpeedCurve(clipId: String) { speedCurves.remove(clipId) }

    fun getOutputDuration(clip: TrackClip): Long = (clip.durationMs / clip.speed).toLong()

    fun freezeFrame(clip: TrackClip, atMs: Long, durationMs: Long): Pair<Long, Long> {
        return Pair(atMs, atMs + durationMs)
    }
}
