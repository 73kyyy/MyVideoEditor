package com.videoeditor.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.videoeditor.data.model.*
import com.videoeditor.engine.FFmpegEngine
import com.videoeditor.engine.VideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class EditorViewModel(private val context: Context) : ViewModel() {

    private val _project = MutableStateFlow(Project())
    val project: StateFlow<Project> = _project

    private val _currentPositionUs = MutableStateFlow(0L)
    val currentPositionUs: StateFlow<Long> = _currentPositionUs

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _selectedClipId = MutableStateFlow<String?>(null)
    val selectedClipId: StateFlow<String?> = _selectedClipId

    private val _selectedTrackId = MutableStateFlow<String?>(null)
    val selectedTrackId: StateFlow<String?> = _selectedTrackId

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting

    private val _exportProgress = MutableStateFlow(0f)
    val exportProgress: StateFlow<Float> = _exportProgress

    private val ffmpegEngine = FFmpegEngine(context)

    // Undo/Redo stacks
    private val undoStack = mutableListOf<Project>()
    private val redoStack = mutableListOf<Project>()
    private val maxUndoSteps = 50

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    init {
        viewModelScope.launch {
            ffmpegEngine.exportProgress.collect { _exportProgress.value = it }
            ffmpegEngine.isExporting.collect { _isExporting.value = it }
        }
    }

    fun createProject(name: String = "未命名项目") {
        _project.value = Project(name = name)
        undoStack.clear()
        redoStack.clear()
    }

    fun importVideo(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val path = copyToCache(uri) ?: return@launch
            val info = ffmpegEngine.getVideoInfo(path) ?: return@launch

            val clip = VideoClip(
                id = UUID.randomUUID().toString(),
                startUs = _project.value.totalDurationUs(),
                endUs = _project.value.totalDurationUs() + info.durationUs,
                trimStartUs = 0L,
                trimEndUs = info.durationUs,
                sourcePath = path,
                width = info.width,
                height = info.height,
                rotation = info.rotation
            )

            saveState()
            val currentTracks = _project.value.videoTracks
            if (currentTracks.isEmpty()) {
                _project.value = _project.value.copy(
                    videoTracks = listOf(VideoTrack(clips = listOf(clip))),
                    width = if (info.rotation % 180 == 0) info.width else info.height,
                    height = if (info.rotation % 180 == 0) info.height else info.width,
                    frameRate = info.frameRate
                )
            } else {
                val updatedTracks = currentTracks.map { track ->
                    track.copy(clips = track.clips + clip)
                }
                _project.value = _project.value.copy(videoTracks = updatedTracks)
            }
        }
    }

    fun importAudio(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val path = copyToCache(uri) ?: return@launch
            val info = ffmpegEngine.getVideoInfo(path) ?: return@launch

            val clip = AudioClip(
                id = UUID.randomUUID().toString(),
                startUs = _project.value.totalDurationUs(),
                endUs = _project.value.totalDurationUs() + info.durationUs,
                trimStartUs = 0L,
                trimEndUs = info.durationUs,
                sourcePath = path
            )

            saveState()
            val currentTracks = _project.value.audioTracks
            if (currentTracks.isEmpty()) {
                _project.value = _project.value.copy(
                    audioTracks = listOf(AudioTrack(clips = listOf(clip)))
                )
            } else {
                val updatedTracks = currentTracks.map { track ->
                    track.copy(clips = track.clips + clip)
                }
                _project.value = _project.value.copy(audioTracks = updatedTracks)
            }
        }
    }

    fun splitClip(clipId: String, positionUs: Long) {
        saveState()
        val project = _project.value

        val updatedVideoTracks = project.videoTracks.map { track ->
            val clipIndex = track.clips.indexOfFirst { it.id == clipId }
            if (clipIndex >= 0) {
                val clip = track.clips[clipIndex] as VideoClip
                val relativePos = positionUs - clip.startUs
                if (relativePos <= 0 || relativePos >= clip.durationUs) return@map track

                val clip1 = clip.copy(
                    endUs = positionUs,
                    trimEndUs = clip.trimStartUs + relativePos
                )
                val clip2 = clip.copy(
                    id = UUID.randomUUID().toString(),
                    startUs = positionUs,
                    trimStartUs = clip.trimStartUs + relativePos
                )
                track.copy(clips = track.clips.toMutableList().apply {
                    removeAt(clipIndex)
                    add(clipIndex, clip1)
                    add(clipIndex + 1, clip2)
                })
            } else track
        }

        _project.value = project.copy(videoTracks = updatedVideoTracks)
    }

    fun deleteClip(clipId: String) {
        saveState()
        val project = _project.value

        val updatedVideoTracks = project.videoTracks.map { track ->
            track.copy(clips = track.clips.filter { it.id != clipId })
        }
        val updatedAudioTracks = project.audioTracks.map { track ->
            track.copy(clips = track.clips.filter { it.id != clipId })
        }
        val updatedTextClips = project.textClips.filter { it.id != clipId }

        _project.value = project.copy(
            videoTracks = updatedVideoTracks,
            audioTracks = updatedAudioTracks,
            textClips = updatedTextClips
        )
        if (_selectedClipId.value == clipId) {
            _selectedClipId.value = null
        }
    }

    fun moveClip(clipId: String, newStartUs: Long) {
        saveState()
        val project = _project.value

        val updatedVideoTracks = project.videoTracks.map { track ->
            val clipIndex = track.clips.indexOfFirst { it.id == clipId }
            if (clipIndex >= 0) {
                val clip = track.clips[clipIndex]
                val duration = clip.durationUs
                val updatedClip = clip.copy(
                    startUs = newStartUs,
                    endUs = newStartUs + duration
                )
                track.copy(clips = track.clips.toMutableList().apply {
                    removeAt(clipIndex)
                    add(clipIndex, updatedClip)
                })
            } else track
        }

        _project.value = project.copy(videoTracks = updatedVideoTracks)
    }

    fun trimClip(clipId: String, newTrimStartUs: Long, newTrimEndUs: Long) {
        saveState()
        val project = _project.value

        val updatedVideoTracks = project.videoTracks.map { track ->
            val clipIndex = track.clips.indexOfFirst { it.id == clipId }
            if (clipIndex >= 0) {
                val clip = track.clips[clipIndex]
                val newDuration = newTrimEndUs - newTrimStartUs
                val updatedClip = clip.copy(
                    trimStartUs = newTrimStartUs,
                    trimEndUs = newTrimEndUs,
                    endUs = clip.startUs + newDuration
                )
                track.copy(clips = track.clips.toMutableList().apply {
                    removeAt(clipIndex)
                    add(clipIndex, updatedClip)
                })
            } else track
        }

        _project.value = project.copy(videoTracks = updatedVideoTracks)
    }

    fun addFilter(clipId: String, filter: Filter) {
        saveState()
        val project = _project.value

        val updatedVideoTracks = project.videoTracks.map { track ->
            val clipIndex = track.clips.indexOfFirst { it.id == clipId }
            if (clipIndex >= 0) {
                val clip = track.clips[clipIndex] as VideoClip
                val updatedClip = clip.copy(filters = clip.filters + filter)
                track.copy(clips = track.clips.toMutableList().apply {
                    removeAt(clipIndex)
                    add(clipIndex, updatedClip)
                })
            } else track
        }

        _project.value = project.copy(videoTracks = updatedVideoTracks)
    }

    fun removeFilter(clipId: String, filterId: String) {
        saveState()
        val project = _project.value

        val updatedVideoTracks = project.videoTracks.map { track ->
            val clipIndex = track.clips.indexOfFirst { it.id == clipId }
            if (clipIndex >= 0) {
                val clip = track.clips[clipIndex] as VideoClip
                val updatedClip = clip.copy(filters = clip.filters.filter { it.id != filterId })
                track.copy(clips = track.clips.toMutableList().apply {
                    removeAt(clipIndex)
                    add(clipIndex, updatedClip)
                })
            } else track
        }

        _project.value = project.copy(videoTracks = updatedVideoTracks)
    }

    fun addTextClip(textClip: TextClip) {
        saveState()
        _project.value = _project.value.copy(
            textClips = _project.value.textClips + textClip
        )
    }

    fun updateTextClip(textClip: TextClip) {
        saveState()
        _project.value = _project.value.copy(
            textClips = _project.value.textClips.map {
                if (it.id == textClip.id) textClip else it
            }
        )
    }

    fun setClipSpeed(clipId: String, speed: Float) {
        saveState()
        val project = _project.value

        val updatedVideoTracks = project.videoTracks.map { track ->
            val clipIndex = track.clips.indexOfFirst { it.id == clipId }
            if (clipIndex >= 0) {
                val clip = track.clips[clipIndex] as VideoClip
                val originalDuration = clip.trimEndUs - clip.trimStartUs
                val newDuration = (originalDuration / speed).toLong()
                val updatedClip = clip.copy(
                    speed = speed,
                    endUs = clip.startUs + newDuration
                )
                track.copy(clips = track.clips.toMutableList().apply {
                    removeAt(clipIndex)
                    add(clipIndex, updatedClip)
                })
            } else track
        }

        _project.value = project.copy(videoTracks = updatedVideoTracks)
    }

    fun setClipVolume(clipId: String, volume: Float) {
        saveState()
        val project = _project.value

        val updatedVideoTracks = project.videoTracks.map { track ->
            val clipIndex = track.clips.indexOfFirst { it.id == clipId }
            if (clipIndex >= 0) {
                val clip = track.clips[clipIndex] as VideoClip
                val updatedClip = clip.copy(volume = volume)
                track.copy(clips = track.clips.toMutableList().apply {
                    removeAt(clipIndex)
                    add(clipIndex, updatedClip)
                })
            } else track
        }

        _project.value = project.copy(videoTracks = updatedVideoTracks)
    }

    fun selectClip(clipId: String?) {
        _selectedClipId.value = clipId
    }

    fun selectTrack(trackId: String?) {
        _selectedTrackId.value = trackId
    }

    fun setCurrentPosition(positionUs: Long) {
        _currentPositionUs.value = positionUs
    }

    fun setPlaying(playing: Boolean) {
        _isPlaying.value = playing
    }

    // Undo/Redo
    private fun saveState() {
        undoStack.add(_project.value)
        if (undoStack.size > maxUndoSteps) {
            undoStack.removeAt(0)
        }
        redoStack.clear()
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        redoStack.add(_project.value)
        _project.value = undoStack.removeLast()
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        undoStack.add(_project.value)
        _project.value = redoStack.removeLast()
    }

    // Export
    fun export(config: ExportConfig, outputPath: String) {
        viewModelScope.launch {
            ffmpegEngine.exportProject(_project.value, config, outputPath)
        }
    }

    private fun copyToCache(uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = File(context.cacheDir, "import_${System.currentTimeMillis()}_${uri.lastPathSegment}")
            file.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    override fun onCleared() {
        super.onCleared()
        ffmpegEngine.cancelExport()
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return EditorViewModel(context) as T
        }
    }
}
