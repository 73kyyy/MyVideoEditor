package com.myvideo.editor.core.video

import com.myvideo.editor.core.video.model.MediaTrack

class RippleEditEngine {
    var isEnabled = true

    fun onClipInserted(track: MediaTrack, insertAtMs: Long, durationMs: Long) {
        if (!isEnabled) return
        track.clips.filter { it.startMs >= insertAtMs }.forEach {
            it.startMs += durationMs; it.endMs += durationMs
        }
    }

    fun onClipRemoved(track: MediaTrack, removedStartMs: Long, removedDurationMs: Long) {
        if (!isEnabled) return
        track.clips.filter { it.startMs > removedStartMs }.forEach {
            it.startMs -= removedDurationMs; it.endMs -= removedDurationMs
        }
    }

    fun onClipTrimmed(track: MediaTrack, clipId: String, trimAmountMs: Long) {
        if (!isEnabled) return
        val clip = track.clips.find { it.id == clipId } ?: return
        track.clips.filter { it.startMs > clip.endMs && it.id != clipId }.forEach {
            it.startMs -= trimAmountMs; it.endMs -= trimAmountMs
        }
    }

    fun onClipSplit(track: MediaTrack, clipId: String, splitAtMs: Long) {
        if (!isEnabled) return
        val clip = track.clips.find { it.id == clipId } ?: return
        val newClip = clip.copy(id = "${clip.id}_split", startMs = splitAtMs, endMs = clip.endMs)
        clip.endMs = splitAtMs
        val idx = track.clips.indexOf(clip)
        track.clips.add(idx + 1, newClip)
    }
}
