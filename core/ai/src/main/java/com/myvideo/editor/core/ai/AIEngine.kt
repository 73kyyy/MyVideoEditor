package com.myvideo.editor.core.ai

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean

class AIEngine(private val context: Context) {

    private val deviceInfo = DeviceTierDetector.getInstance(context).detect()
    private val registry = ModelRegistry(context)
    private val isRunning = AtomicBoolean(false)

    data class InferenceResult(
        val success: Boolean,
        val output: Bitmap? = null,
        val outputFloats: FloatArray? = null,
        val elapsedMs: Long = 0,
        val chunksProcessed: Int = 0,
        val errorMessage: String? = null
    )

    data class ProcessingConfig(
        val chunkSize: Int = deviceInfo.tier.chunkSize,
        val maxThreads: Int = deviceInfo.tier.maxThreads,
        val gcInterval: Int = deviceInfo.tier.gcInterval,
        val maxMemoryMb: Int = getMaxMemoryMb(),
        val timeoutMs: Long = when (deviceInfo.tier) {
            DeviceTierDetector.Tier.T1 -> 300_000L
            DeviceTierDetector.Tier.T2 -> 180_000L
            DeviceTierDetector.Tier.T3 -> 60_000L
        }
    )

    private fun getMaxMemoryMb(): Int {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)
        val availMb = (memInfo.availMem / (1024 * 1024)).toInt()
        return when (deviceInfo.tier) {
            DeviceTierDetector.Tier.T1 -> (availMb * 0.3f).toInt().coerceIn(128, 512)
            DeviceTierDetector.Tier.T2 -> (availMb * 0.4f).toInt().coerceIn(256, 1024)
            DeviceTierDetector.Tier.T3 -> (availMb * 0.5f).toInt().coerceIn(512, 2048)
        }
    }

    fun getDeviceInfo() = deviceInfo
    fun getConfig() = ProcessingConfig()
    fun isProcessing() = isRunning.get()

    fun processImageChunked(input: Bitmap, modelId: String, processChunk: (Bitmap) -> Bitmap?): InferenceResult {
        if (!registry.isModelInstalled(modelId)) return InferenceResult(false, errorMessage = "模型未安装: $modelId")
        if (isRunning.getAndSet(true)) return InferenceResult(false, errorMessage = "正在处理中，请等待")
        val startTime = System.currentTimeMillis()
        val config = getConfig()
        val cs = config.chunkSize
        val w = input.width; val h = input.height
        return try {
            if (w <= cs && h <= cs) {
                val r = processChunk(input)
                return InferenceResult(r != null, r, elapsedMs = System.currentTimeMillis() - startTime, chunksProcessed = 1)
            }
            val output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(output)
            var chunks = 0; var y = 0
            while (y < h) { var x = 0
                while (x < w) {
                    val cw = minOf(cs, w - x); val ch = minOf(cs, h - y)
                    if (isMemoryPressureHigh()) {
                        val chunk = Bitmap.createBitmap(input, x, y, cw, ch)
                        val scaled = Bitmap.createScaledBitmap(chunk, cw / 2, ch / 2, false)
                        chunk.recycle()
                        val r = processChunk(scaled)
                        if (r != null) { val res = Bitmap.createScaledBitmap(r, cw, ch, false); canvas.drawBitmap(res, x.toFloat(), y.toFloat(), null); res.recycle() }
                        scaled.recycle()
                    } else {
                        val chunk = Bitmap.createBitmap(input, x, y, cw, ch)
                        val r = processChunk(chunk)
                        if (r != null) canvas.drawBitmap(r, x.toFloat(), y.toFloat(), null)
                        chunk.recycle()
                    }
                    chunks++; x += cs
                    if (config.gcInterval > 0 && chunks % config.gcInterval == 0) { System.gc(); Thread.sleep(50) }
                }
                y += cs
            }
            InferenceResult(true, output, elapsedMs = System.currentTimeMillis() - startTime, chunksProcessed = chunks)
        } catch (e: OutOfMemoryError) { System.gc(); InferenceResult(false, errorMessage = "内存不足，请关闭其他应用后重试")
        } catch (e: Exception) { InferenceResult(false, errorMessage = "处理失败: ${e.message}")
        } finally { isRunning.set(false) }
    }

    fun processVideoFrame(frame: Bitmap, modelId: String, processFrame: (Bitmap) -> Bitmap?): InferenceResult {
        if (!registry.isModelInstalled(modelId)) return InferenceResult(false, errorMessage = "模型未安装")
        val start = System.currentTimeMillis()
        return try { val r = processFrame(frame); InferenceResult(r != null, r, elapsedMs = System.currentTimeMillis() - start)
        } catch (e: Exception) { InferenceResult(false, errorMessage = e.message) }
    }

    fun processAudioSegmented(samples: FloatArray, modelId: String, process: (FloatArray) -> FloatArray?): InferenceResult {
        if (!registry.isModelInstalled(modelId)) return InferenceResult(false, errorMessage = "模型未安装")
        val start = System.currentTimeMillis()
        val segSize = when (deviceInfo.tier) { DeviceTierDetector.Tier.T1 -> 16000 * 5; DeviceTierDetector.Tier.T2 -> 16000 * 15; DeviceTierDetector.Tier.T3 -> samples.size }
        return try {
            if (samples.size <= segSize) { val r = process(samples); return InferenceResult(r != null, outputFloats = r, elapsedMs = System.currentTimeMillis() - start) }
            val output = FloatArray(samples.size); var off = 0
            while (off < samples.size) { val len = minOf(segSize, samples.size - off); val seg = samples.copyOfRange(off, off + len); val r = process(seg); if (r != null) r.copyInto(output, off, 0, minOf(r.size, len)); off += segSize; if (deviceInfo.tier == DeviceTierDetector.Tier.T1) Thread.sleep(100) }
            InferenceResult(true, outputFloats = output, elapsedMs = System.currentTimeMillis() - start)
        } catch (e: Exception) { InferenceResult(false, errorMessage = e.message) }
    }

    private fun isMemoryPressureHigh(): Boolean {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo(); am.getMemoryInfo(memInfo)
        return memInfo.availMem < memInfo.threshold * 1.5
    }

    fun cancel() { isRunning.set(false) }
    fun release() { isRunning.set(false) }
}
