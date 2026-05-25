package com.myvideo.editor.feature.player

class QuickCutHelper {
    data class CutPoint(val timeMs: Long, val type: CutType)
    enum class CutType { Split, Delete, Keep }

    private val cutPoints = mutableListOf<CutPoint>()

    fun addCut(timeMs: Long, type: CutType = CutType.Split) { cutPoints.add(CutPoint(timeMs, type)); cutPoints.sortBy { it.timeMs } }
    fun removeCut(timeMs: Long) { cutPoints.removeAll { kotlin.math.abs(it.timeMs - timeMs) < 50 } }
    fun getCuts(): List<CutPoint> = cutPoints.toList()
    fun clear() { cutPoints.clear() }

    fun splitAt(timeMs: Long): Pair<Long, Long>? {
        val nearest = cutPoints.minByOrNull { kotlin.math.abs(it.timeMs - timeMs) } ?: return null
        return if (kotlin.math.abs(nearest.timeMs - timeMs) < 500) Pair(nearest.timeMs, nearest.timeMs) else null
    }

    fun quickCut(startMs: Long, endMs: Long): Pair<Long, Long> = Pair(startMs, endMs)
}
