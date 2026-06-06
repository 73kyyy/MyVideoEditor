package com.videoeditor.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.videoeditor.data.model.ExportConfig
import com.videoeditor.data.model.ExportFormat
import com.videoeditor.data.model.Project
import com.videoeditor.data.model.VideoCodec
import com.videoeditor.engine.ExportService
import com.videoeditor.engine.FFmpegEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class ExportViewModel(private val context: Context) : ViewModel() {

    private val _config = MutableStateFlow(ExportConfig())
    val config: StateFlow<ExportConfig> = _config

    private val _exportProgress = MutableStateFlow(0f)
    val exportProgress: StateFlow<Float> = _exportProgress

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting

    private val _exportResult = MutableStateFlow<ExportResult?>(null)
    val exportResult: StateFlow<ExportResult?> = _exportResult

    init {
        viewModelScope.launch {
            ExportService.exportProgress.collect { _exportProgress.value = it }
            ExportService.isExporting.collect { _isExporting.value = it }
        }
    }

    fun updateConfig(update: (ExportConfig) -> ExportConfig) {
        _config.value = update(_config.value)
    }

    fun setResolution(width: Int, height: Int) {
        updateConfig { it.copy(width = width, height = height) }
    }

    fun setFrameRate(frameRate: Int) {
        updateConfig { it.copy(frameRate = frameRate) }
    }

    fun setVideoBitrate(bitrate: Int) {
        updateConfig { it.copy(videoBitrate = bitrate) }
    }

    fun setCodec(codec: VideoCodec) {
        updateConfig { it.copy(codec = codec) }
    }

    fun setFormat(format: ExportFormat) {
        updateConfig { it.copy(format = format) }
    }

    fun startExport(project: Project) {
        val config = _config.value
        val outputDir = File(context.getExternalFilesDir(android.os.Environment.DIRECTORY_MOVIES), "VideoEditor")
        outputDir.mkdirs()
        val outputFile = File(outputDir, "export_${System.currentTimeMillis()}.${config.format.extension}")

        _exportResult.value = ExportResult.Started
        _isExporting.value = true

        viewModelScope.launch {
            val ffmpegEngine = FFmpegEngine(context)
            ffmpegEngine.exportProject(project, config, outputFile.absolutePath)
                .onSuccess {
                    _exportResult.value = ExportResult.Success(outputFile.absolutePath)
                }
                .onFailure {
                    _exportResult.value = ExportResult.Failed(it.message ?: "Unknown error")
                }
            _isExporting.value = false
        }
    }

    fun clearResult() {
        _exportResult.value = null
    }

    override fun onCleared() {
        super.onCleared()
    }

    sealed class ExportResult {
        object Started : ExportResult()
        data class Success(val outputPath: String) : ExportResult()
        data class Failed(val error: String) : ExportResult()
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ExportViewModel(context) as T
        }
    }
}
