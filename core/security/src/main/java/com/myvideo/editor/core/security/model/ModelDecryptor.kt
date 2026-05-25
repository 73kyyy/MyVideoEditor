package com.myvideo.editor.core.security.model

class ModelDecryptor {
    external fun nativeInit(): Boolean
    external fun nativeDecrypt(encrypted: ByteArray): ByteArray
    external fun nativeRelease()

    private var initialized = false

    fun init(): Boolean {
        return try { initialized = nativeInit(); initialized }
        catch (e: Exception) { false }
    }

    fun decrypt(encryptedData: ByteArray): ByteArray? {
        if (!initialized) return null
        return try { nativeDecrypt(encryptedData) }
        catch (e: Exception) { null }
    }

    fun decryptFile(inputPath: String, outputPath: String): Boolean {
        return try {
            val encrypted = java.io.File(inputPath).readBytes()
            val decrypted = decrypt(encrypted) ?: return false
            java.io.File(outputPath).writeBytes(decrypted)
            true
        } catch (e: Exception) { false }
    }

    fun release() { if (initialized) { nativeRelease(); initialized = false } }
}
