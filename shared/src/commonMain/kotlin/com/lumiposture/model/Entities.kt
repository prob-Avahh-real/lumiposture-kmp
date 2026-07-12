package com.lumiposture.model

/// 坐姿质量
enum class PostureQuality(val label: String) {
    EXCELLENT("优秀"), GOOD("良好"), FAIR("一般"), POOR("较差"), CRITICAL("严重")
}

/// 疲劳等级
enum class FatigueLevel(val label: String, val emoji: String, val suggestion: String, val hue: Int) {
    COMFORTABLE("舒适", "😊", "继续保持良好坐姿", 0xFF4CAF50.toInt()),
    MILD("轻度疲劳", "😐", "可以微调坐姿", 0xFFFF9800.toInt()),
    MODERATE("中度疲劳", "😟", "建议起身活动", 0xFFFF5722.toInt()),
    SEVERE("重度疲劳", "😫", "请立即起身休息", 0xFFE53935.toInt())
}

/// 设备连接状态
enum class DeviceConnectionState(val label: String) {
    DISCONNECTED("未连接"), CONNECTING("连接中..."), CONNECTED("已连接"), DISCONNECTING("断开中...")
}

/// 节能模式
enum class EcoMode(val label: String, val co2PerHour: Double) {
    OFF("关闭", 0.0), LOW("低", 0.5), MEDIUM("中", 1.2), HIGH("高", 2.0)
}

/// 按摩模式
enum class MassageMode(val label: String, val command: Int) {
    OFF("关闭", 0), GENTLE("轻柔", 1), STANDARD("标准", 2), INTENSIVE("强力", 3), WAVE("波浪", 4), PULSE("脉冲", 5)
}

/// 提醒类型
enum class ReminderType(val label: String) {
    POSTURE_CORRECT("坐姿纠正"), BREAK_TIME("休息提醒"), FATIGUE_WARNING("疲劳警告"),
    MASSAGE_RECOMMEND("按摩建议"), HYDRATION("喝水提醒"), EYE_REST("护眼提醒")
}

/// 8x8 压力点
data class PressurePoint(val row: Int, val col: Int, val value: Double)

/// 坐姿数据
data class PostureData(
    val timestamp: Long,
    val pressureMap: List<PressurePoint> = emptyList(),  // 8x8 = 64 points
    val tiltAngle: Double = 0.0,                         // 前倾角度
    val rollAngle: Double = 0.0,                         // 左右旋转
    val temperature: Double = 36.0,                      // 腰托温度
    val sittingDurationSeconds: Int = 0
) {
    val totalPressure: Double get() = pressureMap.sumOf { it.value }
    val symmetryIndex: Double get() {
        if (pressureMap.isEmpty()) return 1.0
        val leftSum = pressureMap.filter { it.col < 4 }.sumOf { it.value }
        val rightSum = pressureMap.filter { it.col >= 4 }.sumOf { it.value }
        val total = leftSum + rightSum
        return if (total < 1e-10) 1.0 else 1.0 - kotlin.math.abs(leftSum - rightSum) / total
    }

    companion object {
        fun mock(): PostureData {
            val map = (0 until 8).flatMap { r -> (0 until 8).map { c ->
                PressurePoint(r, c, kotlin.random.Random.nextDouble() * 0.8 + 0.2)
            }}
            return PostureData(
                timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
                pressureMap = map,
                tiltAngle = kotlin.random.Random.nextDouble() * 30 - 5,
                rollAngle = kotlin.random.Random.nextDouble() * 10 - 5,
                temperature = 36.0 + kotlin.random.Random.nextDouble() * 2,
                sittingDurationSeconds = kotlin.random.Random.nextInt(3600)
            )
        }
    }
}

/// 设备状态
data class DeviceState(
    val deviceId: String = "",
    val name: String = "",
    val connectionState: DeviceConnectionState = DeviceConnectionState.DISCONNECTED,
    val batteryLevel: Int = 0,
    val rssi: Int = 0,
    val ecoMode: EcoMode = EcoMode.OFF,
    val massageMode: MassageMode = MassageMode.OFF,
    val heaterEnabled: Boolean = false,
    val heaterTemp: Int = 35,     // 35-45°C
    val firmwareVersion: String = ""
)

/// 疲劳数据
data class FatigueData(
    val timestamp: Long,
    val fatigueIndex: Double = 0.0,          // 0-100
    val level: FatigueLevel = FatigueLevel.COMFORTABLE,
    val pressureAsymmetry: Double = 0.0,
    val tiltDeviation: Double = 0.0,
    val continuousSittingMinutes: Double = 0.0,
    val cumulativeStrain: Double = 0.0       // 累积疲劳值
)

/// 提醒配置
data class ReminderConfig(
    val type: ReminderType,
    val enabled: Boolean = true,
    val intervalMinutes: Int = 30,
    val customMessage: String? = null
) {
    companion object {
        fun defaults() = listOf(
            ReminderConfig(ReminderType.POSTURE_CORRECT, intervalMinutes = 15),
            ReminderConfig(ReminderType.BREAK_TIME, intervalMinutes = 45),
            ReminderConfig(ReminderType.FATIGUE_WARNING, intervalMinutes = 30),
            ReminderConfig(ReminderType.MASSAGE_RECOMMEND, intervalMinutes = 60),
            ReminderConfig(ReminderType.HYDRATION, enabled = false, intervalMinutes = 60),
            ReminderConfig(ReminderType.EYE_REST, enabled = false, intervalMinutes = 30)
        )
    }
}

/// 用户设置
data class UserSettings(
    val userName: String = "",
    val heightCm: Int = 170,
    val weightKg: Int = 65,
    val age: Int = 30,
    val notificationsEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val ecoModeAuto: Boolean = true,
    val sittingAlertIntervalMinutes: Int = 45,
    val dailySittingLimitHours: Int = 8,
    val darkMode: Boolean = true,
    val language: String = "zh"
)
