package com.myvideo.editor.feature.player

class FreezeFrameHelper {
    data class FreezePoint(val timeMs: Long, val durationMs: Long, val speed: Float = 0f)

    private val freezePoints = mutableListOf<FreezePoint>()

    fun addFreeze(timeMs: Long, durationMs: Long = 2000) { freezePoints.add(FreezePoint(timeMs, durationMs)) }
    fun removeFreeze(timeMs: Long) { freezePoints.removeAll { kotlin.math.abs(it.timeMs - timeMs) < 50 } }
    fun getFreezes(): List<FreezePoint> = freezePoints.toList()
    fun clear() { freezePoints.clear() }
    fun isFrozen(timeMs: Long): Boolean = freezePoints.any { timeMs in it.timeMs..(it.timeMs + it.durationMs) }
    fun getFreezeAt(timeMs: Long): FreezePoint? = freezePoints.find { timeMs in it.timeMs..(it.timeMs + it.durationMs) }
}
