package com.myvideo.editor.core.video.codec

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.view.Surface
import java.io.File
import java.nio.ByteBuffer

class HardwareVideoEncoder(private val config: EncoderSelector.EncodeConfig) {

    private var encoder: MediaCodec? = null
    private var muxer: MediaMuxer? = null
    private var inputSurface: Surface? = null
    private var muxerTrack = -1
    private var muxerStarted = false
    private val bufferInfo = MediaCodec.BufferInfo()

    fun init(outputPath: String): Surface? {
        return try {
            File(outputPath).parentFile?.mkdirs()
            val format = MediaFormat.createVideoFormat(config.codec, config.width, config.height).apply {
                setInteger(MediaFormat.KEY_BIT_RATE, config.bitrate)
                setInteger(MediaFormat.KEY_FRAME_RATE, config.fps)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            }
            encoder = MediaCodec.createEncoderByType(config.codec).apply {
                configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                inputSurface = createInputSurface()
                start()
            }
            muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            inputSurface
        } catch (e: Exception) { null }
    }

    fun drainEncoder(endOfStream: Boolean): Boolean {
        val enc = encoder ?: return false
        if (endOfStream) enc.signalEndOfInputStream()
        val timeout = 10_000L
        while (true) {
            val idx = enc.dequeueOutputBuffer(bufferInfo, timeout)
            if (idx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                muxerTrack = muxer!!.addTrack(enc.outputFormat)
                muxer!!.start(); muxerStarted = true
            } else if (idx >= 0) {
                val buf = enc.getOutputBuffer(idx) ?: return false
                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG == 0 && muxerStarted) {
                    muxer!!.writeSampleData(muxerTrack, buf, bufferInfo)
                }
                enc.releaseOutputBuffer(idx, false)
                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) return true
            } else break
        }
        return false
    }

    fun release() {
        try { encoder?.stop(); encoder?.release() } catch (_: Exception) {}
        try { muxer?.stop(); muxer?.release() } catch (_: Exception) {}
        inputSurface?.release()
        encoder = null; muxer = null; inputSurface = null
    }
}
