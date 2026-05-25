package com.myvideo.editor.core.vision

import android.graphics.PointF

class PlaneTracker {
    data class TrackResult(val points: List<PointF>, val homography: FloatArray?, val confidence: Float)

    external fun nativeTrack(prevPixels: IntArray, currPixels: IntArray, w: Int, h: Int): FloatArray

    fun track(prev: IntArray, curr: IntArray, w: Int, h: Int): TrackResult {
        return try {
            val result = nativeTrack(prev, curr, w, h)
            val points = mutableListOf<PointF>()
            for (i in result.indices step 2) points.add(PointF(result[i], result[i + 1]))
            TrackResult(points, result, 0.8f)
        } catch (e: Exception) { TrackResult(emptyList(), null, 0f) }
    }
}
