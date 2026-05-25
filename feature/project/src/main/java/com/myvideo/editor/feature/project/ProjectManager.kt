package com.myvideo.editor.feature.project

class ProjectManager {
    private val projects = mutableListOf<ProjectData>()
    var currentProject: ProjectData? = null; private set

    fun create(name: String, width: Int = 1920, height: Int = 1080, fps: Int = 30): ProjectData {
        val project = ProjectData(id = "proj_${System.currentTimeMillis()}", name = name, width = width, height = height, fps = fps)
        projects.add(project); currentProject = project; return project
    }

    fun open(id: String): ProjectData? {
        currentProject = projects.find { it.id == id }; return currentProject
    }

    fun save() { currentProject?.updatedAt = System.currentTimeMillis() }
    fun delete(id: String) { projects.removeAll { it.id == id }; if (currentProject?.id == id) currentProject = null }
    fun getAll(): List<ProjectData> = projects.toList()
    fun get(id: String): ProjectData? = projects.find { it.id == id }
    fun rename(id: String, name: String) { projects.find { it.id == id }?.let { it.copy(name = name) } }
}
