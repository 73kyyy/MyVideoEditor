package com.myvideo.editor.feature.effects.phantom

import android.graphics.Bitmap

class MotionBlurEffect {
    data class Params(val angle: Float = 0f, val strength: Int = 10)

    fun apply(bitmap: Bitmap, params: Params): Bitmap {
        val w = bitmap.width; val h = bitmap.height
        val pixels = IntArray(w * h); bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        val result = IntArray(w * h)
        val rad = Math.toRadians(params.angle.toDouble())
        val dx = kotlin.math.cos(rad).toFloat(); val dy = kotlin.math.sin(rad).toFloat()
        for (y in 0 until h) for (x in 0 until w) {
            var r = 0f; var g = 0f; var b = 0f; var count = 0
            for (s in -params.strength..params.strength) {
                val sx = (x + dx * s).toInt().coerceIn(0, w - 1)
                val sy = (y + dy * s).toInt().coerceIn(0, h - 1)
                val p = pixels[sy * w + sx]
                r += (p shr 16 and 0xFF); g += (p shr 8 and 0xFF); b += (p and 0xFF); count++
            }
            result[y * w + x] = 0xFF000000.toInt() or ((r/count).toInt().coerceIn(0,255) shl 16) or ((g/count).toInt().coerceIn(0,255) shl 8) or (b/count).toInt().coerceIn(0,255)
        }
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bmp.setPixels(result, 0, w, 0, 0, w, h)
        return bmp
    }

    fun applyRadial(bitmap: Bitmap, centerX: Float, centerY: Float, strength: Int): Bitmap {
        val w = bitmap.width; val h = bitmap.height
        val pixels = IntArray(w * h); bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        val result = IntArray(w * h)
        for (y in 0 until h) for (x in 0 until w) {
            val dx = x - centerX; val dy = y - centerY
            val dist = kotlin.math.sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
            val ndx = dx / dist; val ndy = dy / dist
            var r = 0f; var g = 0f; var b = 0f; var count = 0
            for (s in 0..strength) {
                val sx = (x + ndx * s).toInt().coerceIn(0, w - 1)
                val sy = (y + ndy * s).toInt().coerceIn(0, h - 1)
                val p = pixels[sy * w + sx]
                r += (p shr 16 and 0xFF); g += (p shr 8 and 0xFF); b += (p and 0xFF); count++
            }
            result[y * w + x] = 0xFF000000.toInt() or ((r/count).toInt().coerceIn(0,255) shl 16) or ((g/count).toInt().coerceIn(0,255) shl 8) or (b/count).toInt().coerceIn(0,255)
        }
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bmp.setPixels(result, 0, w, 0, 0, w, h)
        return bmp
    }
}
