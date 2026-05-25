package com.myvideo.editor.core.video

import android.app.ActivityManager
import android.content.Context
import android.os.Debug

class MemoryGuardAndroid17(context: Context) {
    private val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val thresholds = mapOf("low" to 100L, "medium" to 200L, "high" to 500L)
    var onMemoryWarning: ((level: String) -> Unit)? = null

    fun checkMemory(): String {
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        val availMb = info.availMem / (1024 * 1024)
        return when {
            availMb < thresholds["low"]!! -> "critical"
            availMb < thresholds["medium"]!! -> "low"
            availMb < thresholds["high"]!! -> "medium"
            else -> "normal"
        }
    }

    fun getAvailableMB(): Long {
        val info = ActivityManager.MemoryInfo(); am.getMemoryInfo(info)
        return info.availMem / (1024 * 1024)
    }

    fun isLowMemory(): Boolean { val info = ActivityManager.MemoryInfo(); am.getMemoryInfo(info); return info.lowMemory }
    fun getNativeHeapUsedMB(): Long = Debug.getNativeHeapAllocatedSize() / (1024 * 1024)
    fun getNativeHeapTotalMB(): Long = Debug.getNativeHeapSize() / (1024 * 1024)
    fun requestGc() { System.gc() }
}
