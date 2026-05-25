package com.myvideo.editor.core.video.model

data class TimeRemapKeyframe(
    val compositionTimeMs: Long, val sourceTimeMs: Long,
    val interpolation: Interpolation = Interpolation.Linear
)

class TimeRemapper {
    private val keyframes = mutableListOf<TimeRemapKeyframe>()
    fun add(kf: TimeRemapKeyframe) { keyframes.add(kf); keyframes.sortBy { it.compositionTimeMs } }
    fun remove(t: Long) { keyframes.removeAll { kotlin.math.abs(it.compositionTimeMs - t) < 100 } }
    fun getSourceTime(compMs: Long): Long {
        if (keyframes.isEmpty()) return compMs
        if (keyframes.size == 1) return keyframes[0].sourceTimeMs
        val b = keyframes.lastOrNull { it.compositionTimeMs <= compMs } ?: keyframes.first()
        val a = keyframes.firstOrNull { it.compositionTimeMs > compMs } ?: keyframes.last()
        if (b.compositionTimeMs == a.compositionTimeMs) return b.sourceTimeMs
        val v = KeyframeInterpolator.interpolate(
            Keyframe(b.compositionTimeMs, "", b.sourceTimeMs.toFloat(), b.interpolation),
            Keyframe(a.compositionTimeMs, "", a.sourceTimeMs.toFloat(), a.interpolation), compMs
        )
        return v.toLong()
    }
    fun getKeyframes(): List<TimeRemapKeyframe> = keyframes.toList()
    fun clear() { keyframes.clear() }
}
