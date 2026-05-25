package com.myvideo.editor.core.video.model

object KeyframeInterpolator {
    fun interpolate(kf1: Keyframe, kf2: Keyframe, timeMs: Long): Float {
        val t = ((timeMs - kf1.timeMs).toFloat() / (kf2.timeMs - kf1.timeMs)).coerceIn(0f, 1f)
        val e = when (kf2.interpolation) {
            Interpolation.Linear -> t
            Interpolation.EaseIn -> t * t
            Interpolation.EaseOut -> 1f - (1f - t) * (1f - t)
            Interpolation.EaseInOut -> if (t < 0.5f) 2f * t * t else 1f - (-2f * t + 2f).let { it * it } / 2f
            Interpolation.Bezier -> t * t * (3f - 2f * t)
            Interpolation.Hold -> if (t >= 1f) 1f else 0f
        }
        return kf1.value + (kf2.value - kf1.value) * e
    }

    fun interpolateList(kfs: List<Keyframe>, timeMs: Long): Float {
        if (kfs.isEmpty()) return 0f
        if (kfs.size == 1) return kfs[0].value
        val b = kfs.lastOrNull { it.timeMs <= timeMs } ?: kfs.first()
        val a = kfs.firstOrNull { it.timeMs > timeMs } ?: kfs.last()
        return if (b.timeMs == a.timeMs) b.value else interpolate(b, a, timeMs)
    }
}
