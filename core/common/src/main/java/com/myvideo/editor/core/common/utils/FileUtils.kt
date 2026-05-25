package com.myvideo.editor.core.common.utils

import android.content.Context
import java.io.File

object FileUtils {
    fun getCacheDir(context: Context, subDir: String = ""): File {
        val dir = File(context.cacheDir, "nexclip/$subDir")
        dir.mkdirs(); return dir
    }

    fun getExportDir(context: Context): File {
        val dir = File(context.getExternalFilesDir(null), "NexClip")
        dir.mkdirs(); return dir
    }

    fun getTempFile(context: Context, prefix: String = "tmp", suffix: String = ".mp4"): File {
        return File.createTempFile(prefix, suffix, getCacheDir(context, "temp"))
    }

    fun deleteFile(path: String): Boolean = try { File(path).delete() } catch (e: Exception) { false }

    fun deleteDir(dir: File): Boolean {
        if (dir.isDirectory) dir.listFiles()?.forEach { deleteDir(it) }
        return dir.delete()
    }

    fun getFileSize(path: String): Long = try { File(path).length() } catch (e: Exception) { 0 }

    fun formatFileSize(bytes: Long): String = when {
        bytes < 1024 -> "${bytes}B"
        bytes < 1024 * 1024 -> "${bytes / 1024}KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)}MB"
        else -> "${bytes / (1024 * 1024 * 1024)}GB"
    }

    fun getAvailableSpace(path: String): Long = try { File(path).freeSpace } catch (e: Exception) { 0 }

    fun ensureDir(path: String): File = File(path).apply { mkdirs() }

    fun copyFile(src: String, dst: String): Boolean {
        return try {
            File(src).inputStream().use { input ->
                File(dst).outputStream().use { output -> input.copyTo(output) }
            }
            true
        } catch (e: Exception) { false }
    }
}
