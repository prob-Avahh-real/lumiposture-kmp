package com.lumiposture.data

import com.lumiposture.model.PressurePoint
import com.lumiposture.model.PostureData

/// BLE 帧解析器 — 将 68 字节原始帧解码为 PostureData
///
/// 帧格式 (68 bytes):
///   bytes[0..63]: 8x8 压力矩阵 (每点 0-255, 归一化为 0.0-1.0)
///   bytes[64]:    前倾角度 (原始值 - 90, 范围 -90~+165)
///   bytes[65]:    左右旋转角度 (原始值 - 90)
///   bytes[66]:    温度 (原始值 × 0.5 + 10, 范围 10-137°C)
///   bytes[67]:    连续坐姿分钟数
object PostureFrameParser {
    private const val FRAME_SIZE = 68
    private const val PRESSURE_COUNT = 64

    fun parse(bytes: ByteArray, timestampMs: Long): Result<PostureData> {
        if (bytes.size < FRAME_SIZE) {
            return Result.failure(IllegalArgumentException(
                "帧大小不足: 期望 $FRAME_SIZE 字节, 收到 ${bytes.size} 字节"
            ))
        }

        val pressureMap = buildList(PRESSURE_COUNT) {
            for (i in 0 until PRESSURE_COUNT) {
                val row = i / 8
                val col = i % 8
                val value = (bytes[i].toInt() and 0xFF) / 255.0
                add(PressurePoint(row, col, value))
            }
        }

        // 传感器数据: 偏移量 + 非线性变换
        val tiltAngle = (bytes[64].toInt() and 0xFF).toDouble() - 90.0
        val rollAngle = (bytes[65].toInt() and 0xFF).toDouble() - 90.0
        val temperature = (bytes[66].toInt() and 0xFF).toDouble() * 0.5 + 10.0
        val sittingDurationSeconds = (bytes[67].toInt() and 0xFF) * 60

        return Result.success(PostureData(
            timestamp = timestampMs,
            pressureMap = pressureMap,
            tiltAngle = tiltAngle,
            rollAngle = rollAngle,
            temperature = temperature,
            sittingDurationSeconds = sittingDurationSeconds
        ))
    }
}
