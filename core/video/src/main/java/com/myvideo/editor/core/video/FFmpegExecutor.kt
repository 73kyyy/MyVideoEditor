package com.myvideo.editor.core.video

import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.FFmpegSession
import com.arthenica.ffmpegkit.FFprobeKit
import com.arthenica.ffmpegkit.Log
import com.arthenica.ffmpegkit.ReturnCode
import com.arthenica.ffmpegkit.Statistics

class FFmpegExecutor {

    interface Callback {
        fun onProgress(percent: Float)
        fun onComplete(output: String)
        fun onError(error: String)
    }

    fun execute(command: String, callback: Callback) {
        Thread {
            try {
                FFmpegKit.executeAsync(command, { session: FFmpegSession ->
                    if (ReturnCode.isSuccess(session.returnCode)) {
                        val out = Regex("-y\\s+(.+)").find(command)?.groupValues?.get(1) ?: ""
                        callback.onComplete(out)
                    } else {
                        callback.onError(session.failStackTrace ?: "未知错误")
                    }
                }, { _: Log -> }, { stats: Statistics ->
                    callback.onProgress((stats.time.toFloat() / 1000).coerceIn(0f, 100f))
                })
            } catch (e: Exception) { callback.onError(e.message ?: "执行失败") }
        }.start()
    }

    fun executeSync(command: String): Boolean {
        return try { ReturnCode.isSuccess(FFmpegKit.execute(command).returnCode) }
        catch (e: Exception) { false }
    }

    fun getMediaInfo(path: String): String? {
        return try { FFprobeKit.getMediaInformation(path).mediaInformation?.format }
        catch (e: Exception) { null }
    }

    fun release() { FFmpegKitConfig.clearSessions() }
}
