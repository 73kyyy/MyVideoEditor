package com.myvideo.editor.core.security.model

import android.content.Context
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class ModelDownloader(private val context: Context) {
    data class ModelInfo(val id: String, val url: String, val sizeMb: Int, val checksum: String)
    data class DownloadProgress(val modelId: String, val percent: Float, val bytesDownloaded: Long, val totalBytes: Long)

    private val modelDir get() = File(context.filesDir, "models").apply { mkdirs() }
    private var onProgress: ((DownloadProgress) -> Unit)? = null

    fun setProgressListener(listener: (DownloadProgress) -> Unit) { onProgress = listener }

    fun download(model: ModelInfo): Boolean {
        val dest = File(modelDir, "${model.id}.bin")
        if (dest.exists() && verifyChecksum(dest, model.checksum)) return true
        return try {
            val url = URL(model.url)
            val conn = url.openConnection() as HttpURLConnection
            conn.connect()
            val total = conn.contentLength.toLong()
            var downloaded = 0L
            conn.inputStream.use { input ->
                dest.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloaded += read
                        onProgress?.invoke(DownloadProgress(model.id, downloaded.toFloat() / total, downloaded, total))
                    }
                }
            }
            verifyChecksum(dest, model.checksum)
        } catch (e: Exception) { false }
    }

    fun isDownloaded(modelId: String): Boolean = File(modelDir, "$modelId.bin").exists()
    fun getModelPath(modelId: String): String? = File(modelDir, "$modelId.bin").takeIf { it.exists() }?.absolutePath
    fun deleteModel(modelId: String) { File(modelDir, "$modelId.bin").delete() }
    fun getDownloadedSize(): Long = modelDir.listFiles()?.sumOf { it.length() } ?: 0

    private fun verifyChecksum(file: File, expected: String): Boolean {
        val hash = file.inputStream().use {
            val md = java.security.MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(8192)
            var read: Int
            while (it.read(buffer).also { r -> read = r } != -1) md.update(buffer, 0, read)
            md.digest().joinToString("") { "%02x".format(it) }
        }
        return hash == expected || expected.isEmpty()
    }
}
