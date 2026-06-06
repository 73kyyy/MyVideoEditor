package com.videoeditor.data.repository

import android.content.Context
import com.videoeditor.data.model.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class ProjectRepository(private val context: Context) {

    private val projectsDir = File(context.filesDir, "projects").apply { mkdirs() }

    suspend fun saveProject(project: Project) = withContext(Dispatchers.IO) {
        val file = File(projectsDir, "${project.id}.json")
        file.writeText(projectToJson(project).toString())
    }

    suspend fun loadProject(projectId: String): Project? = withContext(Dispatchers.IO) {
        val file = File(projectsDir, "$projectId.json")
        if (!file.exists()) return@withContext null
        try {
            jsonToProject(JSONObject(file.readText()))
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAllProjects(): List<Project> = withContext(Dispatchers.IO) {
        projectsDir.listFiles()
            ?.filter { it.extension == "json" }
            ?.mapNotNull { file ->
                try {
                    jsonToProject(JSONObject(file.readText()))
                } catch (e: Exception) {
                    null
                }
            }
            ?.sortedByDescending { it.updatedAt }
            ?: emptyList()
    }

    suspend fun deleteProject(projectId: String) = withContext(Dispatchers.IO) {
        val file = File(projectsDir, "$projectId.json")
        file.delete()
    }

    private fun projectToJson(project: Project): JSONObject {
        return JSONObject().apply {
            put("id", project.id)
            put("name", project.name)
            put("createdAt", project.createdAt)
            put("updatedAt", project.updatedAt)
            put("width", project.width)
            put("height", project.height)
            put("frameRate", project.frameRate)
            put("durationUs", project.durationUs)

            val videoTracks = JSONArray()
            project.videoTracks.forEach { track ->
                videoTracks.put(JSONObject().apply {
                    put("id", track.id)
                    put("isMuted", track.isMuted)
                    put("volume", track.volume.toDouble())
                    val clips = JSONArray()
                    track.clips.forEach { clip ->
                        clips.put(JSONObject().apply {
                            put("id", clip.id)
                            put("startUs", clip.startUs)
                            put("endUs", clip.endUs)
                            put("trimStartUs", clip.trimStartUs)
                            put("trimEndUs", clip.trimEndUs)
                            put("sourcePath", clip.sourcePath)
                            put("width", clip.width)
                            put("height", clip.height)
                            put("rotation", clip.rotation)
                            put("volume", clip.volume.toDouble())
                            put("speed", clip.speed.toDouble())
                            put("opacity", clip.opacity.toDouble())
                        })
                    }
                    put("clips", clips)
                })
            }
            put("videoTracks", videoTracks)

            val audioTracks = JSONArray()
            project.audioTracks.forEach { track ->
                audioTracks.put(JSONObject().apply {
                    put("id", track.id)
                    put("volume", track.volume.toDouble())
                    put("isMuted", track.isMuted)
                    val clips = JSONArray()
                    track.clips.forEach { clip ->
                        clips.put(JSONObject().apply {
                            put("id", clip.id)
                            put("startUs", clip.startUs)
                            put("endUs", clip.endUs)
                            put("trimStartUs", clip.trimStartUs)
                            put("trimEndUs", clip.trimEndUs)
                            put("sourcePath", clip.sourcePath)
                            put("volume", clip.volume.toDouble())
                            put("speed", clip.speed.toDouble())
                            put("fadeInMs", clip.fadeInMs)
                            put("fadeOutMs", clip.fadeOutMs)
                        })
                    }
                    put("clips", clips)
                })
            }
            put("audioTracks", audioTracks)
        }
    }

    private fun jsonToProject(json: JSONObject): Project {
        return Project(
            id = json.getString("id"),
            name = json.getString("name"),
            createdAt = json.getLong("createdAt"),
            updatedAt = json.getLong("updatedAt"),
            width = json.getInt("width"),
            height = json.getInt("height"),
            frameRate = json.getInt("frameRate"),
            durationUs = json.optLong("durationUs", 0L),
            videoTracks = parseVideoTracks(json.optJSONArray("videoTracks")),
            audioTracks = parseAudioTracks(json.optJSONArray("audioTracks"))
        )
    }

    private fun parseVideoTracks(jsonArray: JSONArray?): List<com.videoeditor.data.model.VideoTrack> {
        if (jsonArray == null) return emptyList()
        return (0 until jsonArray.length()).map { i ->
            val trackJson = jsonArray.getJSONObject(i)
            com.videoeditor.data.model.VideoTrack(
                id = trackJson.getString("id"),
                isMuted = trackJson.optBoolean("isMuted", false),
                volume = trackJson.optDouble("volume", 1.0).toFloat(),
                clips = parseVideoClips(trackJson.optJSONArray("clips"))
            )
        }
    }

    private fun parseVideoClips(jsonArray: JSONArray?): List<com.videoeditor.data.model.VideoClip> {
        if (jsonArray == null) return emptyList()
        return (0 until jsonArray.length()).map { i ->
            val clipJson = jsonArray.getJSONObject(i)
            com.videoeditor.data.model.VideoClip(
                id = clipJson.getString("id"),
                startUs = clipJson.getLong("startUs"),
                endUs = clipJson.getLong("endUs"),
                trimStartUs = clipJson.getLong("trimStartUs"),
                trimEndUs = clipJson.getLong("trimEndUs"),
                sourcePath = clipJson.getString("sourcePath"),
                width = clipJson.optInt("width", 0),
                height = clipJson.optInt("height", 0),
                rotation = clipJson.optInt("rotation", 0),
                volume = clipJson.optDouble("volume", 1.0).toFloat(),
                speed = clipJson.optDouble("speed", 1.0).toFloat(),
                opacity = clipJson.optDouble("opacity", 1.0).toFloat()
            )
        }
    }

    private fun parseAudioTracks(jsonArray: JSONArray?): List<com.videoeditor.data.model.AudioTrack> {
        if (jsonArray == null) return emptyList()
        return (0 until jsonArray.length()).map { i ->
            val trackJson = jsonArray.getJSONObject(i)
            com.videoeditor.data.model.AudioTrack(
                id = trackJson.getString("id"),
                volume = trackJson.optDouble("volume", 1.0).toFloat(),
                isMuted = trackJson.optBoolean("isMuted", false),
                clips = parseAudioClips(trackJson.optJSONArray("clips"))
            )
        }
    }

    private fun parseAudioClips(jsonArray: JSONArray?): List<com.videoeditor.data.model.AudioClip> {
        if (jsonArray == null) return emptyList()
        return (0 until jsonArray.length()).map { i ->
            val clipJson = jsonArray.getJSONObject(i)
            com.videoeditor.data.model.AudioClip(
                id = clipJson.getString("id"),
                startUs = clipJson.getLong("startUs"),
                endUs = clipJson.getLong("endUs"),
                trimStartUs = clipJson.getLong("trimStartUs"),
                trimEndUs = clipJson.getLong("trimEndUs"),
                sourcePath = clipJson.getString("sourcePath"),
                volume = clipJson.optDouble("volume", 1.0).toFloat(),
                speed = clipJson.optDouble("speed", 1.0).toFloat(),
                fadeInMs = clipJson.optLong("fadeInMs", 0L),
                fadeOutMs = clipJson.optLong("fadeOutMs", 0L)
            )
        }
    }
}
