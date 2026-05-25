package com.myvideo.editor.core.video

import android.content.Context
import android.graphics.Bitmap

class VideoProcessor(private val context: Context) {

    private val executor = FFmpegExecutor()

    fun trim(input: String, output: String, startMs: Long, endMs: Long, cb: FFmpegExecutor.Callback) {
        executor.execute("-i $input -ss ${startMs/1000f} -t ${(endMs-startMs)/1000f} -c:v libx264 -preset fast -c:a aac -y $output", cb)
    }

    fun concat(inputs: List<String>, output: String, cb: FFmpegExecutor.Callback) {
        val listFile = java.io.File(context.cacheDir, "concat.txt")
        listFile.writeText(inputs.joinToString("\n") { "file '$it'" })
        executor.execute("-f concat -safe 0 -i ${listFile.absolutePath} -c copy -y $output", cb)
    }

    fun changeSpeed(input: String, output: String, speed: Float, cb: FFmpegExecutor.Callback) {
        val vf = "setpts=${1f/speed}*PTS"
        val af = "atempo=${speed.coerceIn(0.5f, 2f)}"
        executor.execute("-i $input -vf $vf -af $af -c:v libx264 -preset fast -y $output", cb)
    }

    fun reverse(input: String, output: String, cb: FFmpegExecutor.Callback) {
        executor.execute("-i $input -vf reverse -af areverse -c:v libx264 -preset fast -y $output", cb)
    }

    fun export(input: String, output: String, width: Int, height: Int, fps: Int, bitrate: String, cb: FFmpegExecutor.Callback) {
        val cmd = "-i $input -vf scale=$width:$height:force_original_aspect_ratio=decrease,pad=$width:$height:(ow-iw)/2:(oh-ih)/2,fps=$fps -c:v libx264 -preset fast -crf 23 -b:v $bitrate -c:a aac -b:a 128k -movflags +faststart -y $output"
        executor.execute(cmd, cb)
    }

    fun release() { executor.release() }
}
