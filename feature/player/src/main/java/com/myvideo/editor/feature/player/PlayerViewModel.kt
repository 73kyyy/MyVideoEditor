package com.myvideo.editor.feature.player

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class PlayerViewModel {
    var isPlaying by mutableStateOf(false); private set
    var currentPositionMs by mutableStateOf(0L); private set
    var durationMs by mutableStateOf(0L); private set
    var volume by mutableStateOf(1f)
    var playbackSpeed by mutableStateOf(1f)

    fun play() { isPlaying = true }
    fun pause() { isPlaying = false }
    fun toggle() { isPlaying = !isPlaying }
    fun seekTo(ms: Long) { currentPositionMs = ms.coerceIn(0, durationMs) }
    fun setDuration(ms: Long) { durationMs = ms }
    fun applyVolume(v: Float) { volume = v.coerceIn(0f, 1f) }
    fun applySpeed(s: Float) { playbackSpeed = s.coerceIn(0.25f, 4f) }
    fun updatePosition(ms: Long) { currentPositionMs = ms }
}
