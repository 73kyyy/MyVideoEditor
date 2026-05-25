package com.myvideo.editor.core.export

import com.myvideo.editor.core.export.model.ExportConfig
import com.myvideo.editor.core.export.model.ExportStatus

class ExportQueueManager {
    data class ExportJob(
        val id: String, val config: ExportConfig,
        var status: ExportStatus = ExportStatus.Idle, var progress: Float = 0f
    )

    private val queue = mutableListOf<ExportJob>()
    private var current: ExportJob? = null

    fun enqueue(job: ExportJob) { queue.add(job) }
    fun cancel(id: String) { queue.find { it.id == id }?.status = ExportStatus.Cancelled }
    fun getNext(): ExportJob? {
        current = queue.firstOrNull { it.status == ExportStatus.Idle }
        current?.status = ExportStatus.Preparing
        return current
    }
    fun complete(id: String) { queue.find { it.id == id }?.status = ExportStatus.Completed }
    fun fail(id: String) { queue.find { it.id == id }?.status = ExportStatus.Failed }
    fun updateProgress(id: String, progress: Float) { queue.find { it.id == id }?.progress = progress }
    fun getQueue(): List<ExportJob> = queue.toList()
    fun getCurrent(): ExportJob? = current
    fun clear() { queue.clear(); current = null }
    fun getPendingCount(): Int = queue.count { it.status == ExportStatus.Idle }
}
