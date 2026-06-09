package com.myvideo.editor.core.security.membership

import android.util.Log
import java.net.HttpURLConnection
import java.net.URL

/**
 * 会员验证器
 * 强制联网验证会员token，无测试模式后门
 * 双重验证：Java层 + C++层（SecureModelLoader）
 */
class MembershipValidator {

    enum class Tier { Free, Member }

    data class Membership(
        val tier: Tier,
        val expiryMs: Long,
        val isActive: Boolean = tier == Tier.Member && System.currentTimeMillis() < expiryMs
    )

    private var current: Membership = Membership(Tier.Free, 0)
    private var lastVerificationMs: Long = 0
    private val verificationCacheMs = 5 * 60 * 1000L // 5分钟缓存

    /**
     * 联网验证会员token
     */
    fun verifyOnline(token: String): Boolean {
        return try {
            val url = URL("https://api.nexclip.app/v1/verify")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.connect()

            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().readText()
                val expiry = parseExpiry(response)
                current = Membership(Tier.Member, expiry)
                lastVerificationMs = System.currentTimeMillis()
                Log.d("Membership", "验证通过，到期: $expiry")
                true
            } else {
                current = Membership(Tier.Free, 0)
                Log.d("Membership", "验证失败: ${conn.responseCode}")
                false
            }
        } catch (e: Exception) {
            Log.e("Membership", "验证异常: ${e.message}")
            if (isCacheValid() && current.tier == Tier.Member) {
                Log.d("Membership", "使用缓存验证")
                true
            } else {
                current = Membership(Tier.Free, 0)
                false
            }
        }
    }

    /**
     * 检查是否需要重新验证
     */
    fun needsVerification(): Boolean {
        if (current.tier == Tier.Free) return true
        if (System.currentTimeMillis() - lastVerificationMs > verificationCacheMs) return true
        if (System.currentTimeMillis() > current.expiryMs) return true
        return false
    }

    fun isActive(): Boolean = current.isActive
    fun isFree(): Boolean = current.tier == Tier.Free || !current.isActive
    fun isMember(): Boolean = current.tier == Tier.Member && current.isActive
    fun getCurrent(): Membership = current
    fun getExpiry(): Long = current.expiryMs

    fun daysRemaining(): Long {
        val diff = current.expiryMs - System.currentTimeMillis()
        return if (diff > 0) diff / (1000 * 60 * 60 * 24) else 0
    }

    private fun isCacheValid(): Boolean {
        if (lastVerificationMs == 0L) return false
        return System.currentTimeMillis() - lastVerificationMs < verificationCacheMs
    }

    private fun parseExpiry(json: String): Long {
        return try {
            val expiryStr = json.substringAfter("\"expiry\":").substringBefore(",")
            expiryStr.trim().toLong()
        } catch (e: Exception) { 0L }
    }

    fun clearCache() {
        current = Membership(Tier.Free, 0)
        lastVerificationMs = 0
    }
}
