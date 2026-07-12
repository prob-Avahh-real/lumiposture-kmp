package com.lumiposture.data

import com.lumiposture.model.PostureData
import kotlin.test.Test
import kotlin.test.assertEquals

/// DailyStats 计算器单元测试
class DailyStatsTest {

    @Test
    fun `empty list returns zeros`() {
        val stats = computeDailyStats(emptyList())
        assertEquals(0.0, stats.totalSittingMinutes)
        assertEquals(0.0, stats.avgTiltAngle)
        assertEquals(0.0, stats.avgSymmetryIndex)
        assertEquals(0.0, stats.goodPostureRate)
    }

    @Test
    fun `single record produces correct stats`() {
        val data = PostureData(
            timestamp = 0,
            tiltAngle = 15.0,
            sittingDurationSeconds = 120
        )
        val stats = computeDailyStats(listOf(data), sampleIntervalSeconds = 2.0)
        assertEquals(2.0 / 60.0, stats.totalSittingMinutes, 0.01)
        assertEquals(15.0, stats.avgTiltAngle, 0.01)
    }

    @Test
    fun `good posture rate calculation`() {
        // 创建 5 个点: 3 个高对称度 (>0.7), 2 个低对称度
        val symmetric3 = symmetricPosture()  // symmetryIndex=1.0
        val asymmetric2 = PostureData(timestamp = 0)  // symmetryIndex=1.0 (空地图)
        // 需要真的不对称数据
        val data = listOf(
            PostureData(timestamp = 0, tiltAngle = 10.0, pressureMap = listOf(
                com.lumiposture.model.PressurePoint(0, 0, 1.0),
                com.lumiposture.model.PressurePoint(0, 1, 0.0)
            )),
        )
        val stats = computeDailyStats(data, sampleIntervalSeconds = 2.0)
        // 单个点 symmetryIndex = 1 - |1.0-0.0|/(1.0+0.0) = 0.0
        assertEquals(0.0, stats.goodPostureRate, "不对称点应有 0% 良好率")
    }

    @Test
    fun `all perfect posture gives 100 percent good rate`() {
        val points = List(10) {
            PostureData(
                timestamp = it.toLong() * 2000,
                tiltAngle = 10.0,
                pressureMap = listOf(
                    com.lumiposture.model.PressurePoint(0, 0, 0.5),
                    com.lumiposture.model.PressurePoint(0, 1, 0.5)
                )
            )
        }
        val stats = computeDailyStats(points, sampleIntervalSeconds = 2.0)
        assertEquals(1.0, stats.goodPostureRate, "全对称压力应给 100% 良好率")
        assertEquals(10 * 2.0 / 60.0, stats.totalSittingMinutes, 0.01)
        assertEquals(10.0, stats.avgTiltAngle, 0.01)
    }

    private fun symmetricPosture() = PostureData(
        timestamp = 0,
        tiltAngle = 10.0,
        pressureMap = listOf(
            com.lumiposture.model.PressurePoint(0, 0, 0.5),
            com.lumiposture.model.PressurePoint(0, 1, 0.5)
        )
    )
}
