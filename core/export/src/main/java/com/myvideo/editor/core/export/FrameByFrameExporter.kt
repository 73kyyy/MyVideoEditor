package com.myvideo.editor.core.export

import android.graphics.Bitmap

class FrameByFrameExporter {
    interface Callback {
        fun onFrame(frame: Int, total: Int)
        fun onComplete(outputPath: String)
        fun onError(error: String)
    }

    fun export(frames: List<Bitmap>, outputPath: String, fps: Int, callback: Callback) {
        Thread {
            try {
                val interval = 1000L / fps
                frames.forEachIndexed { i, frame ->
                    processFrame(frame, i, outputPath)
                    callback.onFrame(i, frames.size)
                    Thread.sleep(10)
                }
                callback.onComplete(outputPath)
            } catch (e: Exception) { callback.onError(e.message ?: "导出失败") }
        }.start()
    }

    private fun processFrame(frame: Bitmap, index: Int, outputPath: String) {
        // Process individual frame
    }

    fun estimateSize(width: Int, height: Int, fps: Int, durationSec: Float): Long {
        return (width.toLong() * height * 3 * fps * durationSec).toLong()
    }
}
