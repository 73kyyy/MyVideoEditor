package com.myvideo.editor.core.export

import android.content.Context
import java.io.File

class ExportCheckPointManager(private val context: Context) {
    data class CheckPoint(val jobId: String, val frameIndex: Int, val timestamp: Long)

    private val checkpointDir: File get() = File(context.cacheDir, "export_checkpoints").apply { mkdirs() }

    fun save(jobId: String, frameIndex: Int) {
        val file = File(checkpointDir, "$jobId.chk")
        file.writeText("$frameIndex:${System.currentTimeMillis()}")
    }

    fun load(jobId: String): CheckPoint? {
        val file = File(checkpointDir, "$jobId.chk")
        if (!file.exists()) return null
        return try {
            val parts = file.readText().split(":")
            CheckPoint(jobId, parts[0].toInt(), parts[1].toLong())
        } catch (e: Exception) { null }
    }

    fun hasCheckPoint(jobId: String): Boolean = File(checkpointDir, "$jobId.chk").exists()

    fun delete(jobId: String) { File(checkpointDir, "$jobId.chk").delete() }

    fun clearAll() { checkpointDir.listFiles()?.forEach { it.delete() } }
}
