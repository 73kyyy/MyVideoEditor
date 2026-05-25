package com.myvideo.editor.feature.effects.crop

class CropTransformEngine {
    data class CropRect(val x: Int, val y: Int, val width: Int, val height: Int)
    data class TransformParams(val scaleX: Float = 1f, val scaleY: Float = 1f, val rotation: Float = 0f, val translateX: Float = 0f, val translateY: Float = 0f)

    fun crop(pixels: IntArray, srcW: Int, srcH: Int, rect: CropRect): IntArray {
        val result = IntArray(rect.width * rect.height)
        for (y in 0 until rect.height) for (x in 0 until rect.width) {
            val srcX = (x + rect.x).coerceIn(0, srcW - 1)
            val srcY = (y + rect.y).coerceIn(0, srcH - 1)
            result[y * rect.width + x] = pixels[srcY * srcW + srcX]
        }
        return result
    }

    fun transform(pixels: IntArray, srcW: Int, srcH: Int, params: TransformParams): IntArray {
        val result = IntArray(srcW * srcH) { 0xFF000000.toInt() }
        val cx = srcW / 2f; val cy = srcH / 2f
        val cos = kotlin.math.cos(Math.toRadians(params.rotation.toDouble())).toFloat()
        val sin = kotlin.math.sin(Math.toRadians(params.rotation.toDouble())).toFloat()
        for (y in 0 until srcH) for (x in 0 until srcW) {
            val dx = (x - cx) / params.scaleX - params.translateX
            val dy = (y - cy) / params.scaleY - params.translateY
            val srcX = ((dx * cos + dy * sin) + cx).toInt().coerceIn(0, srcW - 1)
            val srcY = ((-dx * sin + dy * cos) + cy).toInt().coerceIn(0, srcH - 1)
            result[y * srcW + x] = pixels[srcY * srcW + srcX]
        }
        return result
    }

    fun fitToRatio(width: Int, height: Int, targetW: Int, targetH: Int): CropRect {
        val targetRatio = targetW.toFloat() / targetH
        val srcRatio = width.toFloat() / height
        return if (srcRatio > targetRatio) {
            val newW = (height * targetRatio).toInt(); CropRect((width - newW) / 2, 0, newW, height)
        } else {
            val newH = (width / targetRatio).toInt(); CropRect(0, (height - newH) / 2, width, newH)
        }
    }
}
