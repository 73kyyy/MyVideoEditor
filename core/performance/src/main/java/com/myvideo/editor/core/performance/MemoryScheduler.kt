package com.myvideo.editor.core.performance

import android.app.ActivityManager
import android.content.Context

class MemoryScheduler(private val context: Context) {
    enum class MemoryPressure { Normal, Moderate, Critical }
    var onMemoryPressure: ((MemoryPressure) -> Unit)? = null

    private val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    fun check(): MemoryPressure {
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        val availMb = info.availMem / (1024 * 1024)
        return when {
            availMb < 200 -> MemoryPressure.Critical
            availMb < 500 -> MemoryPressure.Moderate
            else -> MemoryPressure.Normal
        }
    }

    fun getAvailableMB(): Long {
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        return info.availMem / (1024 * 1024)
    }

    fun isLowMemory(): Boolean {
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        return info.lowMemory
    }

    fun requestTrim(level: Int) {
        when (check()) {
            MemoryPressure.Critical -> { onMemoryPressure?.invoke(MemoryPressure.Critical); System.gc() }
            MemoryPressure.Moderate -> onMemoryPressure?.invoke(MemoryPressure.Moderate)
            else -> {}
        }
    }
}
