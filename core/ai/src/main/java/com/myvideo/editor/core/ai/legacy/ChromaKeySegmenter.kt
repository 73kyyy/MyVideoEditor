package com.myvideo.editor.core.ai.legacy

import android.graphics.Bitmap

class ChromaKeySegmenter {
    data class Params(
        val keyColor: Int = 0x00FF00,
        val similarity: Float = 0.15f,
        val smoothness: Float = 0.1f,
        val spill: Float = 0.05f
    )

    fun apply(bitmap: Bitmap, params: Params = Params()): Bitmap {
        val w = bitmap.width; val h = bitmap.height
        val pixels = IntArray(w * h); bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        val kr = (params.keyColor shr 16 and 0xFF) / 255f
        val kg = (params.keyColor shr 8 and 0xFF) / 255f
        val kb = (params.keyColor and 0xFF) / 255f
        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        for (i in pixels.indices) {
            val p = pixels[i]
            val r = (p shr 16 and 0xFF) / 255f
            val g = (p shr 8 and 0xFF) / 255f
            val b = (p and 0xFF) / 255f
            val dist = kotlin.math.sqrt((r-kr)*(r-kr)+(g-kg)*(g-kg)+(b-kb)*(b-kb))
            val alpha = if (dist < params.similarity) 0f else if (dist < params.similarity + params.smoothness) ((dist-params.similarity)/params.smoothness).coerceIn(0f,1f) else 1f
            pixels[i] = ((alpha*255).toInt() shl 24) or (p and 0x00FFFFFF)
        }
        result.setPixels(pixels, 0, w, 0, 0, w, h)
        return result
    }
}
