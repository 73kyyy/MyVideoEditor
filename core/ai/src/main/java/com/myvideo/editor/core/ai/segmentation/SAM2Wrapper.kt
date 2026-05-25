package com.myvideo.editor.core.ai.segmentation

import android.graphics.Bitmap
import android.graphics.PointF
import com.myvideo.editor.core.ai.common.InferenceSessionManager

class SAM2Wrapper(private val sessionManager: InferenceSessionManager) {
    private var modelId = "sam2"
    var isReady = false; private set

    fun init(modelPath: String): Boolean {
        isReady = sessionManager.loadModel(modelPath, modelId)
        return isReady
    }

    fun segment(bitmap: Bitmap, point: PointF): Bitmap? {
        if (!isReady) return null
        val w = bitmap.width; val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        val input = FloatArray(3 * w * h)
        for (i in pixels.indices) {
            input[i] = (pixels[i] shr 16 and 0xFF) / 255f
            input[w * h + i] = (pixels[i] shr 8 and 0xFF) / 255f
            input[2 * w * h + i] = (pixels[i] and 0xFF) / 255f
        }
        val result = sessionManager.run(modelId, "images", input, longArrayOf(1, 3, h.toLong(), w.toLong()))
        return result?.let { maskToBitmap(it, w, h) }
    }

    fun segmentWithBox(bitmap: Bitmap, x1: Float, y1: Float, x2: Float, y2: Float): Bitmap? {
        return segment(bitmap, PointF((x1 + x2) / 2, (y1 + y2) / 2))
    }

    private fun maskToBitmap(mask: FloatArray, w: Int, h: Int): Bitmap {
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(w * h) { i ->
            val v = (mask[i] * 255).toInt().coerceIn(0, 255)
            (v shl 24) or 0x00FFFFFF
        }
        bmp.setPixels(pixels, 0, w, 0, 0, w, h)
        return bmp
    }

    fun release() { sessionManager.release(modelId); isReady = false }
}
