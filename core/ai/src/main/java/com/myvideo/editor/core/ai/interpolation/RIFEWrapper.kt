package com.myvideo.editor.core.ai.interpolation

import android.graphics.Bitmap
import com.myvideo.editor.core.ai.common.InferenceSessionManager

class RIFEWrapper(private val sessionManager: InferenceSessionManager) {
    private var modelId = "rife"
    var isReady = false; private set

    fun init(modelPath: String): Boolean {
        isReady = sessionManager.loadModel(modelPath, modelId)
        return isReady
    }

    fun interpolate(frame1: Bitmap, frame2: Bitmap, t: Float = 0.5f): Bitmap? {
        if (!isReady) return null
        val w = frame1.width; val h = frame1.height
        val img0 = bitmapToFloatArray(frame1)
        val img1 = bitmapToFloatArray(frame2)
        val timestep = floatArrayOf(t)
        val shape = longArrayOf(1, 3, h.toLong(), w.toLong())
        val result = sessionManager.runMulti(modelId, mapOf(
            "img0" to (img0 to shape),
            "img1" to (img1 to shape),
            "timestep" to (timestep to longArrayOf(1))
        ))
        return result?.let { floatArrayToBitmap(it, w, h) }
    }

    fun interpolate2x(frames: List<Bitmap>): List<Bitmap> {
        if (frames.size < 2) return frames
        val result = mutableListOf<Bitmap>()
        for (i in 0 until frames.size - 1) {
            result.add(frames[i])
            interpolate(frames[i], frames[i + 1])?.let { result.add(it) }
        }
        result.add(frames.last())
        return result
    }

    fun slowMotion4x(frames: List<Bitmap>): List<Bitmap> {
        if (frames.size < 2) return frames
        val result = mutableListOf<Bitmap>()
        for (i in 0 until frames.size - 1) {
            result.add(frames[i])
            for (t in listOf(0.25f, 0.5f, 0.75f)) {
                interpolate(frames[i], frames[i + 1], t)?.let { result.add(it) }
            }
        }
        result.add(frames.last())
        return result
    }

    private fun bitmapToFloatArray(bitmap: Bitmap): FloatArray {
        val w = bitmap.width; val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        val data = FloatArray(3 * w * h)
        for (i in pixels.indices) {
            data[i] = (pixels[i] shr 16 and 0xFF) / 255f          // R
            data[w * h + i] = (pixels[i] shr 8 and 0xFF) / 255f   // G
            data[2 * w * h + i] = (pixels[i] and 0xFF) / 255f     // B
        }
        return data
    }

    private fun floatArrayToBitmap(data: FloatArray, w: Int, h: Int): Bitmap {
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
