package com.myvideo.editor.feature.tracking

import android.graphics.PointF

class AutoPlaneDetector {
    data class Plane(val corners: List<PointF>, val confidence: Float)

    fun detect(width: Int, height: Int): Plane {
        val margin = 0.1f
        return Plane(listOf(
            PointF(width * margin, height * margin),
            PointF(width * (1 - margin), height * margin),
            PointF(width * (1 - margin), height * (1 - margin)),
            PointF(width * margin, height * (1 - margin))
        ), 0.8f)
    }

    fun trackPlane(prev: Plane, dx: Float, dy: Float): Plane {
        val moved = prev.corners.map { PointF(it.x + dx, it.y + dy) }
        return Plane(moved, prev.confidence * 0.95f)
    }
}
