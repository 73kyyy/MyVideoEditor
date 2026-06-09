package com.myvideo.editor.core.security.model

import android.content.Context
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * 模型下载器
 * 从CDN下载加密模型，需会员验证
 * 优先从本地assets读取（CI/CD已打包加密模型）
 */
class ModelDownloader(private val context: Context) {
    data class ModelInfo(val id: String, val url: String, val sizeMb: Int, val checksum: String)
    data class DownloadProgress(val modelId: String, val percent: Float, val bytesDownloaded: Long, val totalBytes: Long)

    private val modelDir get() = File(context.filesDir, "models").apply { mkdirs() }
    private var onProgress: ((DownloadProgress) -> Unit)? = null

    fun setProgressListener(listener: (DownloadProgress) -> Unit) { onProgress = listener }

    fun download(model: ModelInfo): Boolean {
        val dest = File(modelDir, "${model.id}.bin")
        if (dest.exists() && verifyChecksum(dest, model.checksum)) return true

        // 优先从assets读取（CI/CD已打包加密模型到assets）
        val fromAssets = copyFromAssets(model, dest)
        if (fromAssets) return true

        // assets中没有则从CDN下载
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

    /**
     * 从assets目录复制模型文件
     */
    private fun copyFromAssets(model: ModelInfo, dest: File): Boolean {
        return try {
            val possiblePaths = listOf(
                "ai_models/${model.id}.onnx",
                "ai_models/${model.id}.bin",
                "models/${model.id}.onnx",
                "models/${model.id}.bin"
            )

            var sourcePath: String? = null
            for (path in possiblePaths) {
                try {
                    context.assets.open(path).close()
                    sourcePath = path
                    break
                } catch (_: Exception) { continue }
            }

            if (sourcePath != null) {
                context.assets.open(sourcePath).use { input ->
                    dest.outputStream().use { output ->
                        val buffer = ByteArray(8192)
                        var read: Int
                        var total = 0L
                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                            total += read
                            onProgress?.invoke(DownloadProgress(model.id, 1f, total, total))
                        }
                    }
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun isDownloaded(modelId: String): Boolean = File(modelDir, "$modelId.bin").exists()
    fun getModelPath(modelId: String): String? = File(modelDir, "$modelId.bin").takeIf { it.exists() }?.absolutePath
    fun deleteModel(modelId: String) { File(modelDir, "$modelId.bin").delete() }
    fun getDownloadedSize(): Long = modelDir.listFiles()?.sumOf { it.length() } ?: 0

    private fun verifyChecksum(file: File, expected: String): Boolean {
        if (expected.isEmpty()) return true
        val hash = file.inputStream().use {
            val md = java.security.MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(8192)
            var read: Int
            while (it.read(buffer).also { r -> read = r } != -1) md.update(buffer, 0, read)
            md.digest().joinToString("") { "%02x".format(it) }
        }
        return hash == expected
    }
}
