package com.lumiposture.data

import com.lumiposture.model.PostureData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/// InMemorySessionStore 单元测试 — 环形缓冲区 + 今日筛选
class InMemorySessionStoreTest {

    @Test
    fun `adds and counts records`() {
        val store = InMemorySessionStore(maxSize = 100)
        repeat(5) { store.add(PostureData(timestamp = it.toLong() * 1000)) }
        assertEquals(5, store.count())
    }

    @Test
    fun `evicts oldest when full`() {
        val store = InMemorySessionStore(maxSize = 3)
        store.add(PostureData(timestamp = 1))
        store.add(PostureData(timestamp = 2))
        store.add(PostureData(timestamp = 3))
        store.add(PostureData(timestamp = 4))  // 触发淘汰

        assertEquals(3, store.count())
        // 最早的一条 (timestamp=1) 应已淘汰
        val today = store.today()
        assertTrue(today.none { it.timestamp == 1L }, "timestamp=1 应已淘汰")
        assertTrue(today.any { it.timestamp == 4L }, "timestamp=4 应保留")
    }

    @Test
    fun `today filter excludes yesterday`() {
        val store = InMemorySessionStore()
        val yesterday = kotlinx.datetime.Clock.System.now().toEpochMilliseconds() - 86_400_001L
        store.add(PostureData(timestamp = yesterday))
        val today = store.today()
        assertTrue(today.isEmpty(), "昨天的数据不应出现在今天: today.size=${today.size}")
    }

    @Test
    fun `clear empties store`() {
        val store = InMemorySessionStore()
        repeat(10) { store.add(PostureData(timestamp = it.toLong())) }
        store.clear()
        assertEquals(0, store.count())
        assertTrue(store.today().isEmpty())
    }

    @Test
    fun `handles empty store gracefully`() {
        val store = InMemorySessionStore()
        assertEquals(0, store.count())
        assertTrue(store.today().isEmpty())
        store.clear()  // 不应抛异常
    }
}
