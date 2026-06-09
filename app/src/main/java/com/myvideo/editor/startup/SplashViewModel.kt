package com.myvideo.editor.startup

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.myvideo.editor.core.ai.DeviceTierDetector
import com.myvideo.editor.core.ai.ModelRegistry
import com.myvideo.editor.core.security.membership.MembershipValidator
import com.myvideo.editor.core.security.membership.TokenManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashViewModel(application: Application) : AndroidViewModel(application) {

    enum class SplashState {
        CHECKING_DEVICE,
        CHECKING_NETWORK,
        CHECKING_MEMBERSHIP,
        EXTRACTING_MODELS,
        READY,
        ERROR
    }

    var state by mutableStateOf(SplashState.CHECKING_DEVICE)
        private set

    var deviceInfo by mutableStateOf<DeviceTierDetector.DeviceInfo?>(null)
        private set

    var isOnline by mutableStateOf(false)
        private set

    var isMember by mutableStateOf(false)
        private set

    var modelProgress by mutableStateOf(0 to 0)
        private set

    var statusText by mutableStateOf("正在检测设备...")
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        startSplash()
    }

    private fun startSplash() {
        viewModelScope.launch {
            try {
                // 第1步：检测设备硬件
                state = SplashState.CHECKING_DEVICE
                statusText = "正在检测设备硬件..."
                delay(500)
                val detector = DeviceTierDetector.getInstance(getApplication())
                deviceInfo = detector.detect()
                statusText = "设备: ${deviceInfo!!.tier.label} (${deviceInfo!!.socModel})"
                delay(300)

                // 第2步：检查网络
                state = SplashState.CHECKING_NETWORK
                statusText = "正在检查网络连接..."
                isOnline = checkNetwork()
                statusText = if (isOnline) "网络已连接" else "未连接网络"
                delay(300)

                // 第3步：检查会员状态（强制联网验证，无后门）
                state = SplashState.CHECKING_MEMBERSHIP
                statusText = "正在验证会员状态..."

                if (isOnline) {
                    val tokenManager = TokenManager(getApplication())
                    val token = tokenManager.loadToken()
                    if (token != null) {
                        val validator = MembershipValidator()
                        isMember = validator.verifyOnline(token)
                        statusText = if (isMember) "会员验证通过" else "免费版"
                    } else {
                        isMember = false
                        statusText = "免费版"
                    }
                } else {
                    isMember = false
                    statusText = "免费版（离线）"
                }
                delay(300)

                // 第4步：检查模型状态
                state = SplashState.EXTRACTING_MODELS
                val registry = ModelRegistry(getApplication())
                modelProgress = registry.getExtractProgress()
                if (modelProgress.first < modelProgress.second) {
                    statusText = "正在初始化AI模型 (${modelProgress.first}/${modelProgress.second})..."
                    val success = registry.extractAllModels()
                    modelProgress = registry.getExtractProgress()
                    if (!success) {
                        statusText = "部分模型初始化失败"
                    } else {
                        statusText = "AI模型就绪"
                    }
                } else {
                    statusText = "AI模型就绪"
                }
                delay(500)

                // 完成
                state = SplashState.READY
                statusText = "准备就绪"

            } catch (e: Exception) {
                state = SplashState.ERROR
                errorMessage = e.message
                statusText = "启动失败: ${e.message}"
            }
        }
    }

    private fun checkNetwork(): Boolean {
        val cm = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun retry() {
        errorMessage = null
        state = SplashState.CHECKING_DEVICE
        startSplash()
    }
}
