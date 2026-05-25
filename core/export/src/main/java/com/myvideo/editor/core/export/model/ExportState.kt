package com.myvideo.editor.core.export.model

data class ExportState(
    val status: ExportStatus = ExportStatus.Idle,
    val progress: Float = 0f,
    val outputPath: String = "",
    val error: String = "",
    val startTimeMs: Long = 0,
    val estimatedRemainingMs: Long = 0,
    val currentFps: Float = 0f,
    val processedFrames: Int = 0,
    val totalFrames: Int = 0
)

enum class ExportStatus { Idle, Preparing, Encoding, Muxing, Completed, Failed, Cancelled }
