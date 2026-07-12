package com.lumiposture.data

import com.lumiposture.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/// LumiBleProtocol 命令协议测试
class LumiBleProtocolTest {

    @Test
    fun `massage command encodes mode byte`() {
        MassageMode.entries.forEach { mode ->
            val cmd = LumiBleProtocol.massageCommand(mode)
            assertEquals(2, cmd.size)
            assertEquals(0xA0.toByte(), cmd[0], "命令头应为 0xA0")
            assertEquals(mode.command.toByte(), cmd[1], "模式字节应为 ${mode.command}")
        }
    }

    @Test
    fun `eco command encodes ordinal byte`() {
        EcoMode.entries.forEach { mode ->
            val cmd = LumiBleProtocol.ecoCommand(mode)
            assertEquals(2, cmd.size)
            assertEquals(0xA1.toByte(), cmd[0])
            assertEquals(mode.ordinal.toByte(), cmd[1])
        }
    }

    @Test
    fun `heater command clamps temperature`() {
        // 低于最小值
        val coldCmd = LumiBleProtocol.heaterCommand(enabled = true, temp = 20)
        assertEquals(35, coldCmd[2].toInt() and 0xFF)

        // 高于最大值
        val hotCmd = LumiBleProtocol.heaterCommand(enabled = true, temp = 60)
        assertEquals(45, hotCmd[2].toInt() and 0xFF)

        // 范围内
        val normalCmd = LumiBleProtocol.heaterCommand(enabled = true, temp = 40)
        assertEquals(40, normalCmd[2].toInt() and 0xFF)
    }

    @Test
    fun `heater disabled sends zero enable byte`() {
        val cmd = LumiBleProtocol.heaterCommand(enabled = false, temp = 40)
        assertEquals(0xA2.toByte(), cmd[0])
        assertEquals(0, cmd[1].toInt() and 0xFF, "禁用时第 2 字节应为 0")
    }

    @Test
    fun `UUIDs are full 128-bit format`() {
        assertTrue(LumiBleProtocol.DEVICE_SERVICE.contains("0000ffe0"), "服务 UUID 应包含 TI 0xFFE0")
        assertTrue(LumiBleProtocol.COMMAND_CHAR.contains("0000ffe1"), "特征 UUID 应包含 TI 0xFFE1")
        assertTrue(LumiBleProtocol.POSTURE_SERVICE.contains("0000ffe2"))
        assertTrue(LumiBleProtocol.POSTURE_CHAR.contains("0000ffe3"))
    }
}
