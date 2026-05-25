package com.myvideo.editor.core.video

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import java.io.File
import java.nio.ByteBuffer

class SynchronizedMuxer(private val outputPath: String) {
    private var muxer: MediaMuxer? = null
    private val trackMap = mutableMapOf<Int, Int>()
    private var started = false
    private val lock = Any()

    fun start() { synchronized(lock) { File(outputPath).parentFile?.mkdirs(); muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4) } }
    fun addTrack(format: MediaFormat): Int { synchronized(lock) { val t = muxer!!.addTrack(format); trackMap[t] = t; return t } }
    fun writeSample(trackIndex: Int, buffer: ByteBuffer, info: MediaCodec.BufferInfo) {
        synchronized(lock) {
            if (!started && trackMap.isNotEmpty()) { muxer?.start(); started = true }
            if (started) muxer?.writeSampleData(trackIndex, buffer, info)
        }
    }
    fun stop() { synchronized(lock) { try { muxer?.stop() } catch (_: Exception) {} } }
    fun release() { synchronized(lock) { try { muxer?.release() } catch (_: Exception) {}; muxer = null; started = false } }
}
