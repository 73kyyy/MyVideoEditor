package com.myvideo.editor.core.vision

import android.graphics.PointF
import android.graphics.RectF

class MultiRegionTracker {
    data class Region(val id: String, val rect: RectF, var confidence: Float = 1f)

    private val regions = mutableListOf<Region>()

    fun addRegion(id: String, rect: RectF) { regions.add(Region(id, rect)) }
    fun removeRegion(id: String) { regions.removeAll { it.id == id } }
    fun getRegions(): List<Region> = regions.toList()

    fun track(prevPixels: IntArray, currPixels: IntArray, w: Int, h: Int): List<Region> {
        regions.forEach { region ->
            val cx = region.rect.centerX(); val cy = region.rect.centerY()
            region.confidence *= 0.95f
        }
        return regions.filter { it.confidence > 0.3f }
    }

    fun clear() { regions.clear() }
}
