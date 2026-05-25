package com.myvideo.editor.core.security.membership

class MembershipValidator {
    enum class Tier { Free, Pro, Premium }
    data class Membership(val tier: Tier, val expiryMs: Long, val isActive: Boolean = System.currentTimeMillis() < expiryMs)

    private var current: Membership = Membership(Tier.Free, 0)

    fun validate(token: String): Boolean {
        return try {
            val decoded = TokenManager.decode(token)
            current = decoded
            decoded.isActive
        } catch (e: Exception) { false }
    }

    fun getCurrent(): Membership = current
    fun isPro(): Boolean = current.tier >= Tier.Pro && current.isActive
    fun isPremium(): Boolean = current.tier == Tier.Premium && current.isActive
    fun isFree(): Boolean = current.tier == Tier.Free || !current.isActive
    fun getExpiry(): Long = current.expiryMs
    fun daysRemaining(): Long {
        val diff = current.expiryMs - System.currentTimeMillis()
        return if (diff > 0) diff / (1000 * 60 * 60 * 24) else 0
    }
}
