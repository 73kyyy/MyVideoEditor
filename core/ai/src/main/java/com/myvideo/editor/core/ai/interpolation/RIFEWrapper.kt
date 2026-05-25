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
        val p1 = IntArray(w * h); frame1.getPixels(p1, 0, w, 0, 0, w, h)
        val p2 = IntArray(w * h); frame2.getPixels(p2, 0, w, 0, 0, w, h)
        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val out = IntArray(w * h) { i ->
            val r = ((p1[i] shr 16 and 0xFF) * (1 - t) + (p2[i] shr 16 and 0xFF) * t).toInt().coerceIn(0, 255)
            val g = ((p1[i] shr 8 and 0xFF) * (1 - t) + (p2[i] shr 8 and 0xFF) * t).toInt().coerceIn(0, 255)
            val b = ((p1[i] and 0xFF) * (1 - t) + (p2[i] and 0xFF) * t).toInt().coerceIn(0, 255)
            0xFF000000.toInt() or (r shl 16) or (g shl 8) or b
        }
        result.setPixels(out, 0, w, 0, 0, w, h)
        return result
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

    fun release() { sessionManager.release(modelId); isReady = false }
}
