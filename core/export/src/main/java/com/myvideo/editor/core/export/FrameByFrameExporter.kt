package com.myvideo.editor.core.export

import android.graphics.Bitmap
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.ReturnCode
import java.io.File
import java.io.FileOutputStream

class FrameByFrameExporter {
    interface Callback {
        fun onFrame(frame: Int, total: Int)
        fun onComplete(outputPath: String)
        fun onError(error: String)
    }

    fun export(frames: List<Bitmap>, outputPath: String, fps: Int, callback: Callback) {
        Thread {
            try {
                val tempDir = File(FFmpegKitConfig.getCacheDirectory(), "export_frames")
                tempDir.mkdirs()

                // Write frames as PNG images
                frames.forEachIndexed { i, frame ->
                    val frameFile = File(tempDir, String.format("frame_%06d.png", i))
                    FileOutputStream(frameFile).use { fos ->
                        frame.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    }
                    callback.onFrame(i, frames.size)
                }

                // Use FFmpeg to encode frames into video
                val inputPattern = File(tempDir, "frame_%06d.png").absolutePath
                val ffmpegCmd = "-y -framerate $fps -i \"$inputPattern\" -c:v libx264 -preset medium -crf 23 -pix_fmt yuv420p \"$outputPath\""

                val session = FFmpegKit.execute(ffmpegCmd)

                // Cleanup temp frames
                tempDir.listFiles()?.forEach { it.delete() }
                tempDir.delete()

                if (ReturnCode.isSuccess(session.returnCode)) {
                    callback.onComplete(outputPath)
                } else {
                    callback.onError("FFmpeg error: ${session.allLogs.joinToString("\n") { it.message }}")
                }
            } catch (e: Exception) {
                callback.onError(e.message ?: "导出失败")
            }
        }.start()
    }

    fun exportWithAudio(
        frames: List<Bitmap>, audioPath: String?,
        outputPath: String, fps: Int, callback: Callback
    ) {
        Thread {
            try {
                val tempDir = File(FFmpegKitConfig.getCacheDirectory(), "export_frames")
                tempDir.mkdirs()

                // Write frames as PNG
                frames.forEachIndexed { i, frame ->
                    val frameFile = File(tempDir, String.format("frame_%06d.png", i))
                    FileOutputStream(frameFile).use { fos ->
                        frame.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    }
                    callback.onFrame(i, frames.size)
                }

                val inputPattern = File(tempDir, "frame_%06d.png").absolutePath
                val cmd = StringBuilder("-y -framerate $fps -i \"$inputPattern\"")

                if (audioPath != null && File(audioPath).exists()) {
                    cmd.append(" -i \"$audioPath\" -c:a aac -b:a 128k")
                }

                cmd.append(" -c:v libx264 -preset medium -crf 23 -pix_fmt yuv420p")
                cmd.append(" -shortest \"$outputPath\"")

                val session = FFmpegKit.execute(cmd.toString())

                // Cleanup
                tempDir.listFiles()?.forEach { it.delete() }
                tempDir.delete()

                if (ReturnCode.isSuccess(session.returnCode)) {
                    callback.onComplete(outputPath)
                } else {
                    callback.onError("FFmpeg error: ${session.allLogs.joinToString("\n") { it.message }}")
                }
            } catch (e: Exception) {
                callback.onError(e.message ?: "导出失败")
            }
        }.start()
    }

    private fun processFrame(frame: Bitmap, index: Int, outputPath: String) {
        // Frame processing is now handled by FFmpeg
    }

    fun estimateSize(width: Int, height: Int, fps: Int, durationSec: Float): Long {
        // H.264 at CRF 23 roughly: width * height * fps * durationSec * 0.1 bytes
        return (width.toLong() * height * fps * durationSec * 0.1).toLong()
    }
}
