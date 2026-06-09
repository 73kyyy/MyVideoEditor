package com.myvideo.editor.core.ai.common

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import java.io.File

/**
 * 安全模型加载器 - C++ JNI桥接
 *
 * 安全层级:
 * 1. C++层模型解密 (AES-256-GCM + PBKDF2)
 * 2. C++层会员令牌验证 (HMAC-SHA256)
 * 3. C++层防篡改检测 (签名校验 + 防调试)
 * 4. Kotlin层业务逻辑
 */
class SecureModelLoader(private val context: Context) {

    private var nativeHandle: Long = 0
    var isInitialized = false; private set

    companion object {
        private const val TAG = "SecureModelLoader"

        init {
            try {
                System.loadLibrary("secure_inference")
                Log.d(TAG, "secure_inference library loaded")
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Failed to load secure_inference library", e)
            }
        }
    }

    // JNI native methods
    private external fun nativeInit(context: Context): Long
    private external fun nativeDecryptModel(
        handle: Long, assetManager: AssetManager,
        assetPath: String, membershipToken: String
    ): ByteArray?
    private external fun nativeVerifyMembership(handle: Long, membershipToken: String): Boolean
    private external fun nativeCheckIntegrity(handle: Long, context: Context): Boolean
    external fun nativeGenerateToken(expiryTimestamp: Long): String?
    private external fun nativeRelease(handle: Long)

    /**
     * 初始化安全引擎
     * 派生加密密钥，准备解密环境
     */
    fun init(): Boolean {
        if (isInitialized) return true
        return try {
            nativeHandle = nativeInit(context)
            isInitialized = nativeHandle != 0L
            if (isInitialized) {
                Log.d(TAG, "Secure engine initialized")
            } else {
                Log.e(TAG, "Failed to initialize secure engine")
            }
            isInitialized
        } catch (e: Exception) {
            Log.e(TAG, "Exception during init", e)
            false
        }
    }

    /**
     * 解密模型文件
     * 必须先通过会员验证才能解密
     *
     * @param assetPath assets中的路径，如 "ai_models/rife_v4.onnx"
     * @param membershipToken 会员令牌（由generateMembershipToken生成）
     * @return 解密后的模型字节数组，失败返回null
     */
    fun decryptModel(assetPath: String, membershipToken: String): ByteArray? {
        if (!isInitialized || nativeHandle == 0L) {
            Log.e(TAG, "Engine not initialized")
            return null
        }
        return try {
            nativeDecryptModel(nativeHandle, context.assets, assetPath, membershipToken)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decrypt model: $assetPath", e)
            null
        }
    }

    /**
     * 验证会员令牌
     * 独立于Java层的会员校验，防止Java层被绕过
     */
    fun verifyMembership(membershipToken: String): Boolean {
        if (!isInitialized || nativeHandle == 0L) return false
        return try {
            nativeVerifyMembership(nativeHandle, membershipToken)
        } catch (e: Exception) {
            Log.e(TAG, "Membership verification error", e)
            false
        }
    }

    /**
     * 检查应用完整性
     * 包括: APK签名验证、防调试检测
     */
    fun checkIntegrity(): Boolean {
        if (!isInitialized || nativeHandle == 0L) return false
        return try {
            nativeCheckIntegrity(nativeHandle, context)
        } catch (e: Exception) {
            Log.e(TAG, "Integrity check error", e)
            false
        }
    }

    /**
     * 生成会员令牌
     * 令牌格式: MEMBER:expiry_timestamp:hmac_hex
     * 令牌由C++层签名，无法伪造
     *
     * @param expiryTimestamp 过期时间戳（秒），0表示永不过期
     * @return 会员令牌字符串
     */
    fun generateMembershipToken(expiryTimestamp: Long = 0): String? {
        return try {
            nativeGenerateToken(expiryTimestamp)
        } catch (e: Exception) {
            Log.e(TAG, "Token generation error", e)
            null
        }
    }

    /**
     * 解密模型并保存到临时文件
     * 某些API需要文件路径而非字节数组时使用
     *
     * @return 临时文件路径，失败返回null
     */
    fun decryptModelToTempFile(assetPath: String, membershipToken: String): String? {
        val bytes = decryptModel(assetPath, membershipToken) ?: return null
        return try {
            val tempFile = File(context.cacheDir, "dec_${System.currentTimeMillis()}_${File(assetPath).name}")
            tempFile.writeBytes(bytes)
            tempFile.deleteOnExit()
            tempFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write temp file", e)
            null
        }
    }

    /**
     * 释放安全引擎
     * 安全擦除内存中的密钥
     */
    fun release() {
        if (nativeHandle != 0L) {
            try {
                nativeRelease(nativeHandle)
            } catch (e: Exception) {
                Log.e(TAG, "Release error", e)
            }
            nativeHandle = 0L
        }
        isInitialized = false
    }
}
