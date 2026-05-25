package com.myvideo.editor.core.common.utils

import android.content.Context
import java.io.File

class RecycleBinManager(private val context: Context) {
    data class RecycledItem(val originalPath: String, val recycledPath: String, val timestamp: Long)

    private val recycled = mutableListOf<RecycledItem>()
    private val binDir: File get() = FileUtils.getCacheDir(context, "recycle_bin")

    fun moveToRecycle(path: String): Boolean {
        val src = File(path)
        if (!src.exists()) return false
        val dst = File(binDir, "${System.currentTimeMillis()}_${src.name}")
        return try {
            src.copyTo(dst); src.delete()
            recycled.add(RecycledItem(path, dst.absolutePath, System.currentTimeMillis()))
            true
        } catch (e: Exception) { false }
    }

    fun restore(index: Int): Boolean {
        if (index !in recycled.indices) return false
        val item = recycled[index]
        return try {
            File(item.recycledPath).copyTo(File(item.originalPath), overwrite = true)
            File(item.recycledPath).delete()
            recycled.removeAt(index)
            true
        } catch (e: Exception) { false }
    }

    fun empty() { recycled.clear(); binDir.listFiles()?.forEach { it.delete() } }
    fun getAll(): List<RecycledItem> = recycled.toList()
    fun getCount(): Int = recycled.size
}
