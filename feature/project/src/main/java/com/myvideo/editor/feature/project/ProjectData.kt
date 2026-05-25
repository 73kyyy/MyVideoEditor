package com.myvideo.editor.feature.project

data class ProjectData(
    val id: String,
    val name: String,
    val width: Int = 1920,
    val height: Int = 1080,
    val fps: Int = 30,
    val createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis(),
    var durationMs: Long = 0,
    var thumbnailPath: String = "",
    var description: String = "",
    var tags: List<String> = emptyList()
)
