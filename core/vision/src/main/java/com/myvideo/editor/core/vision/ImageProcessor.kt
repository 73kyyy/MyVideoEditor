package com.myvideo.editor.core.vision

import android.graphics.Bitmap

class ImageProcessor {
    external fun nativeCanny(pixels: IntArray, w: Int, h: Int, t1: Float, t2: Float): IntArray
    external fun nativeBlur(pixels: IntArray, w: Int, h: Int, radius: Int): IntArray
    external fun nativeThreshold(pixels: IntArray, w: Int, h: Int, threshold: Float): IntArray

    fun canny(bitmap: Bitmap, threshold1: Float = 100f, threshold2: Float = 200f): Bitmap {
        val w = bitmap.width; val h = bitmap.height
        val pixels = IntArray(w * h); bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        val result = nativeCanny(pixels, w, h, threshold1, threshold2)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bmp.setPixels(result, 0, w, 0, 0, w, h); return bmp
    }

    fun blur(bitmap: Bitmap, radius: Int = 5): Bitmap {
        val w = bitmap.width; val h = bitmap.height
        val pixels = IntArray(w * h); bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        val result = nativeBlur(pixels, w, h, radius)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bmp.setPixels(result, 0, w, 0, 0, w, h); return bmp
    }
}
