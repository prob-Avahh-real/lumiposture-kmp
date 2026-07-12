package com.lumiposture.data

import com.lumiposture.model.FatigueLevel
import com.lumiposture.model.PostureData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/// AppState 集成测试 — 批量更新 + 历史追踪
class AppStateTest {

    @Test
    fun `initial state is clean`() {
        val state = AppState()
        assertEquals(null, state.postureData.value)
        assertEquals(null, state.fatigueData.value)
        assertEquals(0, state.historyCount())
    }

    @Test
    fun `update writes posture and fatigue atomically`() {
        val state = AppState()
        val posture = PostureData(timestamp = 1000, tiltAngle = 15.0)
        val fatigue = com.lumiposture.model.FatigueData(
            timestamp = 1000,
            fatigueIndex = 42.0,
            level = FatigueLevel.MILD
        )

        state.update(posture, fatigue)

        assertEquals(15.0, state.postureData.value?.tiltAngle)
        assertEquals(42.0, state.fatigueData.value?.fatigueIndex)
        assertEquals(1, state.historyCount())
    }

    @Test
    fun `todayHistory filters by current day`() {
        val state = AppState()
        val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        val yesterday = now - 86_400_001L

        state.addToHistory(PostureData(timestamp = yesterday))
        state.addToHistory(PostureData(timestamp = now))

        val today = state.todayHistory()
        assertTrue(today.all { it.timestamp >= now - (now % 86_400_000L) },
            "todayHistory 应只返回今天数据")
    }

    @Test
    fun `clearHistory removes all records`() {
        val state = AppState()
        repeat(5) { state.addToHistory(PostureData(timestamp = it.toLong())) }
        state.clearHistory()
        assertEquals(0, state.historyCount())
    }

    @Test
    fun `settings flow is mutable`() {
        val state = AppState()
        val newSettings = com.lumiposture.model.UserSettings(
            userName = "测试用户", heightCm = 175
        )
        state.settings.value = newSettings
        assertEquals("测试用户", state.settings.value.userName)
        assertEquals(175, state.settings.value.heightCm)
    }
}
