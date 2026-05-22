package com.myvideo.editor.startup

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.myvideo.editor.security.*

/**
 * NexClip 安全模块初始化入口
 * 按优先级顺序初始化14个类目的全部安全模块
 * 主线程只做必要初始化，耗时操作放后台线程
 */
object SecurityInitRunner {

    private var initialized = false
    private val mainHandler = Handler(Looper.getMainLooper())

    // 初始化结果
    data class InitResult(
        val signatureOk: Boolean,
        val antiDebugOk: Boolean,
        val antiHookOk: Boolean,
        val antiInjectOk: Boolean,
        val rootDetectOk: Boolean,
        val memoryOk: Boolean,
        val commOk: Boolean,
        val dataOk: Boolean,
        val uiOk: Boolean,
        val deviceOk: Boolean,
        val monitorOk: Boolean,
        val complianceOk: Boolean,
        val buildOk: Boolean,
        val message: String
    )

    /**
     * 完整安全初始化（APP启动时调用）
     * 主线程：必要检查（签名+反调试+Root）
     * 后台线：其他检查
     */
    fun init(context: Context, onComplete: (InitResult) -> Unit) {
        if (initialized) return

        Thread {
            try {
                // ===== 主线程必须完成的 =====
                val signatureOk = initSignature(context)
                val antiDebugOk = initAntiDebug(context)
                val rootOk = initRootDetection(context)

                // ===== 后台线程 =====
                val antiHookOk = initAntiHook(context)
                val antiInjectOk = initAntiInject(context)
                val memoryOk = initMemory(context)
                val commOk = initCommunication(context)
                val dataOk = initDataProtection(context)
                val uiOk = initUI(context)
                val deviceOk = initDevice(context)
                val monitorOk = initMonitor(context)
                val complianceOk = initCompliance(context)
                val buildOk = initBuildProtection(context)

                initialized = true

                val result = InitResult(
                    signatureOk, antiDebugOk, antiHookOk, antiInjectOk,
                    rootOk, memoryOk, commOk, dataOk, uiOk, deviceOk,
                    monitorOk, complianceOk, buildOk,
                    buildMessage(signatureOk, antiDebugOk, antiHookOk, antiInjectOk,
                        rootOk, memoryOk, commOk, dataOk, uiOk, deviceOk,
                        monitorOk, complianceOk, buildOk)
                )

                // 回到主线程回调
                mainHandler.post { onComplete(result) }

            } catch (e: Exception) {
                mainHandler.post {
                    onComplete(InitResult(false, false, false, false, false,
                        false, false, false, false, false, false, false, false,
                        "初始化异常: ${e.message}"))
                }
            }
        }.start()
    }

    // ===== 各模块初始化 =====

    /** 类目二：签名校验 */
    private fun initSignature(context: Context): Boolean {
        return try {
            SignatureVerifier.verifySignature(context)
        } catch (e: Exception) { false }
    }

    /** 类目三：反调试 */
    private fun initAntiDebug(context: Context): Boolean {
        return try {
            DebuggerDetector.startAntiDebug(context)
        } catch (e: Exception) { false }
    }

    /** 类目四：反Hook */
    private fun initAntiHook(context: Context): Boolean {
        return try {
            HookDetector.startDetection(context)
        } catch (e: Exception) { false }
    }

    /** 类目五：反注入 */
    private fun initAntiInject(context: Context): Boolean {
        return try {
            InjectionDetector.startDetection(context)
        } catch (e: Exception) { false }
    }

    /** 类目六：Root/环境检测 */
    private fun initRootDetection(context: Context): Boolean {
        return try {
            RootDetector.performFullCheck(context)
        } catch (e: Exception) { false }
    }

    /** 类目七：内存安全 */
    private fun initMemory(context: Context): Boolean {
        return try {
            MemoryProtector.startProtection(context)
        } catch (e: Exception) { false }
    }

    /** 类目八：通信安全 */
    private fun initCommunication(context: Context): Boolean {
        return try {
            SecureCommunicator.init(context)
        } catch (e: Exception) { false }
    }

    /** 类目九：数据保护 */
    private fun initDataProtection(context: Context): Boolean {
        return try {
            DataProtector.init(context)
        } catch (e: Exception) { false }
    }

    /** 类目十：界面保护 */
    private fun initUI(context: Context): Boolean {
        return try {
            if (context is Activity) {
                UIProtector.protectActivity(context)
            }
            true
        } catch (e: Exception) { false }
    }

    /** 类目十一：设备识别 */
    private fun initDevice(context: Context): Boolean {
        return try {
            DeviceIdentifier.init(context)
        } catch (e: Exception) { false }
    }

    /** 类目十二：持续监控 */
    private fun initMonitor(context: Context): Boolean {
        return try {
            ContinuousMonitor.startMonitoring(context)
        } catch (e: Exception) { false }
    }

    /** 类目十三：合规 */
    private fun initCompliance(context: Context): Boolean {
        return try {
            val result = ComplianceAuditor.fullInit(context)
            result.privacyOk || result.dataOk
        } catch (e: Exception) { false }
    }

    /** 类目十四：自建加固 */
    private fun initBuildProtection(context: Context): Boolean {
        return try {
            val result = SelfBuildProtector.fullInit(context)
            result.passed
        } catch (e: Exception) { false }
    }

    private fun buildMessage(vararg results: Boolean): String {
        val names = listOf("签名", "反调试", "反Hook", "反注入", "Root检测",
            "内存安全", "通信安全", "数据保护", "界面保护", "设备识别",
            "持续监控", "合规", "自建加固")
        val passed = results.count { it }
        val total = results.size
        return buildString {
            append("安全初始化: $passed/$total 通过\n")
            results.forEachIndexed { i, ok ->
                if (i < names.size) {
                    append("  ${names[i]}: ${if (ok) "✅" else "❌"}\n")
                }
            }
        }
    }

    fun isInitialized(): Boolean = initialized
}
