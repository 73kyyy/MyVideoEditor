package com.myvideo.editor.ui.editor

import android.app.Activity
import android.content.Context
import android.view.View
import com.myvideo.editor.core.security.membership.MembershipValidator
import com.myvideo.editor.core.security.quota.QuotaManager
import com.myvideo.editor.security.UIProtector
import com.myvideo.editor.ui.security.ScreenRecordDetector

/**
 * AI功能策略：
 * 免费用户：每天3次AI，同一功能重复算1次，不能导出含AI的项目
 * 付费会员：无限次，全部可用
 * 所有AI必须联网
 * 免费用户录屏时：弹出提示+虚化项目页面，录屏关闭后恢复
 */
class AIFeatureUIHelper(private val context: Context) {

    private val validator = MembershipValidator()
    private val quotaManager = QuotaManager(context)
    private val aiUsedInProject = mutableSetOf<String>()

    // 录屏状态回调
    private var onScreenRecordChanged: ((Boolean) -> Unit)? = null
    private var lastRecordState = false

    fun markAIUsed(featureId: String) { aiUsedInProject.add(featureId) }
    fun clearAIUsage() { aiUsedInProject.clear() }
    fun hasAIInProject(): Boolean = aiUsedInProject.isNotEmpty()

    /**
     * 检查AI功能是否可用
     */
    fun checkAIAccess(featureId: String, isOnline: Boolean): String? {
        if (!isOnline) return "请连接网络使用"
        if (isCurrentlyScreenRecording() && !validator.isMember() && hasAIInProject()) {
            return "录屏中无法使用AI功能"
        }
        val quota = quotaManager.checkQuota(featureId, validator.isMember())
        if (!quota.allowed) return quota.message
        return null
    }

    fun recordAIUsage(featureId: String) {
        quotaManager.recordUsage(featureId)
        markAIUsed(featureId)
    }

    fun getQuotaText(isOnline: Boolean): String {
        if (validator.isMember()) return "会员 · 无限次"
        if (!isOnline) return "请连接网络使用"
        val used = quotaManager.getTodayUsed()
        val limit = quotaManager.getDailyFreeLimit()
        val features = quotaManager.getTodayFeatures()
        return "今日${used}/${limit}次 · 已用: ${features.joinToString(",") { featureLabel(it) }}"
    }

    private fun featureLabel(id: String): String = when (id) {
        "segment" -> "抠图"; "superres" -> "超分"; "interpolate" -> "插帧"
        "whisper" -> "语音"; "denoise" -> "降噪"; "separate" -> "分离"
        else -> id
    }

    fun canExport(): Boolean = validator.isMember()

    fun checkExportPermission(): String? {
        if (!hasAIInProject()) return null
        if (validator.isMember()) return null
        return "您的项目使用了AI功能，开通会员即可导出"
    }

    fun getExportBlockMessage(): String {
        val count = aiUsedInProject.size
        return "当前项目使用了${count}个AI功能\n开通会员即可导出无水印视频\n¥29/月 · ¥76/季 · ¥228/年"
    }

    fun shouldAddWatermark(): Boolean = !validator.isMember() && hasAIInProject()

    // ===== 录屏检测（使用原版UIProtector + ScreenRecordDetector）=====

    /**
     * 启动录屏检测
     * 检测到录屏 → 回调通知UI弹出提示+虚化项目页面
     * 录屏关闭 → 回调通知UI恢复正常
     */
    fun startScreenRecordDetection(onChanged: (isRecording: Boolean) -> Unit) {
        onScreenRecordChanged = onChanged
    }

    /**
     * 检查录屏状态（UI层每秒调用）
     * 免费用户 + 有AI项目 + 录屏中 → 弹提示+虚化
     */
    fun checkScreenState(context: Context): Boolean {
        val isRecording = ScreenRecordDetector.isScreenRecording(context)
        if (isRecording != lastRecordState) {
            lastRecordState = isRecording
            if (!validator.isMember() && hasAIInProject()) {
                onScreenRecordChanged?.invoke(isRecording)
            }
            if (isRecording && !validator.isMember()) {
                // 使用UIProtector的FLAG_SECURE保护
                if (context is Activity) {
                    UIProtector.enableScreenProtection(context)
                }
            } else if (!isRecording) {
                // 录屏结束，恢复
                if (context is Activity) {
                    try {
                        context.window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
                    } catch (_: Exception) {}
                }
            }
        }
        return isRecording
    }

    /**
     * 当前是否录屏中
     */
    fun isCurrentlyScreenRecording(): Boolean = lastRecordState

    /**
     * 是否需要虚化项目页面
     */
    fun shouldBlurBackground(): Boolean {
        return lastRecordState && !validator.isMember() && hasAIInProject()
    }

    /**
     * 获取录屏提示文本
     */
    fun getScreenRecordWarning(): String {
        return "检测到录屏，AI功能相关内容已隐藏\n开通会员解锁完整功能"
    }

    fun getQuotaManager() = quotaManager

    fun release() {
        onScreenRecordChanged = null
        lastRecordState = false
    }
}
