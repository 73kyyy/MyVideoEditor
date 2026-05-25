package com.myvideo.editor.feature.project

class TemplateManager {
    data class Template(val id: String, val name: String, val width: Int, val height: Int, val fps: Int, val trackCount: Int)

    private val templates = listOf(
        Template("tiktok", "抖音", 1080, 1920, 30, 3),
        Template("youtube", "YouTube", 1920, 1080, 30, 5),
        Template("instagram", "Instagram", 1080, 1080, 30, 3),
        Template("reels", "Reels", 1080, 1920, 30, 3),
        Template("cinema", "电影", 2560, 1080, 24, 8),
        Template("square", "方形", 1080, 1080, 30, 3),
        Template("story", "故事", 1080, 1920, 30, 3)
    )

    fun getTemplates(): List<Template> = templates
    fun get(id: String): Template? = templates.find { it.id == id }
    fun createProject(template: Template): ProjectData = ProjectData("proj_${System.currentTimeMillis()}", template.name, template.width, template.height, template.fps)
}
