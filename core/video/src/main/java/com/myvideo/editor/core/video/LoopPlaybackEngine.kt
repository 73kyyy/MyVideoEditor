package com.myvideo.editor.core.video

class LoopPlaybackEngine {
    var isLooping = false
    var loopStartMs: Long = 0
    var loopEndMs: Long = 0

    fun setLoop(startMs: Long, endMs: Long) { loopStartMs = startMs; loopEndMs = endMs }
    fun enableLoop() { isLooping = true }
    fun disableLoop() { isLooping = false }
    fun toggleLoop() { isLooping = !isLooping }

    fun checkLoop(currentMs: Long): Long {
        if (!isLooping || loopEndMs <= loopStartMs) return currentMs
        return if (currentMs >= loopEndMs) loopStartMs else currentMs
    }

    fun setLoopFromInPoint(inMs: Long) { loopStartMs = inMs }
    fun setLoopFromOutPoint(outMs: Long) { loopEndMs = outMs }
    fun setLoopFromSelection(startMs: Long, endMs: Long) { loopStartMs = startMs; loopEndMs = endMs }
    fun getLoopDuration(): Long = loopEndMs - loopStartMs
}
