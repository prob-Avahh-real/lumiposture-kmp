package com.lumiposture.engine

import com.lumiposture.model.FatigueData
import com.lumiposture.model.FatigueLevel
import com.lumiposture.model.PostureData
import kotlin.math.abs
import kotlin.math.min

/// 时域加权线性疲劳计算器
///
/// 公式:
///   fatigueIndex = (tiltDeviation × W_TILT + pressureAsymmetry × W_ASYM + durationFactor × W_DUR) × 100
///   cumulativeStrain = fatigueIndex × (1 + sittingSeconds / STRAIN_DIVISOR)
///
/// 阈值: <25 舒适, 25-50 轻度, 50-75 中度, >75 重度
class FatigueCalculator : IFatigueEngine {
    private companion object {
        const val OPTIMAL_TILT = 10.0        // 最佳前倾角度
        const val W_TILT = 0.25              // 倾斜权重
        const val W_ASYM = 0.30              // 不对称权重
        const val W_DUR = 0.30               // 久坐权重
        const val DURATION_CAP = 4.0          // 久坐因子上限 (小时)
        const val STRAIN_DIVISOR = 7200.0     // 累积疲劳分母 (2 小时秒数)
        const val TILT_NORMALIZER = 30.0      // 倾斜归一化因子
    }

    override fun calculate(data: PostureData): FatigueData {
        val tiltDeviation = min(abs(data.tiltAngle - OPTIMAL_TILT) / TILT_NORMALIZER, 1.0)
        val pressureAsymmetry = 1.0 - data.symmetryIndex
        val sittingHours = data.sittingDurationSeconds / 3600.0
        val durationFactor = min(sittingHours / DURATION_CAP, 1.0)

        val fatigueIndex = clamp(
            (tiltDeviation * W_TILT + pressureAsymmetry * W_ASYM + durationFactor * W_DUR) * 100.0,
            0.0, 100.0
        )

        val level = classifyFatigue(fatigueIndex)

        return FatigueData(
            timestamp = data.timestamp,
            fatigueIndex = fatigueIndex,
            level = level,
            pressureAsymmetry = pressureAsymmetry,
            tiltDeviation = tiltDeviation,
            continuousSittingMinutes = sittingHours * 60.0,
            cumulativeStrain = cumulativeFatigue(fatigueIndex, data.sittingDurationSeconds)
        )
    }

    private fun classifyFatigue(index: Double): FatigueLevel = when {
        index < 25.0 -> FatigueLevel.COMFORTABLE
        index < 50.0 -> FatigueLevel.MILD
        index < 75.0 -> FatigueLevel.MODERATE
        else -> FatigueLevel.SEVERE
    }

    private fun cumulativeFatigue(index: Double, sittingSeconds: Int): Double =
        index * (1.0 + sittingSeconds / STRAIN_DIVISOR).coerceAtMost(200.0)

    private fun clamp(value: Double, min: Double, max: Double): Double =
        value.coerceIn(min, max)
}
