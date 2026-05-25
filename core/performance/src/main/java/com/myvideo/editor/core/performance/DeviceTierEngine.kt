package com.myvideo.editor.core.performance

import android.app.ActivityManager
import android.content.Context
import android.os.Build

class DeviceTierEngine(context: Context) {
    enum class Tier { Low, Mid, High, Ultra }

    val tier: Tier
    val ramMb: Long
    val cpuCores: Int
    val apiLevel: Int

    init {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)
        ramMb = memInfo.totalMem / (1024 * 1024)
        cpuCores = Runtime.getRuntime().availableProcessors()
        apiLevel = Build.VERSION.SDK_INT
        tier = when {
            ramMb >= 8000 && cpuCores >= 8 -> Tier.Ultra
            ramMb >= 6000 && cpuCores >= 6 -> Tier.High
            ramMb >= 4000 && cpuCores >= 4 -> Tier.Mid
            else -> Tier.Low
        }
    }

    fun getMaxExportResolution(): Pair<Int, Int> = when (tier) {
        Tier.Ultra -> Pair(3840, 2160)
        Tier.High -> Pair(2560, 1440)
        Tier.Mid -> Pair(1920, 1080)
        Tier.Low -> Pair(1280, 720)
    }

    fun getMaxPreviewFps(): Int = when (tier) {
        Tier.Ultra -> 60; Tier.High -> 30; Tier.Mid -> 24; Tier.Low -> 15
    }

    fun getMaxTimelineClips(): Int = when (tier) {
        Tier.Ultra -> 50; Tier.High -> 30; Tier.Mid -> 15; Tier.Low -> 8
    }

    fun getPreviewDownscale(): Float = when (tier) {
        Tier.Ultra -> 1f; Tier.High -> 0.75f; Tier.Mid -> 0.5f; Tier.Low -> 0.25f
    }

    fun supportsAI(): Boolean = tier >= Tier.Mid
    fun supports4K(): Boolean = tier >= Tier.High
}
