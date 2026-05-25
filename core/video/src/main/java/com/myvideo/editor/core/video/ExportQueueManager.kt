package com.myvideo.editor.core.video

class ExportQueueManager {
    data class ExportJob(
        val id: String, val inputPath: String, val outputPath: String,
        val profile: String = "1080p", var status: JobStatus = JobStatus.Pending,
        var progress: Float = 0f
    )
    enum class JobStatus { Pending, Processing, Completed, Failed, Cancelled }
    private val queue = mutableListOf<ExportJob>()
    private var currentJob: ExportJob? = null

    fun enqueue(job: ExportJob) { queue.add(job) }
    fun cancel(id: String) { queue.find { it.id == id }?.status = JobStatus.Cancelled }
    fun getNext(): ExportJob? { currentJob = queue.firstOrNull { it.status == JobStatus.Pending }; currentJob?.status = JobStatus.Processing; return currentJob }
    fun updateProgress(id: String, progress: Float) { queue.find { it.id == id }?.progress = progress }
    fun complete(id: String) { queue.find { it.id == id }?.status = JobStatus.Completed }
    fun fail(id: String) { queue.find { it.id == id }?.status = JobStatus.Failed }
    fun getQueue(): List<ExportJob> = queue.toList()
    fun getCurrent(): ExportJob? = currentJob
    fun clear() { queue.clear(); currentJob = null }
}
