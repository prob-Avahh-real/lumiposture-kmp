package com.lumiposture.data

import com.lumiposture.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Clock

/// 会话数据存储接口
interface ISessionStore {
    fun add(data: PostureData)
    fun today(): List<PostureData>
    fun count(): Int
    fun clear()
}

/// 内存会话存储 — 环形缓冲区, 上限 70000 条 (≈7 天 @ 每 2 秒)
class InMemorySessionStore(private val maxSize: Int = 70000) : ISessionStore {
    private val buffer = ArrayDeque<PostureData>(maxSize)

    override fun add(data: PostureData) {
        if (buffer.size >= maxSize) buffer.removeFirst()
        buffer.addLast(data)
    }

    override fun today(): List<PostureData> {
        val dayStart = todayStartMs()
        return buffer.filter { it.timestamp >= dayStart }
    }

    override fun count(): Int = buffer.size
    override fun clear() { buffer.clear() }

    private fun todayStartMs(): Long {
        val now = Clock.System.now().toEpochMilliseconds()
        return now - (now % 86_400_000L)
    }
}

/// 应用状态容器 — 集中式可观察状态 + 历史数据管理
///
/// 替代 Flutter Provider 模式。所有 UI 层通过此容器读取和订阅状态。
class AppState(private val sessionStore: ISessionStore = InMemorySessionStore()) {
    val deviceState: MutableStateFlow<DeviceState> = MutableStateFlow(DeviceState())
    val postureData: MutableStateFlow<PostureData?> = MutableStateFlow(null)
    val fatigueData: MutableStateFlow<FatigueData?> = MutableStateFlow(null)
    val settings: MutableStateFlow<UserSettings> = MutableStateFlow(UserSettings())

    fun addToHistory(data: PostureData) = sessionStore.add(data)
    fun todayHistory(): List<PostureData> = sessionStore.today()
    fun historyCount(): Int = sessionStore.count()
    fun clearHistory() = sessionStore.clear()

    /// 批量更新 (原子性)
    fun update(posture: PostureData, fatigue: FatigueData) {
        postureData.value = posture
        fatigueData.value = fatigue
        addToHistory(posture)
    }
}
