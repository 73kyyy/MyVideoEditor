package com.myvideo.editor.core.ai

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.Log

class DeviceTierDetector(private val context: Context) {

    enum class Tier(val label: String, val maxThreads: Int, val chunkSize: Int, val gcInterval: Int) {
        T1("入门", 2, 256, 4),
        T2("标准", 4, 512, 0),
        T3("旗舰", 8, 1024, 0)
    }

    data class DeviceInfo(
        val tier: Tier,
        val ramMb: Long,
        val cpuCores: Int,
        val cpuMaxFreqMhz: Int,
        val gpuRenderer: String,
        val sdkInt: Int,
        val socModel: String,
        val hasNpu: Boolean,
        val score: Int
    )

    fun detect(): DeviceInfo {
        val ramMb = getRamMb()
        val cpuCores = Runtime.getRuntime().availableProcessors()
        val cpuFreq = getCpuMaxFreq()
        val gpuRenderer = getGpuRenderer()
        val sdkInt = Build.VERSION.SDK_INT
        val socModel = Build.SOC_MODEL ?: Build.HARDWARE ?: "Unknown"
        val hasNpu = detectNpu(socModel)
        val score = calculateScore(ramMb, cpuCores, cpuFreq, gpuRenderer, sdkInt, hasNpu)
        val tier = when {
            score >= 75 -> Tier.T3
            score >= 50 -> Tier.T2
            else -> Tier.T1
        }
        Log.d("DeviceTier", "RAM:${ramMb}MB CPU:${cpuCores}核 ${cpuFreq}MHz GPU:$gpuRenderer SoC:$socModel NPU:$hasNpu → ${tier.label}(${score}分)")
        return DeviceInfo(tier, ramMb, cpuCores, cpuFreq, gpuRenderer, sdkInt, socModel, hasNpu, score)
    }

    private fun calculateScore(ram: Long, cores: Int, freq: Int, gpu: String, sdk: Int, npu: Boolean): Int {
        var score = 0
        score += when {
            ram >= 12000 -> 30; ram >= 8000 -> 25; ram >= 6000 -> 20
            ram >= 4000 -> 15; else -> 5
        }
        score += when {
            cores >= 8 && freq >= 2800 -> 30; cores >= 8 && freq >= 2000 -> 25
            cores >= 6 && freq >= 2000 -> 20; cores >= 4 -> 15; else -> 5
        }
        score += when {
            gpu.contains("Adreno 7", true) || gpu.contains("Mali-G7", true) -> 20
            gpu.contains("Adreno 6", true) || gpu.contains("Mali-G6", true) -> 15
            gpu.contains("Adreno 5", true) || gpu.contains("Mali-G5", true) -> 10
            else -> 5
        }
        if (npu) score += 10
        score += when { sdk >= 33 -> 10; sdk >= 30 -> 7; sdk >= 28 -> 5; else -> 3 }
        return score
    }

    private fun getRamMb(): Long {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)
        return memInfo.totalMem / (1024 * 1024)
    }

    private fun getCpuMaxFreq(): Int {
        var maxFreq = 0
        for (i in 0 until Runtime.getRuntime().availableProcessors()) {
            try {
                val f = java.io.File("/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_max_freq").readText().trim().toIntOrNull() ?: 0
                if (f > maxFreq) maxFreq = f
            } catch (_: Exception) {}
        }
        return maxFreq / 1000
    }

    private fun getGpuRenderer(): String {
        return try { android.opengl.GLES20.glGetString(android.opengl.GLES20.GL_RENDERER) ?: "Unknown" }
        catch (_: Exception) { "Unknown" }
    }

    private fun detectNpu(soc: String): Boolean {
        val s = soc.lowercase()
        if (s.contains("snapdragon 8") || s.contains("sm8") || s.contains("sm7")) return true
        if (s.contains("dimensity 9") || s.contains("dimensity 8") || s.contains("mt69")) return true
        if (s.contains("exynos 2") || s.contains("exynos 99")) return true
        if (s.contains("tensor")) return true
        if (s.contains("kirin 9") || s.contains("kirin 8")) return true
        return false
    }

    companion object {
        @Volatile private var instance: DeviceTierDetector? = null
        fun getInstance(context: Context): DeviceTierDetector =
            instance ?: synchronized(this) { instance ?: DeviceTierDetector(context).also { instance = it } }
    }
}
