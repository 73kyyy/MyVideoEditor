package com.myvideo.editor.core.ai.legacy

import android.graphics.Bitmap

class SharpenFilter {
    fun apply(bitmap: Bitmap, strength: Float = 1f): Bitmap {
        val w = bitmap.width; val h = bitmap.height
        val pixels = IntArray(w * h); bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        val result = IntArray(w * h)
        val s = strength.coerceIn(0f, 3f)
        val kernel = floatArrayOf(0f, -s, 0f, -s, 1f+4f*s, -s, 0f, -s, 0f)
        for (y in 1 until h - 1) for (x in 1 until w - 1) {
            var r = 0f; var g = 0f; var b = 0f
            for (ky in -1..1) for (kx in -1..1) {
                val p = pixels[(y+ky)*w+(x+kx)]; val k = kernel[(ky+1)*3+(kx+1)]
                r += (p shr 16 and 0xFF) * k; g += (p shr 8 and 0xFF) * k; b += (p and 0xFF) * k
            }
            result[y*w+x] = 0xFF000000.toInt() or (r.toInt().coerceIn(0,255) shl 16) or (g.toInt().coerceIn(0,255) shl 8) or b.toInt().coerceIn(0,255)
        }
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bmp.setPixels(result, 0, w, 0, 0, w, h)
        return bmp
    }
}
