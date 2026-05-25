package com.myvideo.editor.core.performance

import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

class IoWriteBuffer(private val outputPath: String, private val bufferSize: Int = 1024 * 1024) {
    private var output: FileOutputStream? = null
    private var buffer: ByteBuffer? = null
    private var bytesWritten = 0L

    fun open() {
        File(outputPath).parentFile?.mkdirs()
        output = FileOutputStream(outputPath)
        buffer = ByteBuffer.allocate(bufferSize)
    }

    fun write(data: ByteArray) {
        val buf = buffer ?: return
        if (buf.remaining() < data.size) flush()
        buf.put(data)
    }

    fun flush() {
        val buf = buffer ?: return
        if (buf.position() > 0) {
            buf.flip()
            val arr = ByteArray(buf.remaining())
            buf.get(arr)
            output?.write(arr)
            bytesWritten += arr.size
            buf.clear()
        }
    }

    fun close() { flush(); output?.flush(); output?.close(); output = null; buffer = null }
    fun getBytesWritten(): Long = bytesWritten
}
