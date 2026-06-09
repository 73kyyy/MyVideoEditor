package com.myvideo.editor.core.ai.segmentation

import android.graphics.Bitmap
import android.graphics.PointF
import com.myvideo.editor.core.ai.common.InferenceSessionManager
import java.io.File

class SAM2Wrapper(private val sessionManager: InferenceSessionManager) {
    private var encoderModelId = "sam_encoder"
    private var decoderModelId = "sam_decoder"
    private var cachedEmbeddings: FloatArray? = null
    private var cachedBitmapHash: Int = 0
    var isReady = false; private set

    fun init(encoderPath: String, decoderPath: String): Boolean {
        val encOk = sessionManager.loadModel(encoderPath, encoderModelId)
        val decOk = sessionManager.loadModel(decoderPath, decoderModelId)
        isReady = encOk && decOk
        return isReady
    }

    fun init(modelPath: String): Boolean {
        val dir = File(modelPath).parentFile ?: return false
        val encPath = File(dir, "sam_encoder.onnx").absolutePath
        val decPath = File(dir, "sam_decoder.onnx").absolutePath
        return init(encPath, decPath)
    }

    fun segment(bitmap: Bitmap, point: PointF): Bitmap? {
        if (!isReady) return null
        val w = bitmap.width; val h = bitmap.height

        // Step 1: Get image embeddings (cache for same bitmap)
        val embeddings = getEmbeddings(bitmap) ?: return null

        // Step 2: Run decoder with point prompt
        val pointCoords = floatArrayOf(point.x, point.y)
        val pointLabels = floatArrayOf(1f)  // 1 = foreground point

        val result = sessionManager.runMulti(decoderModelId, mapOf(
            "image_embeddings" to (embeddings to longArrayOf(1, 256, 64, 64)),
            "point_coords" to (pointCoords to longArrayOf(1, 1, 2)),
            "point_labels" to (pointLabels to longArrayOf(1, 1))
        ))

        return result?.let { maskToBitmap(it, w, h) }
    }

    fun segmentWithBox(bitmap: Bitmap, x1: Float, y1: Float, x2: Float, y2: Float): Bitmap? {
        if (!isReady) return null
        val w = bitmap.width; val h = bitmap.height
        val embeddings = getEmbeddings(bitmap) ?: return null

        // Box prompt: two points (top-left and bottom-right)
        val pointCoords = floatArrayOf(x1, y1, x2, y2)
        val pointLabels = floatArrayOf(2f, 3f)  // 2=top-left, 3=bottom-right

        val result = sessionManager.runMulti(decoderModelId, mapOf(
            "image_embeddings" to (embeddings to longArrayOf(1, 256, 64, 64)),
            "point_coords" to (pointCoords to longArrayOf(1, 2, 2)),
            "point_labels" to (pointLabels to longArrayOf(1, 2))
        ))

        return result?.let { maskToBitmap(it, w, h) }
    }

    private fun getEmbeddings(bitmap: Bitmap): FloatArray? {
        val hash = bitmap.hashCode()
        if (cachedEmbeddings != null && cachedBitmapHash == hash) {
            return cachedEmbeddings
        }

        // Resize to 1024x1024 as expected by MobileSAM encoder
        val resized = Bitmap.createScaledBitmap(bitmap, 1024, 1024, true)
        val input = bitmapToFloatArray(resized)
        resized.recycle()

        val embeddings = sessionManager.run(
            encoderModelId, "input_image", input, longArrayOf(1, 3, 1024, 1024)
        )
        if (embeddings != null) {
            cachedEmbeddings = embeddings
            cachedBitmapHash = hash
        }
        return embeddings
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

    private fun maskToBitmap(mask: FloatArray, w: Int, h: Int): Bitmap {
        // Mask output is 256x256, resize to original image size
        val maskSize = 256
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            for (x in 0 until w) {
                val mx = x * maskSize / w
                val my = y * maskSize / h
                val idx = my * maskSize + mx
                val v = if (idx < mask.size && mask[idx] > 0f) 255 else 0
                pixels[y * w + x] = (v shl 24) or 0x00FFFFFF
            }
        }
        bmp.setPixels(pixels, 0, w, 0, 0, w, h)
        return bmp
    }

    fun release() {
        sessionManager.release(encoderModelId)
        sessionManager.release(decoderModelId)
        cachedEmbeddings = null
        isReady = false
    }
}
