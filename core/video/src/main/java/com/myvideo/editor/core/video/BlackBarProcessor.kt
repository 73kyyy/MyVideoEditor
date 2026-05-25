package com.myvideo.editor.core.video

class BlackBarProcessor {
    data class BlackBarResult(val top: Int, val bottom: Int, val left: Int, val right: Int)

    fun detectBlackBars(pixels: IntArray, width: Int, height: Int, threshold: Int = 16): BlackBarResult {
        var top = 0; var bottom = height; var left = 0; var right = width
        while (top < height && isBlackRow(pixels, top, width, threshold)) top++
        while (bottom > top && isBlackRow(pixels, bottom - 1, width, threshold)) bottom--
        while (left < width && isBlackCol(pixels, left, width, height, threshold)) left++
        while (right > left && isBlackCol(pixels, right - 1, width, height, threshold)) right--
        return BlackBarResult(top, height - bottom, left, width - right)
    }

    fun cropBlackBars(pixels: IntArray, width: Int, height: Int, bars: BlackBarResult): IntArray {
        val newW = width - bars.left - bars.right
        val newH = height - bars.top - bars.bottom
        if (newW <= 0 || newH <= 0) return pixels
        return IntArray(newW * newH) { i ->
            val x = i % newW + bars.left; val y = i / newW + bars.top
            pixels[y * width + x]
        }
    }

    fun addBlackBars(pixels: IntArray, srcW: Int, srcH: Int, targetW: Int, targetH: Int): IntArray {
        val offsetX = ((targetW - srcW) / 2).coerceAtLeast(0)
        val offsetY = ((targetH - srcH) / 2).coerceAtLeast(0)
        val result = IntArray(targetW * targetH) { 0xFF000000.toInt() }
        for (y in 0 until srcH.coerceAtMost(targetH)) for (x in 0 until srcW.coerceAtMost(targetW)) {
            val srcIdx = y * srcW + x; val dstIdx = (y + offsetY) * targetW + (x + offsetX)
            if (srcIdx < pixels.size && dstIdx < result.size) result[dstIdx] = pixels[srcIdx]
        }
        return result
    }

    private fun isBlackRow(pixels: IntArray, row: Int, width: Int, threshold: Int): Boolean {
        val start = row * width
        for (i in start until (start + width).coerceAtMost(pixels.size)) {
            val p = pixels[i]; val brightness = ((p shr 16 and 0xFF) + (p shr 8 and 0xFF) + (p and 0xFF)) / 3
            if (brightness > threshold) return false
        }
        return true
    }

    private fun isBlackCol(pixels: IntArray, col: Int, width: Int, height: Int, threshold: Int): Boolean {
        for (y in 0 until height) {
            val idx = y * width + col
            if (idx < pixels.size) {
                val p = pixels[idx]; val brightness = ((p shr 16 and 0xFF) + (p shr 8 and 0xFF) + (p and 0xFF)) / 3
                if (brightness > threshold) return false
            }
        }
        return true
    }
}
