package com.myvideo.editor.core.video

import com.myvideo.editor.core.video.model.TrackClip

class ClipDragEngine {
    private var dragClip: TrackClip? = null
    private var dragOffsetMs: Long = 0
    private var originalStartMs: Long = 0
    private var originalTrackIndex: Int = 0
    val snapEngine = SnapEngine()

    fun startDrag(clip: TrackClip, offsetMs: Long) {
        dragClip = clip; dragOffsetMs = offsetMs
        originalStartMs = clip.startMs; originalTrackIndex = clip.trackIndex
    }

    fun updateDrag(targetMs: Long, targetTrack: Int) {
        val clip = dragClip ?: return
        var newStart = (targetMs - dragOffsetMs).coerceAtLeast(0)
        newStart = snapEngine.snap(newStart)
        clip.startMs = newStart
        clip.endMs = newStart + clip.durationMs
        clip.trackIndex = targetTrack
    }

    fun endDrag(): TrackClip? { val c = dragClip; dragClip = null; return c }
    fun cancelDrag() { dragClip?.let { it.startMs = originalStartMs; it.endMs = originalStartMs + it.durationMs; it.trackIndex = originalTrackIndex }; dragClip = null }
    fun isDragging(): Boolean = dragClip != null
    fun getDragClip(): TrackClip? = dragClip
}
