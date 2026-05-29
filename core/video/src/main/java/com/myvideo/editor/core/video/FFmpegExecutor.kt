package com.myvideo.editor.core.video

import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.FFmpegSessionCompleteCallback
import com.arthenica.ffmpegkit.FFprobeKit
import com.arthenica.ffmpegkit.ReturnCode

class FFmpegExecutor {

    interface Callback {
        fun onProgress(percent: Float)
        fun onComplete(output: String)
        fun onError(error: String)
    }

    fun execute(command: String, callback: Callback) {
        Thread {
            try {
                val cb = FFmpegSessionCompleteCallback { session ->
                    if (ReturnCode.isSuccess(session.returnCode)) {
                        val out = Regex("-y\\s+(.+)").find(command)?.groupValues?.get(1) ?: ""
                        callback.onComplete(out)
                    } else {
                        callback.onError(session.failStackTrace ?: "未知错误")
                    }
                }
                FFmpegKit.executeAsync(command, cb)
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
