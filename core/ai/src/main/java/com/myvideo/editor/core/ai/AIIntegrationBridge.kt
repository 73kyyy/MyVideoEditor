package com.myvideo.editor.core.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PointF
import android.util.Log
import com.myvideo.editor.core.ai.common.InferenceSessionManager
import com.myvideo.editor.core.ai.denoise.RNNoiseWrapper
import com.myvideo.editor.core.ai.enhancement.ESRGANWrapper
import com.myvideo.editor.core.ai.interpolation.RIFEWrapper
import com.myvideo.editor.core.ai.segmentation.SAM2Wrapper
import com.myvideo.editor.core.ai.separation.DemucsWrapper
import com.myvideo.editor.core.ai.speech.WhisperWrapper
import com.myvideo.editor.core.security.membership.FeatureGate
import com.myvideo.editor.core.security.membership.MembershipValidator
import java.io.File

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

    // ONNX Runtime session manager (shared across all models)
    private val sessionManager = InferenceSessionManager()

    // Model wrappers (lazy initialized)
    private var rifeWrapper: RIFEWrapper? = null
    private var esrganWrapper: ESRGANWrapper? = null
    private var sam2Wrapper: SAM2Wrapper? = null
    private var whisperWrapper: WhisperWrapper? = null
    private var demucsWrapper: DemucsWrapper? = null
    private var rnnoiseWrapper: RNNoiseWrapper? = null

    private var isInitialized = false

    enum class AIFeature(val id: String, val displayName: String, val modelId: String) {
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
        val text: String? = null,
        val elapsedMs: Long = 0,
        val errorMessage: String? = null,
        val requiresNetwork: Boolean = false,
        val requiresMembership: Boolean = false
    )

    fun initialize(): Boolean {
        if (isInitialized) return true
        if (!sessionManager.init()) {
            Log.e(TAG, "Failed to initialize ONNX Runtime")
            return false
        }
        isInitialized = true
        return true
    }

    private fun ensureModelExtracted(modelId: String): Boolean {
        if (!registry.isModelInstalled(modelId)) {
            return registry.extractModelFromAssets(modelId)
        }
        return true
    }

    private fun getLocalPath(assetPath: String): String {
        return File(context.filesDir, assetPath).absolutePath
    }

    /**
     * 检查是否可用（必须联网+会员）
     */
    fun checkAvailability(feature: AIFeature, isOnline: Boolean): AIResult {
        if (!isOnline) {
            return AIResult(false, errorMessage = "请连接网络使用", requiresNetwork = true)
        }
        if (validator.isFree()) {
            return AIResult(false, errorMessage = "开通会员解锁${feature.displayName}", requiresMembership = true)
        }
        if (!ensureModelExtracted(feature.modelId)) {
            return AIResult(false, errorMessage = "模型未安装")
        }
        return AIResult(true)
    }

    /**
     * 调用AI抠图 (MobileSAM)
     */
    fun segment(input: Bitmap, isOnline: Boolean, point: PointF = PointF(input.width / 2f, input.height / 2f)): AIResult {
        val check = checkAvailability(AIFeature.SEGMENT, isOnline)
        if (!check.success) return check
        if (!initialize()) return AIResult(false, errorMessage = "ONNX Runtime初始化失败")

        val start = System.currentTimeMillis()
        return try {
            val wrapper = sam2Wrapper ?: run {
                val paths = registry.getAllAssetPaths(AIFeature.SEGMENT.modelId)
                val w = SAM2Wrapper(sessionManager)
                val encPath = getLocalPath(paths.getOrElse(0) { "ai_models/sam_encoder.onnx" })
                val decPath = getLocalPath(paths.getOrElse(1) { "ai_models/sam_decoder.onnx" })
                if (!w.init(encPath, decPath)) {
                    return AIResult(false, errorMessage = "SAM2模型加载失败")
                }
                sam2Wrapper = w
                w
            }
            val result = wrapper.segment(input, point)
            AIResult(result != null, result, elapsedMs = System.currentTimeMillis() - start)
        } catch (e: Exception) {
            AIResult(false, errorMessage = "抠图失败: ${e.message}")
        }
    }

    /**
     * 调用AI超分 (RealESRGAN)
     */
    fun superResolution(input: Bitmap, isOnline: Boolean, scale: Int = 4): AIResult {
        val check = checkAvailability(AIFeature.SUPER_RES, isOnline)
        if (!check.success) return check
        if (!initialize()) return AIResult(false, errorMessage = "ONNX Runtime初始化失败")

        val start = System.currentTimeMillis()
        return try {
            val wrapper = esrganWrapper ?: run {
                val path = registry.getAllAssetPaths(AIFeature.SUPER_RES.modelId).firstOrNull()
                    ?: "ai_models/realesrgan_x4plus.onnx"
                val w = ESRGANWrapper(sessionManager)
                if (!w.init(getLocalPath(path))) {
                    return AIResult(false, errorMessage = "ESRGAN模型加载失败")
                }
                esrganWrapper = w
                w
            }
            val result = if (scale == 2) wrapper.upscale2x(input) else wrapper.upscale4x(input)
            AIResult(result != null, result, elapsedMs = System.currentTimeMillis() - start)
        } catch (e: Exception) {
            AIResult(false, errorMessage = "超分失败: ${e.message}")
        }
    }

    /**
     * 调用AI插帧 (RIFE)
     */
    fun interpolate(frame1: Bitmap, frame2: Bitmap, isOnline: Boolean, t: Float = 0.5f): AIResult {
        val check = checkAvailability(AIFeature.INTERPOLATE, isOnline)
        if (!check.success) return check
        if (!initialize()) return AIResult(false, errorMessage = "ONNX Runtime初始化失败")

        val start = System.currentTimeMillis()
        return try {
            val wrapper = rifeWrapper ?: run {
                val path = registry.getAllAssetPaths(AIFeature.INTERPOLATE.modelId).firstOrNull()
                    ?: "ai_models/rife_v4.onnx"
                val w = RIFEWrapper(sessionManager)
                if (!w.init(getLocalPath(path))) {
                    return AIResult(false, errorMessage = "RIFE模型加载失败")
                }
                rifeWrapper = w
                w
            }
            val result = wrapper.interpolate(frame1, frame2, t)
            AIResult(result != null, result, elapsedMs = System.currentTimeMillis() - start)
        } catch (e: Exception) {
            AIResult(false, errorMessage = "插帧失败: ${e.message}")
        }
    }

    /**
     * 调用Whisper语音转文字
     */
    fun whisperTranscribe(audioSamples: FloatArray, isOnline: Boolean, sampleRate: Int = 16000): AIResult {
        val check = checkAvailability(AIFeature.WHISPER, isOnline)
        if (!check.success) return check
        if (!initialize()) return AIResult(false, errorMessage = "ONNX Runtime初始化失败")

        val start = System.currentTimeMillis()
        return try {
            val wrapper = whisperWrapper ?: run {
                val paths = registry.getAllAssetPaths(AIFeature.WHISPER.modelId)
                val w = WhisperWrapper(sessionManager)
                val encPath = getLocalPath(paths.getOrElse(0) { "ai_models/whisper_encoder.onnx" })
                val decPath = getLocalPath(paths.getOrElse(1) { "ai_models/whisper_decoder.onnx" })
                val tokPath = getLocalPath(paths.getOrElse(2) { "ai_models/whisper_tokens.txt" })
                if (!w.init(encPath, decPath, tokPath)) {
                    return AIResult(false, errorMessage = "Whisper模型加载失败")
                }
                whisperWrapper = w
                w
            }
            val result = wrapper.transcribeFromPcm(audioSamples, sampleRate)
            AIResult(result != null, text = result?.text, elapsedMs = System.currentTimeMillis() - start)
        } catch (e: Exception) {
            AIResult(false, errorMessage = "语音识别失败: ${e.message}")
        }
    }

    /**
     * 调用AI降噪 (RNNoise)
     */
    fun denoise(audioSamples: FloatArray, isOnline: Boolean): AIResult {
        val check = checkAvailability(AIFeature.DENOISE, isOnline)
        if (!check.success) return check
        if (!initialize()) return AIResult(false, errorMessage = "ONNX Runtime初始化失败")

        val start = System.currentTimeMillis()
        return try {
            val wrapper = rnnoiseWrapper ?: run {
                val path = registry.getAllAssetPaths(AIFeature.DENOISE.modelId).firstOrNull()
                    ?: "ai_models/rnnoise.onnx"
                val w = RNNoiseWrapper(sessionManager)
                if (!w.init(getLocalPath(path))) {
                    return AIResult(false, errorMessage = "RNNoise模型加载失败")
                }
                rnnoiseWrapper = w
                w
            }
            val result = wrapper.denoiseChunked(audioSamples)
            AIResult(true, outputFloats = result, elapsedMs = System.currentTimeMillis() - start)
        } catch (e: Exception) {
            AIResult(false, errorMessage = "降噪失败: ${e.message}")
        }
    }

    /**
     * 调用人声分离 (Demucs)
     */
    fun separate(audioSamples: FloatArray, isOnline: Boolean, sampleRate: Int = 44100): AIResult {
        val check = checkAvailability(AIFeature.SEPARATE, isOnline)
        if (!check.success) return check
        if (!initialize()) return AIResult(false, errorMessage = "ONNX Runtime初始化失败")

        val start = System.currentTimeMillis()
        return try {
            val wrapper = demucsWrapper ?: run {
                val path = registry.getAllAssetPaths(AIFeature.SEPARATE.modelId).firstOrNull()
                    ?: "ai_models/demucs_htdemucs.onnx"
                val w = DemucsWrapper(sessionManager)
                if (!w.init(getLocalPath(path))) {
                    return AIResult(false, errorMessage = "Demucs模型加载失败")
                }
                demucsWrapper = w
                w
            }
            val result = wrapper.separate(audioSamples, sampleRate)
            AIResult(result != null, outputFloats = result?.vocals, elapsedMs = System.currentTimeMillis() - start)
        } catch (e: Exception) {
            AIResult(false, errorMessage = "人声分离失败: ${e.message}")
        }
    }

    fun getDeviceInfo() = deviceDetector.detect()
    fun getRegistry() = registry
    fun release() {
        rifeWrapper?.release(); rifeWrapper = null
        esrganWrapper?.release(); esrganWrapper = null
        sam2Wrapper?.release(); sam2Wrapper = null
        whisperWrapper?.release(); whisperWrapper = null
        demucsWrapper?.release(); demucsWrapper = null
        rnnoiseWrapper?.release(); rnnoiseWrapper = null
        sessionManager.releaseAll()
        engine.release()
        isInitialized = false
    }

    companion object {
        private const val TAG = "AIIntegrationBridge"
    }
}
