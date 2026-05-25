package com.myvideo.editor.core.performance

import android.graphics.Bitmap

class DecodedFrameCache(private val maxCacheSize: Int = 30) {
    private val cache = LinkedHashMap<Long, Bitmap>(maxCacheSize, 0.75f, true)
    private var totalMemoryBytes = 0L
    private val maxMemoryBytes = 100 * 1024 * 1024L

    fun put(timeMs: Long, frame: Bitmap) {
        val size = frame.byteCount.toLong()
        while (totalMemoryBytes + size > maxMemoryBytes && cache.isNotEmpty()) {
            val oldest = cache.keys.first()
            totalMemoryBytes -= cache.remove(oldest)?.byteCount?.toLong() ?: 0L
        }
        cache[timeMs] = frame
        totalMemoryBytes += size
    }

    fun get(timeMs: Long): Bitmap? = cache[timeMs]

    fun getNearest(timeMs: Long, toleranceMs: Long = 100): Bitmap? {
        cache[timeMs]?.let { return it }
        return cache.keys.minByOrNull { kotlin.math.abs(it - timeMs) }
            ?.takeIf { kotlin.math.abs(it - timeMs) <= toleranceMs }
            ?.let { cache[it] }
    }

    fun clear() { cache.clear(); totalMemoryBytes = 0L }
    fun size(): Int = cache.size
    fun memoryUsageMB(): Long = totalMemoryBytes / (1024 * 1024)
}
