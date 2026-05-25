package com.myvideo.editor.core.security.membership

import android.util.Base64

object TokenManager {
    private var cachedToken: String? = null

    fun encode(membership: MembershipValidator.Membership): String {
        val data = "${membership.tier.name}:${membership.expiryMs}"
        return Base64.encodeToString(data.toByteArray(), Base64.NO_WRAP)
    }

    fun decode(token: String): MembershipValidator.Membership {
        val data = String(Base64.decode(token, Base64.NO_WRAP))
        val parts = data.split(":")
        val tier = MembershipValidator.Tier.valueOf(parts[0])
        val expiry = parts[1].toLong()
        return MembershipValidator.Membership(tier, expiry)
    }

    fun save(token: String) { cachedToken = token }
    fun load(): String? = cachedToken
    fun clear() { cachedToken = null }
    fun isValid(token: String): Boolean = try { decode(token).isActive } catch (e: Exception) { false }
}
