package com.myvideo.editor.core.performance

import android.graphics.Bitmap

class FrameBufferPool(private val maxPoolSize: Int = 10) {
    private val pool = mutableListOf<Bitmap>()
    private val inUse = mutableSetOf<Bitmap>()

    fun acquire(width: Int, height: Int): Bitmap {
        synchronized(pool) {
            val available = pool.firstOrNull { it.width == width && it.height == height && !inUse.contains(it) }
            if (available != null) { inUse.add(available); return available }
        }
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        synchronized(pool) { pool.add(bmp); inUse.add(bmp) }
        return bmp
    }

    fun release(bitmap: Bitmap) { synchronized(pool) { inUse.remove(bitmap) } }

    fun clear() { synchronized(pool) { pool.forEach { if (!it.isRecycled) it.recycle() }; pool.clear(); inUse.clear() } }

    fun getPoolSize(): Int = pool.size
    fun getInUseCount(): Int = inUse.size
    fun getAvailableCount(): Int = pool.size - inUse.size
}
