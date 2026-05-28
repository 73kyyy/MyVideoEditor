package com.myvideo.editor.core.security.membership

import android.content.Context
import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * 会员Token加密存储
 * token存在本地加密SharedPreferences
 * 使用时解密发送到服务器验证
 */
class TokenManager(private val context: Context) {

    private val prefsName = "nexclip_token"
    private val keyToken = "member_token"
    private val keySalt = "token_salt"
    private val passphrase = "NexClip_Security_2024"

    fun saveToken(token: String) {
        val salt = generateSalt()
        val encrypted = encrypt(token, salt)
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE).edit()
            .putString(keyToken, encrypted)
            .putString(keySalt, Base64.encodeToString(salt, Base64.NO_WRAP))
            .apply()
    }

    fun loadToken(): String? {
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val encrypted = prefs.getString(keyToken, null) ?: return null
        val saltB64 = prefs.getString(keySalt, null) ?: return null
        val salt = Base64.decode(saltB64, Base64.NO_WRAP)
        return try { decrypt(encrypted, salt) } catch (e: Exception) { null }
    }

    fun hasToken(): Boolean = loadToken() != null

    fun clearToken() {
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE).edit().clear().apply()
    }

    private fun generateSalt(): ByteArray {
        val salt = ByteArray(16)
        java.security.SecureRandom().nextBytes(salt)
        return salt
    }

    private fun encrypt(plain: String, salt: ByteArray): String {
        val key = deriveKey(salt)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(ByteArray(16)))
        val encrypted = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    private fun decrypt(cipherText: String, salt: ByteArray): String {
        val key = deriveKey(salt)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(ByteArray(16)))
        val decrypted = cipher.doFinal(Base64.decode(cipherText, Base64.NO_WRAP))
        return String(decrypted, Charsets.UTF_8)
    }

    private fun deriveKey(salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(passphrase.toCharArray(), salt, 10000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }
}
