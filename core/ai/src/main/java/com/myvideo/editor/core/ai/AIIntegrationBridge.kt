package com.myvideo.editor.core.ai

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.myvideo.editor.core.security.membership.FeatureGate
import com.myvideo.editor.core.security.membership.MembershipValidator

/**
 * AI功能统一调用入口
 * 所有AI调用必须经过：联网验证 → 会员检查 → 本地推理
 */
class AIIntegrationBridge(private val context: Context) {

    private val deviceDetector = DeviceTierDetector.getInstance(context)
    private val registry = ModelRegistry(context)
    private val engine = AIEngine(context)
    private val validator = MembershipValidator()
    private val featureGate = FeatureGate(validator)

    enum class AIFeature(val id: String, val name: String, val modelId: String) {
        SEGMENT("segment", "智能抠图", "sam2_base"),
        SUPER_RES("superres", "超分辨率", "esrgan_x4"),
        INTERPOLATE("interpolate", "视频插帧", "rife_v4"),
        WHISPER("whisper", "语音转文字", "whisper_tiny"),
        DENOISE("denoise", "AI降噪", "rnnoise"),
        SEPARATE("separate", "人声分离", "demucs_ft")
    }

    data class AIResult(
        val success: Boolean,
        val output: Bitmap? = null,
        val outputFloats: FloatArray? = null,
        val elapsedMs: Long = 0,
        val errorMessage: String? = null,
        val requiresNetwork: Boolean = false,
        val requiresMembership: Boolean = false
    )

    /**
     * 检查是否可用（必须联网+会员）
     */
    fun checkAvailability(feature: AIFeature, isOnline: Boolean): AIResult {
        if (!isOnline) {
            return AIResult(false, errorMessage = "请连接网络使用", requiresNetwork = true)
        }
        if (validator.isFree()) {
            return AIResult(false, errorMessage = "开通会员解锁${feature.name}", requiresMembership = true)
        }
        if (!registry.isModelInstalled(feature.modelId)) {
            return AIResult(false, errorMessage = "模型未安装")
        }
        return AIResult(true)
    }

    /**
     * 调用AI抠图
     */
    fun segment(input: Bitmap, isOnline: Boolean, mask: FloatArray? = null): AIResult {
        val check = checkAvailability(AIFeature.SEGMENT, isOnline)
        if (!check.success) return check
        val result = engine.processImageChunked(input, AIFeature.SEGMENT.modelId) { chunk ->
            // NCNN推理占位
            chunk
        }
        return AIResult(result.success, result.output, elapsedMs = result.elapsedMs, errorMessage = result.errorMessage)
    }

    /**
     * 调用AI超分
     */
    fun superResolution(input: Bitmap, isOnline: Boolean, scale: Int = 4): AIResult {
        val check = checkAvailability(AIFeature.SUPER_RES, isOnline)
        if (!check.success) return check
        val result = engine.processImageChunked(input, AIFeature.SUPER_RES.modelId) { chunk ->
            // NCNN推理占位
            chunk
        }
        return AIResult(result.success, result.output, elapsedMs = result.elapsedMs, errorMessage = result.errorMessage)
    }

    /**
     * 调用AI插帧
     */
    fun interpolate(frame1: Bitmap, frame2: Bitmap, isOnline: Boolean): AIResult {
        val check = checkAvailability(AIFeature.INTERPOLATE, isOnline)
        if (!check.success) return check
        val result = engine.processVideoFrame(frame1, AIFeature.INTERPOLATE.modelId) { frame ->
            frame
        }
        return AIResult(result.success, result.output, elapsedMs = result.elapsedMs, errorMessage = result.errorMessage)
    }

    /**
     * 调用Whisper语音转文字
     */
    fun whisperTranscribe(audioSamples: FloatArray, isOnline: Boolean): AIResult {
        val check = checkAvailability(AIFeature.WHISPER, isOnline)
        if (!check.success) return check
        val result = engine.processAudioSegmented(audioSamples, AIFeature.WHISPER.modelId) { segment ->
            segment
        }
        return AIResult(result.success, outputFloats = result.outputFloats, elapsedMs = result.elapsedMs, errorMessage = result.errorMessage)
    }

    /**
     * 调用AI降噪
     */
    fun denoise(audioSamples: FloatArray, isOnline: Boolean): AIResult {
        val check = checkAvailability(AIFeature.DENOISE, isOnline)
        if (!check.success) return check
        val result = engine.processAudioSegmented(audioSamples, AIFeature.DENOISE.modelId) { segment ->
            segment
        }
        return AIResult(result.success, outputFloats = result.outputFloats, elapsedMs = result.elapsedMs, errorMessage = result.errorMessage)
    }

    /**
     * 调用人声分离
     */
    fun separate(audioSamples: FloatArray, isOnline: Boolean): AIResult {
        val check = checkAvailability(AIFeature.SEPARATE, isOnline)
        if (!check.success) return check
        val result = engine.processAudioSegmented(audioSamples, AIFeature.SEPARATE.modelId) { segment ->
            segment
        }
        return AIResult(result.success, outputFloats = result.outputFloats, elapsedMs = result.elapsedMs, errorMessage = result.errorMessage)
    }

    fun getDeviceInfo() = deviceDetector.detect()
    fun getRegistry() = registry
    fun release() { engine.release() }
}
