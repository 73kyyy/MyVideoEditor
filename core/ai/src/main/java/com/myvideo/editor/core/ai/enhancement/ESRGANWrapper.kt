package com.myvideo.editor.core.ai.enhancement

import android.graphics.Bitmap
import com.myvideo.editor.core.ai.common.InferenceSessionManager

class ESRGANWrapper(private val sessionManager: InferenceSessionManager) {
    private var modelId = "esrgan"
    var isReady = false; private set

    fun init(modelPath: String): Boolean {
        isReady = sessionManager.loadModel(modelPath, modelId)
        return isReady
    }

    fun upscale4x(bitmap: Bitmap): Bitmap? {
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
        val result = sessionManager.run(modelId, "input", input, longArrayOf(1, 3, h.toLong(), w.toLong()))
        return result?.let { outputToBitmap(it, w * 4, h * 4) }
    }

    fun upscale2x(bitmap: Bitmap): Bitmap? {
        val x4 = upscale4x(bitmap) ?: return null
        return Bitmap.createScaledBitmap(x4, bitmap.width * 2, bitmap.height * 2, true)
    }

    private fun outputToBitmap(data: FloatArray, w: Int, h: Int): Bitmap {
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(w * h)
        val size = w * h
        for (i in 0 until size) {
            val r = (data[i].coerceIn(0f, 1f) * 255).toInt()
            val g = (data[size + i].coerceIn(0f, 1f) * 255).toInt()
            val b = (data[2 * size + i].coerceIn(0f, 1f) * 255).toInt()
            pixels[i] = 0xFF000000.toInt() or (r shl 16) or (g shl 8) or b
        }
        bmp.setPixels(pixels, 0, w, 0, 0, w, h)
        return bmp
    }

    fun release() { sessionManager.release(modelId); isReady = false }
}
