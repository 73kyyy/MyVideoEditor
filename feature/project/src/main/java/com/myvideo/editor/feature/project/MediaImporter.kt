package com.myvideo.editor.feature.project

import android.content.Context
import android.net.Uri

class MediaImporter(private val context: Context) {
    data class ImportedMedia(val path: String, val type: String, val durationMs: Long, val width: Int, val height: Int)

    fun import(uri: Uri): ImportedMedia? {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val path = uri.toString()
                    ImportedMedia(path, "video", 0, 0, 0)
                } else null
            }
        } catch (e: Exception) { null }
    }

    fun importMultiple(uris: List<Uri>): List<ImportedMedia> = uris.mapNotNull { import(it) }
}
