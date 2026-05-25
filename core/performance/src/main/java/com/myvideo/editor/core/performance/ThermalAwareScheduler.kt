package com.myvideo.editor.core.performance

import android.content.Context
import android.os.Build
import android.os.PowerManager

class ThermalAwareScheduler(context: Context) {
    enum class ThermalState { Normal, Warm, Hot, Critical }
    var onThermalChange: ((ThermalState) -> Unit)? = null
    private val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    fun getState(): ThermalState {
        if (Build.VERSION.SDK_INT >= 29) {
            return when (pm.currentThermalStatus) {
                PowerManager.THERMAL_STATUS_NONE, PowerManager.THERMAL_STATUS_LIGHT -> ThermalState.Normal
                PowerManager.THERMAL_STATUS_MODERATE -> ThermalState.Warm
                PowerManager.THERMAL_STATUS_SEVERE -> ThermalState.Hot
                else -> ThermalState.Critical
            }
        }
        return ThermalState.Normal
    }

    fun getMaxFps(): Int = when (getState()) {
        ThermalState.Normal -> 60; ThermalState.Warm -> 30
        ThermalState.Hot -> 24; ThermalState.Critical -> 15
    }

    fun getMaxBitrate(): Int = when (getState()) {
        ThermalState.Normal -> 50_000_000; ThermalState.Warm -> 20_000_000
        ThermalState.Hot -> 10_000_000; ThermalState.Critical -> 5_000_000
    }

    fun shouldThrottle(): Boolean = getState() >= ThermalState.Hot
    fun isCritical(): Boolean = getState() == ThermalState.Critical
}
