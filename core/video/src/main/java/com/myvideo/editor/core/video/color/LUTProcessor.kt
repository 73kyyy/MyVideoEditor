package com.myvideo.editor.core.video.color

class LUTProcessor {
    data class LUT(val size: Int, val data: Array<FloatArray>)

    fun apply3DLUT(pixels: IntArray, lut: LUT): IntArray {
        val s = lut.size
        val scale = (s - 1).toFloat() / 255f
        return IntArray(pixels.size) { i ->
            val p = pixels[i]
            val ri = ((p shr 16 and 0xFF) * scale).toInt().coerceIn(0, s-2)
            val gi = ((p shr 8 and 0xFF) * scale).toInt().coerceIn(0, s-2)
            val bi = ((p and 0xFF) * scale).toInt().coerceIn(0, s-2)
            val idx = ri * s * s + gi * s + bi
            val out = if (idx < lut.data.size && lut.data[idx].size >= 3) lut.data[idx] else floatArrayOf(
                (p shr 16 and 0xFF)/255f, (p shr 8 and 0xFF)/255f, (p and 0xFF)/255f
            )
            val r = (out[0]*255).toInt().coerceIn(0,255)
            val g = (out[1]*255).toInt().coerceIn(0,255)
            val b = (out[2]*255).toInt().coerceIn(0,255)
            (p and 0xFF000000.toInt()) or (r shl 16) or (g shl 8) or b
        }
    }

    fun generateIdentity(size: Int): LUT {
        val data = Array(size*size*size) { FloatArray(3) }
        val scale = 1f/(size-1)
        for (r in 0 until size) for (g in 0 until size) for (b in 0 until size) {
            data[r*size*size+g*size+b] = floatArrayOf(r*scale, g*scale, b*scale)
        }
        return LUT(size, data)
    }
}
