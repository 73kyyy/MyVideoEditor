package com.myvideo.editor.core.video.model

data class SpeedPoint(val timeMs: Long, val speed: Float)

class SpeedCurve {
    private val points = mutableListOf<SpeedPoint>()
    fun add(p: SpeedPoint) { points.add(p); points.sortBy { it.timeMs } }
    fun remove(timeMs: Long) { points.removeAll { kotlin.math.abs(it.timeMs - timeMs) < 100 } }
    fun getSpeedAt(timeMs: Long): Float {
        if (points.isEmpty()) return 1f
        if (points.size == 1) return points[0].speed
        val b = points.lastOrNull { it.timeMs <= timeMs } ?: points.first()
        val a = points.firstOrNull { it.timeMs > timeMs } ?: points.last()
        if (b.timeMs == a.timeMs) return b.speed
        val t = (timeMs - b.timeMs).toFloat() / (a.timeMs - b.timeMs)
        return b.speed + (a.speed - b.speed) * t
    }
    fun getPoints(): List<SpeedPoint> = points.toList()
    fun clear() { points.clear() }
}
