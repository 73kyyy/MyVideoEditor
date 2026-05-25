package com.myvideo.editor.core.security.membership

class FeatureGate(private val validator: MembershipValidator) {
    enum class Feature(val requiredTier: MembershipValidator.Tier) {
        BasicExport(MembershipValidator.Tier.Free),
        HDExport(MembershipValidator.Tier.Pro),
        FourKExport(MembershipValidator.Tier.Pro),
        AIEnhance(MembershipValidator.Tier.Pro),
        AISegmentation(MembershipValidator.Tier.Premium),
        NoWatermark(MembershipValidator.Tier.Pro),
        PremiumFilters(MembershipValidator.Tier.Pro),
        BatchExport(MembershipValidator.Tier.Premium),
        CloudSync(MembershipValidator.Tier.Premium),
        PriorityRender(MembershipValidator.Tier.Premium)
    }

    fun isAllowed(feature: Feature): Boolean {
        return when (feature.requiredTier) {
            MembershipValidator.Tier.Free -> true
            MembershipValidator.Tier.Pro -> validator.isPro()
            MembershipValidator.Tier.Premium -> validator.isPremium()
        }
    }

    fun checkOrThrow(feature: Feature) {
        if (!isAllowed(feature)) throw SecurityException("需要${feature.requiredTier}会员")
    }

    fun getUpgradeMessage(feature: Feature): String = "升级到${feature.requiredTier.name}解锁${feature.name}"
}
