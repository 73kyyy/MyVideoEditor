package com.myvideo.editor.core.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PointF
import android.util.Log
import com.myvideo.editor.core.ai.common.InferenceSessionManager
import com.myvideo.editor.core.ai.common.SecureModelLoader
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
 * 安全链路: 联网验证 → Java会员检查 → C++会员验证 → C++模型解密 → 内存加载推理
 *
 * 安全架构:
 * - 模型加密存储在assets（AES-256-GCM）
 * - C++层解密+会员验证（防止Java层被绕过）
 * - 模型从内存加载（不落盘，防止文件提取）
 * - 防篡改检测（签名校验+防调试）
 */
class AIIntegrationBridge(private val context: Context) {

    private val deviceDetector = DeviceTierDetector.getInstance(context)
    private val registry = ModelRegistry(context)
    private val engine = AIEngine(context)
    private val validator = MembershipValidator()
    private val featureGate = FeatureGate(validator)

    // ONNX Runtime session manager (shared across all models)
    private val sessionManager = InferenceSessionManager()

    // C++安全模型加载器（解密+会员验证+防篡改）
    private val secureLoader = SecureModelLoader(context)

    // 会员令牌（C++签名，无法伪造）
    private var membershipToken: String? = null

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

        // 1. 初始化ONNX Runtime
        if (!sessionManager.init()) {
            Log.e(TAG, "Failed to initialize ONNX Runtime")
            return false
        }

        // 2. 初始化C++安全引擎（派生密钥）
        if (!secureLoader.init()) {
            Log.e(TAG, "Failed to initialize secure model loader")
            return false
        }

        // 3. 检查应用完整性
        if (!secureLoader.checkIntegrity()) {
            Log.w(TAG, "App integrity check failed - possible tampering detected")
            // 开发环境不阻止，生产环境可改为 return false
        }

        isInitialized = true
        return true
    }

    /**
     * 设置会员令牌
     * 令牌由C++层签名，Java层无法伪造
     * 必须在调用AI功能前设置
     */
    fun setMembershipToken(token: String) {
        this.membershipToken = token
    }

    /**
     * 生成会员令牌
     * 供会员验证成功后调用，生成C++签名的令牌
     */
    fun generateMembershipToken(expiryTimestamp: Long = 0): String? {
        return secureLoader.generateMembershipToken(expiryTimestamp)
    }

    /**
     * 安全解密模型并加载到ORT
     * 流程: C++验证会员 → C++解密模型 → 内存加载到ORT
     * 模型不落盘，防止文件被提取
     */
    private fun secureLoadModel(assetPath: String, modelId: String): Boolean {
        val token = membershipToken
        if (token.isNullOrEmpty()) {
            Log.e(TAG, "No membership token set - cannot decrypt model")
            return false
        }

        // C++层: 验证会员令牌 + 解密模型
        val decryptedBytes = secureLoader.decryptModel(assetPath, token)
        if (decryptedBytes == null) {
            Log.e(TAG, "Failed to decrypt model: $assetPath")
            return false
        }

        // 从内存加载到ORT（不落盘）
        val loaded = sessionManager.loadModelFromBytes(modelId, decryptedBytes)
        if (!loaded) {
            Log.e(TAG, "Failed to load decrypted model into ORT: $assetPath")
        }
        return loaded
    }

    /**
     * 检查是否可用（双重验证：Java层 + C++层）
     */
    fun checkAvailability(feature: AIFeature, isOnline: Boolean): AIResult {
        if (!isOnline) {
            return AIResult(false, errorMessage = "请连接网络使用", requiresNetwork = true)
        }

        // Java层会员检查
        if (validator.isFree()) {
            return AIResult(false, errorMessage = "开通会员解锁${feature.displayName}", requiresMembership = true)
        }

        // C++层会员令牌验证
        val token = membershipToken
        if (token.isNullOrEmpty() || !secureLoader.verifyMembership(token)) {
            return AIResult(false, errorMessage = "会员验证失败，请重新登录", requiresMembership = true)
        }

        return AIResult(true)
    }

    /**
     * 调用AI抠图 (MobileSAM)
     */
    fun segment(input: Bitmap, isOnline: Boolean, point: PointF = PointF(input.width / 2f, input.height / 2f)): AIResult {
        val check = checkAvailability(AIFeature.SEGMENT, isOnline)
        if (!check.success) return check
        if (!initialize()) return AIResult(false, errorMessage = "初始化失败")

        val start = System.currentTimeMillis()
        return try {
            val wrapper = sam2Wrapper ?: run {
                val paths = registry.getAllAssetPaths(AIFeature.SEGMENT.modelId)
                val encPath = paths.getOrElse(0) { "ai_models/sam_encoder.onnx" }
                val decPath = paths.getOrElse(1) { "ai_models/sam_decoder.onnx" }
                if (!secureLoadModel(encPath, "sam_encoder") || !secureLoadModel(decPath, "sam_decoder")) {
                    return AIResult(false, errorMessage = "SAM2模型解密/加载失败")
                }
                val w = SAM2Wrapper(sessionManager)
                if (!w.initFromSession("sam_encoder", "sam_decoder")) {
                    return AIResult(false, errorMessage = "SAM2模型初始化失败")
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
        if (!initialize()) return AIResult(false, errorMessage = "初始化失败")

        val start = System.currentTimeMillis()
        return try {
            val wrapper = esrganWrapper ?: run {
                val path = registry.getAllAssetPaths(AIFeature.SUPER_RES.modelId).firstOrNull()
                    ?: "ai_models/realesrgan_x4plus.onnx"
                if (!secureLoadModel(path, "esrgan")) {
                    return AIResult(false, errorMessage = "ESRGAN模型解密/加载失败")
                }
                val w = ESRGANWrapper(sessionManager)
                if (!w.initFromSession("esrgan")) {
                    return AIResult(false, errorMessage = "ESRGAN模型初始化失败")
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
        if (!initialize()) return AIResult(false, errorMessage = "初始化失败")

        val start = System.currentTimeMillis()
        return try {
            val wrapper = rifeWrapper ?: run {
                val path = registry.getAllAssetPaths(AIFeature.INTERPOLATE.modelId).firstOrNull()
                    ?: "ai_models/rife_v4.onnx"
                if (!secureLoadModel(path, "rife")) {
                    return AIResult(false, errorMessage = "RIFE模型解密/加载失败")
                }
                val w = RIFEWrapper(sessionManager)
                if (!w.initFromSession("rife")) {
                    return AIResult(false, errorMessage = "RIFE模型初始化失败")
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
        if (!initialize()) return AIResult(false, errorMessage = "初始化失败")

        val start = System.currentTimeMillis()
        return try {
            val wrapper = whisperWrapper ?: run {
                val paths = registry.getAllAssetPaths(AIFeature.WHISPER.modelId)
                val encPath = paths.getOrElse(0) { "ai_models/whisper_encoder.onnx" }
                val decPath = paths.getOrElse(1) { "ai_models/whisper_decoder.onnx" }
                if (!secureLoadModel(encPath, "whisper_encoder") || !secureLoadModel(decPath, "whisper_decoder")) {
                    return AIResult(false, errorMessage = "Whisper模型解密/加载失败")
                }
                // tokens.txt不需要解密
                val tokPath = paths.getOrElse(2) { "ai_models/whisper_tokens.txt" }
                val w = WhisperWrapper(sessionManager)
                if (!w.initFromSession("whisper_encoder", "whisper_decoder", tokPath, context)) {
                    return AIResult(false, errorMessage = "Whisper模型初始化失败")
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
        if (!initialize()) return AIResult(false, errorMessage = "初始化失败")

        val start = System.currentTimeMillis()
        return try {
            val wrapper = rnnoiseWrapper ?: run {
                val path = registry.getAllAssetPaths(AIFeature.DENOISE.modelId).firstOrNull()
                    ?: "ai_models/rnnoise.onnx"
                if (!secureLoadModel(path, "rnnoise")) {
                    return AIResult(false, errorMessage = "RNNoise模型解密/加载失败")
                }
                val w = RNNoiseWrapper(sessionManager)
                if (!w.initFromSession("rnnoise")) {
                    return AIResult(false, errorMessage = "RNNoise模型初始化失败")
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
        if (!initialize()) return AIResult(false, errorMessage = "初始化失败")

        val start = System.currentTimeMillis()
        return try {
            val wrapper = demucsWrapper ?: run {
                val path = registry.getAllAssetPaths(AIFeature.SEPARATE.modelId).firstOrNull()
                    ?: "ai_models/demucs_htdemucs.onnx"
                if (!secureLoadModel(path, "demucs")) {
                    return AIResult(false, errorMessage = "Demucs模型解密/加载失败")
                }
                val w = DemucsWrapper(sessionManager)
                if (!w.initFromSession("demucs")) {
                    return AIResult(false, errorMessage = "Demucs模型初始化失败")
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
        secureLoader.release()
        engine.release()
        membershipToken = null
        isInitialized = false
    }

    companion object {
        private const val TAG = "AIIntegrationBridge"
    }
}
