package com.myvideo.editor.core.security.model

data class ModelChunkManifest(
    val modelId: String,
    val version: String,
    val totalSize: Long,
    val chunkSize: Long = 1024 * 1024,
    val chunks: List<ChunkInfo>
)

data class ChunkInfo(
    val index: Int,
    val url: String,
    val size: Long,
    val checksum: String
)

class ManifestParser {
    fun parse(json: String): ModelChunkManifest? {
        return try {
            val obj = org.json.JSONObject(json)
            val chunksArr = obj.getJSONArray("chunks")
            val chunks = (0 until chunksArr.length()).map { i ->
                val c = chunksArr.getJSONObject(i)
                ChunkInfo(c.getInt("index"), c.getString("url"), c.getLong("size"), c.getString("checksum"))
            }
            ModelChunkManifest(obj.getString("modelId"), obj.getString("version"), obj.getLong("totalSize"), obj.optLong("chunkSize", 1024*1024), chunks)
        } catch (e: Exception) { null }
    }

    fun getChunkCount(manifest: ModelChunkManifest): Int = manifest.chunks.size
    fun getTotalChunks(manifest: ModelChunkManifest): Int = manifest.chunks.size
}
