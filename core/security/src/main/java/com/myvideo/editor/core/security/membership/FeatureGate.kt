package com.myvideo.editor.core.security.membership

import com.myvideo.editor.core.common.constants.FeatureFlags

/**
 * AI功能门控
 * 测试模式(TEST_MODE=true)：所有功能直接可用
 * 正式模式(TEST_MODE=false)：免费用户锁定，付费会员需联网验证
 */
class FeatureGate(private val validator: MembershipValidator) {

    enum class AIFeature(val label: String) {
        SEGMENT("智能抠图"),
        SUPER_RES("超分辨率"),
        INTERPOLATE("视频插帧"),
        WHISPER("语音转文字"),
        DENOISE("AI降噪"),
        SEPARATE("人声分离")
    }

    data class GateResult(
        val allowed: Boolean,
        val reason: String = ""
    )

    /**
     * 检查AI功能是否可用
     */
    fun check(feature: AIFeature, isOnline: Boolean): GateResult {
        if (FeatureFlags.TEST_MODE) {
            return GateResult(true)
        }

        if (validator.isFree()) {
            return GateResult(false, "开通会员解锁${feature.label}")
        }
        if (!isOnline) {
            return GateResult(false, "请连接网络使用")
        }
        if (validator.needsVerification()) {
            return GateResult(false, "会员验证中，请稍候")
        }
        if (!validator.isMember()) {
            return GateResult(false, "会员已过期，请续费")
        }
        return GateResult(true)
    }

    /**
     * 检查是否是会员（不检查网络）
     */
    fun isFeatureUnlocked(): Boolean = if (FeatureFlags.TEST_MODE) true else validator.isMember()

    /**
     * 获取解锁提示
     */
    fun getUnlockMessage(feature: AIFeature): String {
        if (FeatureFlags.TEST_MODE) return ""
        return if (validator.isFree()) {
            "开通会员解锁${feature.label}"
        } else if (!validator.isMember()) {
            "会员已过期，请续费"
        } else {
            "请连接网络使用"
        }
    }

    /**
     * 获取所有功能状态
     */
    fun getAllFeatureStatus(isOnline: Boolean): Map<AIFeature, GateResult> {
        return AIFeature.values().associateWith { check(it, isOnline) }
    }
}
