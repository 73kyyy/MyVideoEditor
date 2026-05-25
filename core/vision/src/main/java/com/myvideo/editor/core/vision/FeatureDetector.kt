package com.myvideo.editor.core.vision

import android.graphics.PointF

class FeatureDetector {
    data class Feature(val point: PointF, val response: Float, val octave: Int = 0)

    fun detectFast(pixels: IntArray, w: Int, h: Int, threshold: Int = 20): List<Feature> {
        val features = mutableListOf<Feature>()
        for (y in 3 until h - 3) for (x in 3 until w - 3) {
            val center = brightness(pixels[y * w + x])
            val top = brightness(pixels[(y - 3) * w + x])
            val bottom = brightness(pixels[(y + 3) * w + x])
            val left = brightness(pixels[y * w + (x - 3)])
            val right = brightness(pixels[y * w + (x + 3)])
            if (kotlin.math.abs(center - top) > threshold && kotlin.math.abs(center - bottom) > threshold &&
                kotlin.math.abs(center - left) > threshold && kotlin.math.abs(center - right) > threshold) {
                features.add(Feature(PointF(x.toFloat(), y.toFloat()), (kotlin.math.abs(center - top) + kotlin.math.abs(center - bottom)).toFloat()))
            }
        }
        return features.sortedByDescending { it.response }.take(500)
    }

    fun detectCorners(pixels: IntArray, w: Int, h: Int): List<Feature> {
        val features = mutableListOf<Feature>()
        for (y in 1 until h - 1) for (x in 1 until w - 1) {
            val dx = brightness(pixels[y * w + x + 1]) - brightness(pixels[y * w + x - 1])
            val dy = brightness(pixels[(y + 1) * w + x]) - brightness(pixels[(y - 1) * w + x])
            val mag = kotlin.math.sqrt((dx * dx + dy * dy).toFloat())
            if (mag > 50) features.add(Feature(PointF(x.toFloat(), y.toFloat()), mag))
        }
        return features.sortedByDescending { it.response }.take(300)
    }

    private fun brightness(pixel: Int): Int = ((pixel shr 16 and 0xFF) + (pixel shr 8 and 0xFF) + (pixel and 0xFF)) / 3
}
