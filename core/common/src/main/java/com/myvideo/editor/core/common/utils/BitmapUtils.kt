package com.myvideo.editor.core.common.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object BitmapUtils {
    fun scale(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val ratio = minOf(maxWidth.toFloat() / bitmap.width, maxHeight.toFloat() / bitmap.height)
        if (ratio >= 1f) return bitmap
        return Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true)
    }

    fun rotate(bitmap: Bitmap, degrees: Float): Bitmap {
        if (degrees == 0f) return bitmap
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun crop(bitmap: Bitmap, x: Int, y: Int, w: Int, h: Int): Bitmap {
        return Bitmap.createBitmap(bitmap, x.coerceIn(0, bitmap.width - 1), y.coerceIn(0, bitmap.height - 1),
            w.coerceAtMost(bitmap.width), h.coerceAtMost(bitmap.height))
    }

    fun saveToFile(bitmap: Bitmap, path: String, quality: Int = 90): Boolean {
        return try {
            FileOutputStream(path).use { bitmap.compress(Bitmap.CompressFormat.JPEG, quality, it) }
            true
        } catch (e: Exception) { false }
    }

    fun toByteArray(bitmap: Bitmap, quality: Int = 80): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        return stream.toByteArray()
    }

    fun extractPixelArray(bitmap: Bitmap): IntArray {
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        return pixels
    }

    fun isOpaque(pixel: Int): Boolean = (pixel shr 24 and 0xFF) > 128

    fun getBrightness(pixel: Int): Int {
        return ((pixel shr 16 and 0xFF) + (pixel shr 8 and 0xFF) + (pixel and 0xFF)) / 3
    }
}
