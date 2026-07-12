package com.lumiposture.engine

import com.lumiposture.model.FatigueLevel
import com.lumiposture.model.PostureData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/// FatigueCalculator 单元测试 — 覆盖所有疲劳等级和边界条件
class FatigueCalculatorTest {
    private val calculator = FatigueCalculator()

    // ─── 舒适等级 (< 25) ───────────────────────────────

    @Test
    fun `perfect posture — no tilt, perfect symmetry, 0 sitting time`() {
        val data = PostureData(
            timestamp = 0,
            tiltAngle = 10.0,         // 最佳前倾角
            sittingDurationSeconds = 0,
            pressureMap = symmetricPressureMap()
        )
        val result = calculator.calculate(data)

        assertEquals(FatigueLevel.COMFORTABLE, result.level)
        assertTrue(result.fatigueIndex < 5.0, "理想坐姿疲劳应接近 0, 实际 ${result.fatigueIndex}")
    }

    @Test
    fun `mild fatigue — slight tilt deviation, short sitting`() {
        val data = PostureData(
            timestamp = 0,
            tiltAngle = 20.0,         // 偏离最佳 10°
            sittingDurationSeconds = 600,  // 10 分钟
            pressureMap = symmetricPressureMap()
        )
        val result = calculator.calculate(data)

        assertEquals(FatigueLevel.MILD, result.level)
        assertTrue(result.fatigueIndex in 25.0..49.9, "指数 ${result.fatigueIndex} 应在 [25, 50)")
    }

    @Test
    fun `moderate fatigue — significant asymmetry + sitting`() {
        val data = PostureData(
            timestamp = 0,
            tiltAngle = 25.0,         // 偏离 15°
            sittingDurationSeconds = 5400,  // 1.5 小时
            pressureMap = asymmetricPressureMap(leftHeavy = true)
        )
        val result = calculator.calculate(data)

        assertEquals(FatigueLevel.MODERATE, result.level)
        assertTrue(result.fatigueIndex in 50.0..74.9, "指数 ${result.fatigueIndex} 应在 [50, 75)")
    }

    @Test
    fun `severe fatigue — max tilt, max asymmetry, 4+ hours sitting`() {
        val data = PostureData(
            timestamp = 0,
            tiltAngle = 45.0,         // 偏离 35° (归一化→1.0)
            sittingDurationSeconds = 14400,  // 4 小时
            pressureMap = asymmetricPressureMap(leftHeavy = false, extreme = true)
        )
        val result = calculator.calculate(data)

        assertEquals(FatigueLevel.SEVERE, result.level)
        assertTrue(result.fatigueIndex >= 75.0, "指数 ${result.fatigueIndex} 应 >= 75")
    }

    // ─── 边界条件 ────────────────────────────────────────

    @Test
    fun `fatigue index never exceeds 100`() {
        val extreme = PostureData(
            timestamp = 0,
            tiltAngle = 100.0,         // 超范围输入
            sittingDurationSeconds = 86400,  // 24 小时
            pressureMap = asymmetricPressureMap(extreme = true)
        )
        val result = calculator.calculate(extreme)
        assertTrue(result.fatigueIndex <= 100.0, "疲劳指数 ${result.fatigueIndex} 不应超过 100")
    }

    @Test
    fun `fatigue index never negative`() {
        val data = PostureData(
            timestamp = 0,
            tiltAngle = 10.0,
            sittingDurationSeconds = 0,
            pressureMap = symmetricPressureMap()
        )
        val result = calculator.calculate(data)
        assertTrue(result.fatigueIndex >= 0.0, "疲劳指数不应为负: ${result.fatigueIndex}")
    }

    @Test
    fun `empty pressure map produces symmetry 1_0`() {
        val data = PostureData(timestamp = 0)
        val result = calculator.calculate(data)

        assertTrue(result.pressureAsymmetry == 0.0, "空压力图应对称")
        assertEquals(FatigueLevel.COMFORTABLE, result.level)
    }

    @Test
    fun `cumulative strain grows with sitting time`() {
        val short = calculator.calculate(PostureData(
            timestamp = 0, tiltAngle = 25.0, sittingDurationSeconds = 600,
            pressureMap = asymmetricPressureMap()
        ))
        val long = calculator.calculate(PostureData(
            timestamp = 0, tiltAngle = 25.0, sittingDurationSeconds = 3600,
            pressureMap = asymmetricPressureMap()
        ))

        assertTrue(long.cumulativeStrain > short.cumulativeStrain,
            "长时间久坐应有更高的累积疲劳: short=${short.cumulativeStrain}, long=${long.cumulativeStrain}")
    }

    @Test
    fun `cumulative strain clamped to 200`() {
        val data = PostureData(
            timestamp = 0,
            tiltAngle = 45.0,
            sittingDurationSeconds = 360000,  // 100 小时
            pressureMap = asymmetricPressureMap(extreme = true)
        )
        val result = calculator.calculate(data)
        assertTrue(result.cumulativeStrain <= 200.0,
            "累积疲劳 ${result.cumulativeStrain} 不应超过 200")
    }

    // ─── 阈值边界 ────────────────────────────────────────

    @Test
    fun `threshold boundary — exactly 25`() {
        // 构造恰好 25 的输入: tilt=25, asymmetry=0.5, duration=0.25
        val data = PostureData(
            timestamp = 0,
            tiltAngle = 25.0,         // deviation=15, normalized=0.5
            sittingDurationSeconds = 3600,  // 1h, factor=0.25
            pressureMap = halfAsymmetryPressureMap()
        )
        val result = calculator.calculate(data)
        // 0.5*0.25 + 0.5*0.30 + 0.25*0.30 = 0.125+0.15+0.075=0.35*100=35 >= 25
        assertEquals(FatigueLevel.MILD, result.level)
    }

    // ─── 精度验证 ────────────────────────────────────────

    @Test
    fun `FFT-free formula — deterministic for same input`() {
        val data = PostureData(timestamp = 0, tiltAngle = 15.0,
            sittingDurationSeconds = 1800, pressureMap = symmetricPressureMap())
        val r1 = calculator.calculate(data)
        val r2 = calculator.calculate(data)
        assertEquals(r1.fatigueIndex, r2.fatigueIndex, "相同输入应有相同输出 (确定性)")
    }

    @Test
    fun `continuous sitting minutes correctly computed`() {
        repeat(5) { hours ->
            val seconds = hours * 3600
            val data = PostureData(timestamp = 0, tiltAngle = 10.0,
                sittingDurationSeconds = seconds)
            val result = calculator.calculate(data)
            assertEquals(hours * 60.0, result.continuousSittingMinutes,
                "应为 ${hours * 60} 分钟, 实际 ${result.continuousSittingMinutes}")
        }
    }
}
