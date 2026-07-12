package com.lumiposture.data

import com.lumiposture.model.PostureData

/// 每日统计数据
data class DailyStats(
    val totalSittingMinutes: Double = 0.0,
    val avgTiltAngle: Double = 0.0,
    val avgSymmetryIndex: Double = 0.0,
    val goodPostureRate: Double = 0.0,
    val breakCount: Int = 0
)

/// 从今日数据计算统计摘要
///
/// 采样间隔假设: 2 秒/点 (与 10Hz 降采样一致)
/// 良好坐姿阈值: symmetryIndex > 0.7
fun computeDailyStats(todayData: List<PostureData>, sampleIntervalSeconds: Double = 2.0): DailyStats {
    if (todayData.isEmpty()) return DailyStats()

    val totalMinutes = todayData.size * sampleIntervalSeconds / 60.0
    val avgTilt = todayData.map { it.tiltAngle }.average()
    val avgSym = todayData.map { it.symmetryIndex }.average()
    val goodRate = todayData.count { it.symmetryIndex > 0.7 }.toDouble() / todayData.size

    return DailyStats(
        totalSittingMinutes = totalMinutes,
        avgTiltAngle = avgTilt,
        avgSymmetryIndex = avgSym,
        goodPostureRate = goodRate
    )
}
