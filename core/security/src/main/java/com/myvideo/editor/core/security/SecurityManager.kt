package com.myvideo.editor.core.security

import android.content.Context

class SecurityManager(private val context: Context) {
    private val checker = SecurityChecker(context)
    private val riskEngine = RiskScoreEngine()

    data class SecurityResult(val passed: Boolean, val riskScore: Int, val details: List<String>)

    fun checkAll(): SecurityResult {
        val results = mutableListOf<String>()
        var score = 0

        if (checker.isRooted()) { results.add("设备已ROOT"); score += 30 }
        if (checker.isEmulator()) { results.add("检测到模拟器"); score += 20 }
        if (checker.isDebuggerAttached()) { results.add("检测到调试器"); score += 25 }
        if (checker.isHooked()) { results.add("检测到HOOK框架"); score += 25 }
        if (checker.isTampered()) { results.add("应用被篡改"); score += 40 }

        return SecurityResult(score < 50, score, results)
    }

    fun isSecure(): Boolean = checkAll().passed
    fun getRiskScore(): Int = riskEngine.calculate(checker)
}
