package com.myvideo.editor.feature.effects.mask

import com.myvideo.editor.core.video.model.MaskData
import com.myvideo.editor.core.video.model.MaskPoint

class MaskRenderer {
    enum class BlendMode { Add, Subtract, Intersect, Difference }

    fun renderMask(width: Int, height: Int, mask: MaskData): FloatArray {
        val result = FloatArray(width * height) { if (mask.invert) 1f else 0f }
        for (y in 0 until height) for (x in 0 until width) {
            if (isInsidePolygon(x.toFloat(), y.toFloat(), mask.points, mask.isClosed)) {
                val edgeDist = distanceToEdge(x.toFloat(), y.toFloat(), mask.points, mask.isClosed)
                val feather = if (mask.feather > 0) (1f - (edgeDist / mask.feather).coerceIn(0f, 1f)) * mask.opacity else mask.opacity
                result[y * width + x] = if (mask.invert) 1f - feather else feather
            }
        }
        return result
    }

    fun applyMask(pixels: IntArray, mask: FloatArray): IntArray {
        return IntArray(pixels.size) { i ->
            val a = (mask[i] * 255).toInt().coerceIn(0, 255)
            (pixels[i] and 0x00FFFFFF) or (a shl 24)
        }
    }

    fun combineMasks(mask1: FloatArray, mask2: FloatArray, mode: BlendMode): FloatArray {
        return FloatArray(mask1.size) { i ->
            when (mode) {
                BlendMode.Add -> (mask1[i] + mask2[i]).coerceIn(0f, 1f)
                BlendMode.Subtract -> (mask1[i] - mask2[i]).coerceIn(0f, 1f)
                BlendMode.Intersect -> mask1[i] * mask2[i]
                BlendMode.Difference -> kotlin.math.abs(mask1[i] - mask2[i])
            }
        }
    }

    private fun isInsidePolygon(x: Float, y: Float, points: List<MaskPoint>, closed: Boolean): Boolean {
        if (points.size < 3) return false
        var inside = false; var j = points.size - 1
        for (i in points.indices) {
            val pi = points[i]; val pj = points[j]
            if ((pi.y > y) != (pj.y > y) && x < (pj.x - pi.x) * (y - pi.y) / (pj.y - pi.y) + pi.x) inside = !inside
            j = i
        }
        return inside
    }

    private fun distanceToEdge(x: Float, y: Float, points: List<MaskPoint>, closed: Boolean): Float {
        var minDist = Float.MAX_VALUE
        for (i in 0 until points.size - (if (closed) 0 else 1)) {
            val p1 = points[i]; val p2 = points[(i + 1) % points.size]
            val dist = pointToSegmentDist(x, y, p1.x, p1.y, p2.x, p2.y)
            if (dist < minDist) minDist = dist
        }
        return minDist
    }

    private fun pointToSegmentDist(px: Float, py: Float, x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x2 - x1; val dy = y2 - y1; val lenSq = dx * dx + dy * dy
        if (lenSq == 0f) return kotlin.math.sqrt((px-x1)*(px-x1)+(py-y1)*(py-y1))
        val t = (((px-x1)*dx+(py-y1)*dy)/lenSq).coerceIn(0f,1f)
        return kotlin.math.sqrt((px-(x1+t*dx))*(px-(x1+t*dx))+(py-(y1+t*dy))*(py-(y1+t*dy)))
    }
}
