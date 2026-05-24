package com.myvideo.editor.startup

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * NexClip 启动屏ViewModel
 * 管理启动流程状态
 */
class SplashViewModel {
    var isLoading by mutableStateOf(true)
    var deviceTier by mutableStateOf("")
    var loadMessage by mutableStateOf("正在初始化...")
    var isReady by mutableStateOf(false)

    fun startLoading(context: Context) {
        Thread {
            loadMessage = "检测设备性能..."
            val tier = DeviceTierDetector.detect(context)
            deviceTier = tier.label
            Thread.sleep(500)

            loadMessage = "加载资源..."
            Thread.sleep(300)

            loadMessage = "准备就绪"
            isLoading = false
            isReady = true
        }.start()
    }
}
