package com.lumiposture.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/// PostureData 计算属性单元测试
class PostureDataTest {

    @Test
    fun `totalPressure sums all values`() {
        val data = PostureData(
            timestamp = 0,
            pressureMap = listOf(
                PressurePoint(0, 0, 0.1),
                PressurePoint(0, 1, 0.2),
                PressurePoint(0, 2, 0.3),
            )
        )
        assertEquals(0.6, data.totalPressure, 0.001)
    }

    @Test
    fun `empty pressureMap gives zero totalPressure`() {
        val data = PostureData(timestamp = 0)
        assertEquals(0.0, data.totalPressure)
    }

    @Test
    fun `symmetryIndex is 1_0 for perfectly symmetric pressure`() {
        val data = PostureData(
            timestamp = 0,
            pressureMap = listOf(
                PressurePoint(0, 0, 0.5),  // left
                PressurePoint(0, 7, 0.5),  // right
            )
        )
        assertEquals(1.0, data.symmetryIndex, 0.01)
    }

    @Test
    fun `symmetryIndex is 1_0 for empty pressureMap`() {
        val data = PostureData(timestamp = 0)
        assertEquals(1.0, data.symmetryIndex)
    }

    @Test
    fun `symmetryIndex is 0_0 for fully asymmetric pressure (all left)`() {
        val data = PostureData(
            timestamp = 0,
            pressureMap = listOf(
                PressurePoint(0, 0, 1.0),  // left only
            )
        )
        assertEquals(0.0, data.symmetryIndex, 0.01)
    }

    @Test
    fun `mock generates valid data`() {
        val data = PostureData.mock()
        assertEquals(64, data.pressureMap.size)
        assertTrue(data.tiltAngle in -5.0..25.0, "倾斜角 ${data.tiltAngle} 应在范围内")
        assertTrue(data.temperature in 36.0..38.0, "温度 ${data.temperature} 应在范围内")
        assertTrue(data.sittingDurationSeconds in 0..3600, "坐姿时长应在 0-3600 秒")
    }

    @Test
    fun `mock totalPressure is positive`() {
        val data = PostureData.mock()
        assertTrue(data.totalPressure > 0, "模拟数据应有正压力值")
    }
}
