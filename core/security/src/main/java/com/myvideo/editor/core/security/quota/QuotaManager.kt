package com.myvideo.editor.core.security.quota

import android.content.Context
import java.util.Calendar

/**
 * 每日AI免费次数管理
 * 免费用户：每天3次AI功能
 * 同一功能重复使用只算1次
 * 付费会员：无限次
 * 每天0点重置
 * 必须联网验证
 */
class QuotaManager(private val context: Context) {

    private val prefsName = "nexclip_quota"
    private val keyDate = "daily_date"
    private val keyUsedFeatures = "used_features"
    private val keyTotalUsed = "total_used"
    private val dailyFreeLimit = 3

    data class QuotaResult(
        val allowed: Boolean,
        val usedCount: Int,
        val limit: Int,
        val remaining: Int,
        val todayFeatures: Set<String>,
        val message: String
    )

    /**
     * 检查是否还能使用某个AI功能
     * 同一功能重复使用不扣次数
     */
    fun checkQuota(featureId: String, isMember: Boolean): QuotaResult {
        if (isMember) {
            return QuotaResult(true, 0, -1, -1, emptySet(), "会员无限次")
        }

        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val savedDate = prefs.getString(keyDate, "") ?: ""
        val today = getTodayString()

        val todayFeatures: MutableSet<String>
        val totalUsed: Int

        if (savedDate == today) {
            todayFeatures = prefs.getStringSet(keyUsedFeatures, emptySet())?.toMutableSet() ?: mutableSetOf()
            totalUsed = prefs.getInt(keyTotalUsed, 0)
        } else {
            todayFeatures = mutableSetOf()
            totalUsed = 0
            prefs.edit()
                .putString(keyDate, today)
                .putStringSet(keyUsedFeatures, emptySet())
                .putInt(keyTotalUsed, 0)
                .apply()
        }

        // 同一功能已经用过，不算次数
        if (todayFeatures.contains(featureId)) {
            return QuotaResult(true, totalUsed, dailyFreeLimit, dailyFreeLimit - totalUsed, todayFeatures, "今日已使用${featureId}，可继续使用")
        }

        val remaining = dailyFreeLimit - totalUsed

        return if (remaining > 0) {
            QuotaResult(true, totalUsed, dailyFreeLimit, remaining, todayFeatures, "今日剩余${remaining}次")
        } else {
            QuotaResult(false, totalUsed, dailyFreeLimit, 0, todayFeatures, "今日免费次数已用完，开通会员无限使用")
        }
    }

    /**
     * 记录一次AI使用（新功能才记录）
     */
    fun recordUsage(featureId: String) {
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val today = getTodayString()
        val savedDate = prefs.getString(keyDate, "") ?: ""

        val features: MutableSet<String>
        val totalUsed: Int

        if (savedDate == today) {
            features = prefs.getStringSet(keyUsedFeatures, emptySet())?.toMutableSet() ?: mutableSetOf()
            totalUsed = prefs.getInt(keyTotalUsed, 0)
        } else {
            features = mutableSetOf()
            totalUsed = 0
        }

        // 已经用过这个功能，不扣次数
        if (features.contains(featureId)) return

        features.add(featureId)

        prefs.edit()
            .putString(keyDate, today)
            .putStringSet(keyUsedFeatures, features)
            .putInt(keyTotalUsed, totalUsed + 1)
            .apply()
    }

    /**
     * 获取今日已用次数（去重后）
     */
    fun getTodayUsed(): Int {
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val savedDate = prefs.getString(keyDate, "") ?: ""
        if (savedDate != getTodayString()) return 0
        return prefs.getInt(keyTotalUsed, 0)
    }

    /**
     * 获取今日使用过的功能列表
     */
    fun getTodayFeatures(): Set<String> {
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val savedDate = prefs.getString(keyDate, "") ?: ""
        if (savedDate != getTodayString()) return emptySet()
        return prefs.getStringSet(keyUsedFeatures, emptySet()) ?: emptySet()
    }

    fun getDailyFreeLimit(): Int = dailyFreeLimit

    private fun getTodayString(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH) + 1}-${cal.get(Calendar.DAY_OF_MONTH)}"
    }
}
