package com.videoeditor.engine

import android.content.Context
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.ReturnCode
import com.videoeditor.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File

class FFmpegEngine(private val context: Context) {

    private val _exportProgress = MutableStateFlow(0f)
    val exportProgress: StateFlow<Float> = _exportProgress

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting

    suspend fun exportProject(
        project: Project,
        config: ExportConfig,
        outputPath: String
    ): Result<String> = withContext(Dispatchers.IO) {
        _isExporting.value = true
        _exportProgress.value = 0f

        try {
            val command = buildExportCommand(project, config, outputPath)
            val session = FFmpegKit.execute(command)

            if (ReturnCode.isSuccess(session.returnCode)) {
                _exportProgress.value = 1f
                Result.success(outputPath)
            } else {
                Result.failure(Exception("Export failed: ${session.failStackTrace}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            _isExporting.value = false
        }
    }

    private fun buildExportCommand(
        project: Project,
        config: ExportConfig,
        outputPath: String
    ): String {
        val videoClips = project.videoTracks.flatMap { it.clips }
        if (videoClips.isEmpty()) return ""

        val concatFile = createConcatFile(project)

        val baseCmd = StringBuilder()
        baseCmd.append("-f concat -safe 0 -i \"${concatFile.absolutePath}\"")

        // Audio from video clips
        val hasAudioTrack = project.audioTracks.isNotEmpty()
        if (hasAudioTrack) {
            val audioConcatFile = createAudioConcatFile(project)
            baseCmd.append(" -i \"${audioConcatFile.absolutePath}\"")
        }

        // Video filters
        val filters = mutableListOf<String>()

        // Scale
        filters.add("scale=${config.width}:${config.height}")

        // Apply clip filters
        val clipFilters = videoClips.flatMap { it.filters }
        clipFilters.forEach { filter ->
            when (filter.type) {
                FilterType.BRIGHTNESS -> filters.add("eq=brightness=${(filter.intensity - 0.5f) * 2}")
                FilterType.CONTRAST -> filters.add("eq=contrast=${filter.intensity}")
                FilterType.SATURATION -> filters.add("eq=saturation=${filter.intensity}")
                FilterType.BLACK_WHITE -> filters.add("hue=s=0")
                FilterType.SEPIA -> filters.add("colorchannelmixer=.393:.769:.189:0:.349:.686:.168:0:.272:.534:.131")
                FilterType.VIGNETTE -> filters.add("vignette=angle=0.5")
                FilterType.BLUR -> filters.add("boxblur=${(filter.intensity * 10).toInt()}")
                FilterType.SHARPEN -> filters.add("unsharp=5:5:${filter.intensity * 2}")
                else -> {}
            }
        }

        if (filters.isNotEmpty()) {
            baseCmd.append(" -vf \"${filters.joinToString(",")}\"")
        }

        // Codec settings
        baseCmd.append(" -c:v ${config.codec.ffmpegName}")
        baseCmd.append(" -b:v ${config.videoBitrate}")
        baseCmd.append(" -r ${config.frameRate}")
        baseCmd.append(" -c:a aac -b:a ${config.audioBitrate}")
        baseCmd.append(" -ar ${config.audioSampleRate}")
        baseCmd.append(" -ac ${config.audioChannels}")
        baseCmd.append(" -y \"$outputPath\"")

        return baseCmd.toString()
    }

    private fun createConcatFile(project: Project): File {
        val concatFile = File(context.cacheDir, "concat_${System.currentTimeMillis()}.txt")
        val videoClips = project.videoTracks.flatMap { it.clips }.sortedBy { it.startUs }

        val content = videoClips.joinToString("\n") { clip ->
            val trimStartSec = clip.trimStartUs / 1_000_000.0
            val durationSec = (clip.trimEndUs - clip.trimStartUs) / 1_000_000.0
            "file '${clip.sourcePath}'\ninpoint $trimStartSec\noutpoint ${trimStartSec + durationSec}"
        }

        concatFile.writeText(content)
        return concatFile
    }

    private fun createAudioConcatFile(project: Project): File {
        val concatFile = File(context.cacheDir, "audio_concat_${System.currentTimeMillis()}.txt")
        val audioClips = project.audioTracks.flatMap { it.clips }.sortedBy { it.startUs }

        val content = audioClips.joinToString("\n") { clip ->
            val trimStartSec = clip.trimStartUs / 1_000_000.0
            val durationSec = (clip.trimEndUs - clip.trimStartUs) / 1_000_000.0
            "file '${clip.sourcePath}'\ninpoint $trimStartSec\noutpoint ${trimStartSec + durationSec}"
        }

        concatFile.writeText(content)
        return concatFile
    }

    fun getVideoInfo(path: String): VideoInfo? {
        return try {
            val mediaInformation = FFmpegKitConfig.getMediaInformation(path)
            val stream = mediaInformation.streams.firstOrNull()
            VideoInfo(
                width = stream?.width?.toIntOrNull() ?: 0,
                height = stream?.height?.toIntOrNull() ?: 0,
                durationUs = (mediaInformation.duration?.toLongOrNull() ?: 0L) * 1000,
                frameRate = stream?.rFrameRate?.split("/")?.firstOrNull()?.toIntOrNull() ?: 30,
                rotation = stream?.displayAspectRatio?.toIntOrNull() ?: 0,
                bitrate = mediaInformation.bitrate?.toLongOrNull() ?: 0L
            )
        } catch (e: Exception) {
            null
        }
    }

    fun cancelExport() {
        FFmpegKit.cancel()
    }
}

data class VideoInfo(
    val width: Int,
    val height: Int,
    val durationUs: Long,
    val frameRate: Int,
    val rotation: Int,
    val bitrate: Long
)
