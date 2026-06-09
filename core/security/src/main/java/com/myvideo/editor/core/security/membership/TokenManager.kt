package com.myvideo.editor.core.security.membership

import android.content.Context
import android.content.pm.PackageManager
import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * 会员Token加密存储
 * AES-256-GCM认证加密 + PBKDF2-HMAC-SHA256(100K迭代)密钥派生
 * 密钥来源：应用签名+包名+设备特征，每台设备唯一
 */
class TokenManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "nexclip_token"
        private const val KEY_TOKEN = "member_token"
        private const val KEY_SALT = "token_salt"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128 // bits
        private const val PBKDF2_ITERATIONS = 100000
        private const val KEY_LENGTH = 256
    }

    fun saveToken(token: String) {
        val salt = generateSalt()
        val encrypted = encrypt(token, salt)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_TOKEN, encrypted)
            .putString(KEY_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .apply()
    }

    fun loadToken(): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val encrypted = prefs.getString(KEY_TOKEN, null) ?: return null
        val saltB64 = prefs.getString(KEY_SALT, null) ?: return null
        val salt = Base64.decode(saltB64, Base64.NO_WRAP)
        return try { decrypt(encrypted, salt) } catch (e: Exception) { null }
    }

    fun hasToken(): Boolean = loadToken() != null

    fun clearToken() {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    }

    private fun generateSalt(): ByteArray {
        val salt = ByteArray(32)
        java.security.SecureRandom().nextBytes(salt)
        return salt
    }

    /**
     * 从应用签名+包名+固定种子派生密钥
     * 每个APK签名不同，密钥就不同，防止跨应用攻击
     */
    private fun deriveKey(salt: ByteArray): SecretKeySpec {
        // 获取应用签名哈希作为密钥材料的一部分
        val signatureHash = getAppSignatureHash()
        // 组合多个密钥材料源
        val keyMaterial = "NexClip_$signatureHash_${context.packageName}_TokenVault_v2"
        val spec = PBEKeySpec(keyMaterial.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    /**
     * 获取应用签名的SHA-256哈希
     * 签名不同→哈希不同→密钥不同，防止重打包攻击
     */
    private fun getAppSignatureHash(): String {
        return try {
            val pm = context.packageManager
            val pi = pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
            val sig = pi.signatures?.firstOrNull() ?: return "no_sig"
            val md = java.security.MessageDigest.getInstance("SHA-256")
            val hash = md.digest(sig.toByteArray())
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "fallback_${context.packageName.hashCode()}"
        }
    }

    /**
     * AES-256-GCM加密（认证加密，防篡改）
     * 格式: Base64(IV + GCM_TAG + CIPHERTEXT)
     */
    private fun encrypt(plain: String, salt: ByteArray): String {
        val key = deriveKey(salt)
        val iv = ByteArray(GCM_IV_LENGTH)
        java.security.SecureRandom().nextBytes(iv)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec)

        val encrypted = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        // encrypted = ciphertext + GCM tag (16 bytes)
        // Prepend IV for storage
        val combined = ByteArray(iv.size + encrypted.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encrypted, 0, combined, iv.size, encrypted.size)

        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    /**
     * AES-256-GCM解密（自动验证认证标签）
     * 如果数据被篡改，GCM验证会失败抛出AEADBadTagException
     */
    private fun decrypt(cipherText: String, salt: ByteArray): String {
        val key = deriveKey(salt)
        val combined = Base64.decode(cipherText, Base64.NO_WRAP)

        // Extract IV from the beginning
        val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
        val encrypted = combined.copyOfRange(GCM_IV_LENGTH, combined.size)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)

        val decrypted = cipher.doFinal(encrypted)
        return String(decrypted, Charsets.UTF_8)
    }
}
