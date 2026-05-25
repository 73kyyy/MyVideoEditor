package com.myvideo.editor.core.video

class FrameNavigator(private val fps: Int = 30) {
    private var currentPositionMs: Long = 0
    private var durationMs: Long = 0
    private val frameMs: Long get() = (1000f / fps).toLong()

    fun setDuration(ms: Long) { durationMs = ms }
    fun setPosition(ms: Long) { currentPositionMs = ms.coerceIn(0, durationMs) }
    fun getPosition(): Long = currentPositionMs
    fun getFrame(): Int = (currentPositionMs / frameMs).toInt()

    fun nextFrame(): Long { currentPositionMs = (currentPositionMs + frameMs).coerceAtMost(durationMs); return currentPositionMs }
    fun prevFrame(): Long { currentPositionMs = (currentPositionMs - frameMs).coerceAtLeast(0); return currentPositionMs }
    fun nextNFrames(n: Int): Long { currentPositionMs = (currentPositionMs + frameMs * n).coerceAtMost(durationMs); return currentPositionMs }
    fun prevNFrames(n: Int): Long { currentPositionMs = (currentPositionMs - frameMs * n).coerceAtLeast(0); return currentPositionMs }
    fun goToTime(ms: Long): Long { currentPositionMs = ms.coerceIn(0, durationMs); return currentPositionMs }
    fun goToFrame(frame: Int): Long { currentPositionMs = (frame * frameMs).coerceIn(0, durationMs); return currentPositionMs }
    fun getTotalFrames(): Int = (durationMs / frameMs).toInt()
    fun isAtStart(): Boolean = currentPositionMs <= 0
    fun isAtEnd(): Boolean = currentPositionMs >= durationMs
}
