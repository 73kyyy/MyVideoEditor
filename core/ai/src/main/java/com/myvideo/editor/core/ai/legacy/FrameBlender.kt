package com.myvideo.editor.core.ai.legacy

import android.graphics.Bitmap

class FrameBlender {
    fun blend(frame1: Bitmap, frame2: Bitmap, opacity: Float = 0.5f): Bitmap? {
        if (frame1.width != frame2.width || frame1.height != frame2.height) return null
        val w = frame1.width; val h = frame1.height
        val p1 = IntArray(w*h); frame1.getPixels(p1, 0, w, 0, 0, w, h)
        val p2 = IntArray(w*h); frame2.getPixels(p2, 0, w, 0, 0, w, h)
        val o = opacity.coerceIn(0f, 1f)
        val result = IntArray(w*h) { i ->
            val r = ((p1[i] shr 16 and 0xFF)*(1-o)+(p2[i] shr 16 and 0xFF)*o).toInt().coerceIn(0,255)
            val g = ((p1[i] shr 8 and 0xFF)*(1-o)+(p2[i] shr 8 and 0xFF)*o).toInt().coerceIn(0,255)
            val b = ((p1[i] and 0xFF)*(1-o)+(p2[i] and 0xFF)*o).toInt().coerceIn(0,255)
            0xFF000000.toInt() or (r shl 16) or (g shl 8) or b
        }
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bmp.setPixels(result, 0, w, 0, 0, w, h)
        return bmp
    }

    fun overlay(base: Bitmap, overlay: Bitmap, x: Int, y: Int, opacity: Float = 1f): Bitmap {
        val result = base.copy(Bitmap.Config.ARGB_8888, true)
        val ow = overlay.width; val oh = overlay.height
        val op = IntArray(ow*oh); overlay.getPixels(op, 0, ow, 0, 0, ow, oh)
        val bp = IntArray(base.width*base.height); result.getPixels(bp, 0, base.width, 0, 0, base.width, base.height)
        for (oy in 0 until oh) for (ox in 0 until ow) {
            val dx = x+ox; val dy = y+oy
            if (dx in 0 until base.width && dy in 0 until base.height) {
                val src = op[oy*ow+ox]; val sa = (src shr 24 and 0xFF)/255f*opacity
                val di = dy*base.width+dx; val dst = bp[di]
                val r = ((dst shr 16 and 0xFF)*(1-sa)+(src shr 16 and 0xFF)*sa).toInt().coerceIn(0,255)
                val g = ((dst shr 8 and 0xFF)*(1-sa)+(src shr 8 and 0xFF)*sa).toInt().coerceIn(0,255)
                val b = ((dst and 0xFF)*(1-sa)+(src and 0xFF)*sa).toInt().coerceIn(0,255)
                bp[di] = 0xFF000000.toInt() or (r shl 16) or (g shl 8) or b
            }
        }
        result.setPixels(bp, 0, base.width, 0, 0, base.width, base.height)
        return result
    }
}
