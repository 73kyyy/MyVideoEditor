package com.myvideo.editor.startup

import android.app.ActivityManager
import android.content.Context
import android.os.Build

/**
 * NexClip 设备性能档位检测
 * 根据设备配置自动调整性能参数
 */
object DeviceTierDetector {

    enum class Tier(val label: String, val maxResolution: Int, val maxFps: Int) {
        LOW("T3 入门", 720, 24),
        MID("T2 中端", 1080, 30),
        HIGH("T1 旗舰", 2160, 60)
    }

    private var cachedTier: Tier? = null

    fun detect(context: Context): Tier {
        cachedTier?.let { return it }

        val tier = try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            am.getMemoryInfo(memInfo)
            val totalRamGb = memInfo.totalMem / (1024 * 1024 * 1024)
            val cores = Runtime.getRuntime().availableProcessors()

            when {
                totalRamGb >= 8 && cores >= 6 -> Tier.HIGH
                totalRamGb >= 4 && cores >= 4 -> Tier.MID
                else -> Tier.LOW
            }
        } catch (e: Exception) { Tier.MID }

        cachedTier = tier
        return tier
    }

    fun getMaxResolution(context: Context): Int = detect(context).maxResolution
    fun getMaxFps(context: Context): Int = detect(context).maxFps
}
