package com.lumiposture.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/// 数据模型完整性测试 — 枚举 + 数据类
class EntitiesTest {

    @Test
    fun `PostureQuality has 5 levels`() {
        assertEquals(5, PostureQuality.entries.size)
    }

    @Test
    fun `FatigueLevel has 4 levels with distinct thresholds`() {
        assertEquals(4, FatigueLevel.entries.size)
        val labels = FatigueLevel.entries.map { it.label }
        assertEquals(labels.distinct().size, labels.size, "标签应唯一")
    }

    @Test
    fun `MassageMode has 6 modes`() {
        assertEquals(6, MassageMode.entries.size)
    }

    @Test
    fun `EcoMode co2PerHour is monotonic`() {
        val co2 = EcoMode.entries.map { it.co2PerHour }
        for (i in 1 until co2.size) {
            assertTrue(co2[i] >= co2[i - 1], "CO2 应单调递增")
        }
    }

    @Test
    fun `ReminderConfig defaults has 6 items`() {
        assertEquals(6, ReminderConfig.defaults().size)
    }

    @Test
    fun `ReminderConfig posture correct is enabled by default`() {
        val posture = ReminderConfig.defaults().find { it.type == ReminderType.POSTURE_CORRECT }!!
        assertTrue(posture.enabled)
        assertEquals(15, posture.intervalMinutes)
    }

    @Test
    fun `hydration reminder is disabled by default`() {
        val hydration = ReminderConfig.defaults().find { it.type == ReminderType.HYDRATION }!!
        assertTrue(!hydration.enabled, "喝水提醒默认应关闭")
    }

    @Test
    fun `DeviceState has safe defaults`() {
        val state = DeviceState()
        assertEquals(DeviceConnectionState.DISCONNECTED, state.connectionState)
        assertEquals(0, state.batteryLevel)
        assertEquals(EcoMode.OFF, state.ecoMode)
    }

    @Test
    fun `UserSettings has reasonable defaults`() {
        val settings = UserSettings()
        assertEquals(170, settings.heightCm)
        assertEquals(65, settings.weightKg)
        assertEquals(45, settings.sittingAlertIntervalMinutes)
        assertEquals("zh", settings.language)
    }
}
