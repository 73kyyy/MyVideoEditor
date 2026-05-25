package com.myvideo.editor.core.common.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Build

object DeviceCapabilityChecker {
    data class DeviceInfo(
        val ramMb: Long, val cpuCores: Int, val apiLevel: Int,
        val is64Bit: Boolean, val hasVulkan: Boolean
    )

    fun getDeviceInfo(context: Context): DeviceInfo {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)
        return DeviceInfo(
            ramMb = memInfo.totalMem / (1024 * 1024),
            cpuCores = Runtime.getRuntime().availableProcessors(),
            apiLevel = Build.VERSION.SDK_INT,
            is64Bit = Build.SUPPORTED_64_BIT_ABIS.isNotEmpty(),
            hasVulkan = context.packageManager.hasSystemFeature("android.hardware.vulkan.compute")
        )
    }

    fun getDeviceTier(context: Context): String {
        val info = getDeviceInfo(context)
        return when {
            info.ramMb >= 6000 && info.cpuCores >= 8 -> "旗舰"
            info.ramMb >= 4000 && info.cpuCores >= 6 -> "中端"
            else -> "入门"
        }
    }

    fun supports4K(context: Context): Boolean = getDeviceInfo(context).ramMb >= 4000
    fun supportsAI(context: Context): Boolean = getDeviceInfo(context).ramMb >= 3000
    fun supportsHDR(context: Context): Boolean = Build.VERSION.SDK_INT >= 29
}
