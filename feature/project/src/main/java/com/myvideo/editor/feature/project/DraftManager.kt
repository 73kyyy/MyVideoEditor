package com.myvideo.editor.feature.project

import android.content.Context
import java.io.File

class DraftManager(private val context: Context) {
    data class Draft(val projectId: String, val timestamp: Long, val path: String)

    private val draftDir get() = File(context.cacheDir, "drafts").apply { mkdirs() }

    fun saveDraft(projectId: String, data: String) {
        File(draftDir, "$projectId.draft").writeText(data)
    }
    fun loadDraft(projectId: String): String? {
        val f = File(draftDir, "$projectId.draft"); return if (f.exists()) f.readText() else null
    }
    fun hasDraft(projectId: String): Boolean = File(draftDir, "$projectId.draft").exists()
    fun deleteDraft(projectId: String) { File(draftDir, "$projectId.draft").delete() }
    fun getAllDrafts(): List<Draft> = draftDir.listFiles()?.filter { it.extension == "draft" }?.map { Draft(it.nameWithoutExtension, it.lastModified(), it.absolutePath) } ?: emptyList()
    fun clearAll() { draftDir.listFiles()?.forEach { it.delete() } }
}
