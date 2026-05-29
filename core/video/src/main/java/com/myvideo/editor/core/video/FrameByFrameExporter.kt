package com.myvideo.editor.core.video

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.view.Surface
import com.myvideo.editor.core.video.codec.HardwareVideoEncoder

class FrameByFrameExporter(private val encoder: HardwareVideoEncoder) {

    data class ExportConfig(val width: Int, val height: Int, val fps: Int, val outputPath: String)

    interface Callback {
        fun onFrameExported(frame: Int, total: Int)
        fun onComplete(outputPath: String)
        fun onError(error: String)
    }

    fun exportFromBitmaps(frames: List<Bitmap>, config: ExportConfig, callback: Callback) {
        Thread {
            try {
                val surface: Surface = encoder.init(config.outputPath)
                    ?: throw Exception("编码器初始化失败")
                frames.forEachIndexed { i, bitmap ->
                    val canvas = surface.lockCanvas(null)
                    canvas.drawBitmap(bitmap, 0f, 0f, null)
                    surface.unlockCanvasAndPost(canvas)
                    encoder.drainEncoder(false)
                    callback.onFrameExported(i, frames.size)
                }
                encoder.drainEncoder(true)
                encoder.release()
                callback.onComplete(config.outputPath)
            } catch (e: Exception) {
                callback.onError(e.message ?: "导出失败")
                encoder.release()
            }
        }.start()
    }

    fun exportFromVideo(inputPath: String, config: ExportConfig, callback: Callback) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(inputPath)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L
            val interval = 1000L / config.fps
            val frames = mutableListOf<Bitmap>()
            var t = 0L
            while (t < duration) {
                val frame = retriever.getFrameAtTime(
                    t * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                )
                if (frame != null) frames.add(frame)
                t += interval
            }
            exportFromBitmaps(frames, config, callback)
        } catch (e: Exception) {
            callback.onError(e.message ?: "读取视频失败")
        } finally {
            retriever.release()
        }
    }
}
