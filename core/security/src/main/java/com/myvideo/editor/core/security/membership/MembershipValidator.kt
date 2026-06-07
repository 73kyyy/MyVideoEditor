package com.myvideo.editor.core.security.membership

import android.util.Log
import com.myvideo.editor.core.common.constants.FeatureFlags
import java.net.HttpURLConnection
import java.net.URL

/**
 * 会员验证器
 * 测试模式(TEST_MODE=true)：跳过服务器验证，所有功能可用
 * 正式模式(TEST_MODE=false)：强制联网验证会员token
 */
class MembershipValidator {

    enum class Tier { Free, Member }

    data class Membership(
        val tier: Tier,
        val expiryMs: Long,
        val isActive: Boolean = tier == Tier.Member && System.currentTimeMillis() < expiryMs
    )

    private var current: Membership = if (FeatureFlags.TEST_MODE) {
        // 测试模式：直接设为会员，有效期1年
        Membership(Tier.Member, System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000)
    } else {
        Membership(Tier.Free, 0)
    }

    private var lastVerificationMs: Long = if (FeatureFlags.TEST_MODE) System.currentTimeMillis() else 0
    private val verificationCacheMs = 5 * 60 * 1000L // 5分钟缓存

    /**
     * 联网验证会员token
     * 测试模式下直接返回true
     */
    fun verifyOnline(token: String): Boolean {
        if (FeatureFlags.TEST_MODE) {
            Log.d("Membership", "[测试模式] 跳过服务器验证，会员自动通过")
            current = Membership(Tier.Member, System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000)
            lastVerificationMs = System.currentTimeMillis()
            return true
        }

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
            if (isCacheValid()) {
                Log.d("Membership", "使用缓存验证")
                current.tier == Tier.Member
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
        if (FeatureFlags.TEST_MODE) return false
        if (current.tier == Tier.Free) return true
        if (System.currentTimeMillis() - lastVerificationMs > verificationCacheMs) return true
        if (System.currentTimeMillis() > current.expiryMs) return true
        return false
    }

    fun isActive(): Boolean = if (FeatureFlags.TEST_MODE) true else current.isActive
    fun isFree(): Boolean = if (FeatureFlags.TEST_MODE) false else (current.tier == Tier.Free || !current.isActive)
    fun isMember(): Boolean = if (FeatureFlags.TEST_MODE) true else (current.tier == Tier.Member && current.isActive)
    fun getCurrent(): Membership = current
    fun getExpiry(): Long = current.expiryMs

    fun daysRemaining(): Long {
        if (FeatureFlags.TEST_MODE) return 365
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
