package com.myvideo.editor.feature.tracking

class KeyframeEditor {
    data class TrackingKeyframe(val frame: Int, val x: Float, val y: Float, val scale: Float = 1f, val rotation: Float = 0f)
    private val keyframes = mutableListOf<TrackingKeyframe>()

    fun add(kf: TrackingKeyframe) { keyframes.add(kf); keyframes.sortBy { it.frame } }
    fun remove(frame: Int) { keyframes.removeAll { it.frame == frame } }
    fun getKeyframes(): List<TrackingKeyframe> = keyframes.toList()
    fun getAt(frame: Int): TrackingKeyframe? {
        val b = keyframes.lastOrNull { it.frame <= frame } ?: return keyframes.firstOrNull()
        val a = keyframes.firstOrNull { it.frame > frame } ?: return b
        val t = (frame - b.frame).toFloat() / (a.frame - b.frame)
        return TrackingKeyframe(frame, b.x + (a.x - b.x) * t, b.y + (a.y - b.y) * t, b.scale + (a.scale - b.scale) * t, b.rotation + (a.rotation - b.rotation) * t)
    }
    fun clear() { keyframes.clear() }
}
