package com.myvideo.editor.core.security.model

import android.content.Context
import java.io.File

class ModelCacheManager(private val context: Context) {
    private val cacheDir get() = File(context.cacheDir, "model_cache").apply { mkdirs() }
    private val maxCacheSizeMb = 500

    fun getCachedModel(modelId: String): File? {
        val file = File(cacheDir, "$modelId.cache")
        return if (file.exists() && System.currentTimeMillis() - file.lastModified() < 7 * 24 * 60 * 60 * 1000) file else null
    }

    fun cacheModel(modelId: String, data: ByteArray) {
        evictIfNeeded()
        File(cacheDir, "$modelId.cache").writeBytes(data)
    }

    fun getCacheSize(): Long = cacheDir.listFiles()?.sumOf { it.length() } ?: 0

    fun evictIfNeeded() {
        val files = cacheDir.listFiles()?.sortedBy { it.lastModified() } ?: return
        var size = files.sumOf { it.length() }
        for (file in files) {
            if (size <= maxCacheSizeMb * 1024 * 1024) break
            size -= file.length(); file.delete()
        }
    }

    fun clearCache() { cacheDir.listFiles()?.forEach { it.delete() } }
    fun hasCache(modelId: String): Boolean = File(cacheDir, "$modelId.cache").exists()
}
