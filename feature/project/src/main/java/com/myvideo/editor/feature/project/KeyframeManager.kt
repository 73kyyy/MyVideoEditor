package com.myvideo.editor.feature.project

class KeyframeManager {
    data class KF(val clipId: String, val timeMs: Long, val property: String, val value: Float)
    private val keyframes = mutableListOf<KF>()

    fun add(kf: KF) { keyframes.add(kf); keyframes.sortBy { it.timeMs } }
    fun remove(clipId: String, property: String, timeMs: Long) { keyframes.removeAll { it.clipId == clipId && it.property == property && kotlin.math.abs(it.timeMs - timeMs) < 50 } }
    fun getForClip(clipId: String): List<KF> = keyframes.filter { it.clipId == clipId }
    fun getForProperty(clipId: String, property: String): List<KF> = keyframes.filter { it.clipId == clipId && it.property == property }
    fun getValueAt(clipId: String, property: String, timeMs: Long): Float? {
        val kfs = getForProperty(clipId, property)
        if (kfs.isEmpty()) return null
        val b = kfs.lastOrNull { it.timeMs <= timeMs } ?: kfs.first()
        val a = kfs.firstOrNull { it.timeMs > timeMs } ?: kfs.last()
        if (b.timeMs == a.timeMs) return b.value
        val t = (timeMs - b.timeMs).toFloat() / (a.timeMs - b.timeMs)
        return b.value + (a.value - b.value) * t
    }
    fun clear() { keyframes.clear() }
}
