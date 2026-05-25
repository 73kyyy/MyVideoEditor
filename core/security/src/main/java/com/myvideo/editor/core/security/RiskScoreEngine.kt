package com.myvideo.editor.core.security

class RiskScoreEngine {
    data class RiskFactor(val name: String, val weight: Int, val detected: Boolean = false)

    private val factors = listOf(
        RiskFactor("ROOT", 30), RiskFactor("模拟器", 20), RiskFactor("调试器", 25),
        RiskFactor("HOOK", 25), RiskFactor("篡改", 40), RiskFactor("VPN", 10),
        RiskFactor("代理", 10), RiskFactor("异常环境", 15)
    )

    fun calculate(checker: SecurityChecker): Int {
        var score = 0
        if (checker.isRooted()) score += 30
        if (checker.isEmulator()) score += 20
        if (checker.isDebuggerAttached()) score += 25
        if (checker.isHooked()) score += 25
        if (checker.isTampered()) score += 40
        return score.coerceIn(0, 100)
    }

    fun getRiskLevel(score: Int): String = when {
        score >= 80 -> "极高风险"; score >= 50 -> "高风险"
        score >= 30 -> "中风险"; score >= 10 -> "低风险"
        else -> "安全"
    }

    fun getFactors(): List<RiskFactor> = factors
}
