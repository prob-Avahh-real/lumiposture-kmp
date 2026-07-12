package com.lumiposture.data

import com.lumiposture.model.PressurePoint
import com.lumiposture.model.PostureData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/// PostureFrameParser 单元测试 — 68 字节帧解析
class PostureFrameParserTest {

    @Test
    fun `parses valid 68-byte frame correctly`() {
        val bytes = ByteArray(68) { i ->
            when {
                i < 64 -> 128  // 中点压力 (128/255 ≈ 0.502)
                i == 64 -> 100 // tilt = 100-90 = 10°
                i == 65 -> 90  // roll = 90-90 = 0°
                i == 66 -> 52  // temp = 52*0.5+10 = 36°C
                i == 67 -> 30  // 30 minutes sitting
                else -> 0
            }
        }

        val result = PostureFrameParser.parse(bytes, 1000L)

        assertTrue(result.isSuccess, "应成功解析: ${result.exceptionOrNull()}")
        val data = result.getOrThrow()
        assertEquals(1000L, data.timestamp)
        assertEquals(64, data.pressureMap.size)
        assertEquals(10.0, data.tiltAngle, 0.1)
        assertEquals(0.0, data.rollAngle, 0.1)
        assertEquals(36.0, data.temperature, 0.1)
        assertEquals(1800, data.sittingDurationSeconds)  // 30*60
    }

    @Test
    fun `rejects frame smaller than 68 bytes`() {
        val bytes = ByteArray(32)
        val result = PostureFrameParser.parse(bytes, 0L)

        assertTrue(result.isFailure, "应拒绝不完整帧")
        val error = result.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
        assertTrue(error!!.message!!.contains("帧大小不足"))
    }

    @Test
    fun `handles all-zero frame`() {
        val bytes = ByteArray(68)
        val result = PostureFrameParser.parse(bytes, 0L)

        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertTrue(data.pressureMap.all { it.value == 0.0 }, "零帧压力应全为零")
        assertEquals(-90.0, data.tiltAngle, 0.01)   // 0-90
        assertEquals(-90.0, data.rollAngle, 0.01)
        assertEquals(10.0, data.temperature, 0.01)   // 0*0.5+10
        assertEquals(0, data.sittingDurationSeconds)
    }

    @Test
    fun `handles max-value frame`() {
        val bytes = ByteArray(68) { 0xFF.toByte() }
        val result = PostureFrameParser.parse(bytes, 0L)

        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        // 所有压力应为 1.0 (255/255)
        assertTrue(data.pressureMap.all { it.value == 1.0 }, "全 255 压力应全为 1.0")
        assertEquals(165.0, data.tiltAngle, 0.01)    // 255-90
        assertEquals(75.5, data.temperature, 0.01)    // 255*0.5+10
        assertEquals(15300, data.sittingDurationSeconds)  // 255*60
    }

    @Test
    fun `pressure map has correct coordinates`() {
        val bytes = ByteArray(68) { i ->
            if (i < 64) (i % 256).toByte() else 0
        }
        val result = PostureFrameParser.parse(bytes, 0L)
        val data = result.getOrThrow()

        // 验证 8x8 网格坐标
        val coords = data.pressureMap.map { it.row to it.col }.toSet()
        assertEquals(64, coords.size, "应有 64 个唯一点")
        for (r in 0 until 8) {
            for (c in 0 until 8) {
                assertTrue(coords.contains(r to c), "缺少坐标 ($r, $c)")
            }
        }
    }

    @Test
    fun `temperature is clamped by sensor range (not by parser)`() {
        // 解析器不做温度校验 — 由上层负责
        val hot = ByteArray(68) { i -> if (i == 66) 200 else 0 }
        val result = PostureFrameParser.parse(hot, 0L)
        assertEquals(110.0, result.getOrThrow().temperature, 0.1)
    }
}
