package com.lumiposture.engine

import com.lumiposture.model.FatigueData
import com.lumiposture.model.PostureData
import kotlin.test.Test
import kotlin.test.assertEquals

/// IFatigueEngine 接口契约测试 — 验证实现符合接口规范
class FatigueEngineContractTest {

    @Test
    fun `interface accepts FatigueCalculator`() {
        val engine: IFatigueEngine = FatigueCalculator()
        val result = engine.calculate(PostureData(timestamp = 0))
        assertEquals(0.0, result.fatigueIndex, 0.01)
    }

    @Test
    fun `calculates fatigue for every FatigueLevel`() {
        val engine: IFatigueEngine = FatigueCalculator()

        val inputs = listOf(
            // (tilt, sittingSeconds, expectedLabel)
            Triple(10.0, 0) to "舒适",        // 完美坐姿
            Triple(20.0, 1800) to "轻度疲劳",  // 30min, 稍偏
            Triple(30.0, 7200) to "中度疲劳",  // 2h, 偏 20°
            Triple(40.0, 14400) to "重度疲劳", // 4h, 偏 30°
        )

        for ((params, expected) in inputs) {
            val data = PostureData(
                timestamp = 0,
                tiltAngle = params.first,
                sittingDurationSeconds = params.second
            )
            val result = engine.calculate(data)
            assertEquals(expected, result.level.label,
                "tilt=${params.first}, dur=${params.second}s → 期望 $expected, 实际 ${result.level.label}")
        }
    }
}
