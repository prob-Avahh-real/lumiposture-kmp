package com.lumiposture

/// LumiPosture 全局配置 — 单一真理源
///
/// 所有可调参数集中于此, 引擎和 UI 通过此对象引用,
/// 避免魔法数字分散在各模块中。
object LumiConfig {
    const val APP_NAME = "LumiPosture"
    const val APP_NAME_CN = "智能腰托"
    const val BUNDLE_ID_ANDROID = "com.lumiposture.app"
    const val BUNDLE_ID_IOS = "com.lumiposture.app"

    object Ble {
        const val DEVICE_PREFIX = "LUMI"
        const val DOWNSAMPLE_HZ = 10
        const val DOWNSAMPLE_INTERVAL_MS = 100L
    }

    object Fatigue {
        const val OPTIMAL_FORWARD_TILT = 10.0
        const val TILT_WEIGHT = 0.25
        const val ASYMMETRY_WEIGHT = 0.30
        const val DURATION_WEIGHT = 0.30
        const val DURATION_CAP_HOURS = 4.0
        const val STRAIN_DIVISOR_SECONDS = 7200.0
        const val TILT_NORMALIZER = 30.0

        // 阈值
        const val COMFORTABLE_MAX = 25.0
        const val MILD_MAX = 50.0
        const val MODERATE_MAX = 75.0
    }

    object Heater {
        const val MIN_TEMP_C = 35
        const val MAX_TEMP_C = 45
        const val DEFAULT_TEMP_C = 38
    }

    object History {
        const val MAX_RECORDS = 70000            // ~7 天 @ 2 秒采样
        const val SAMPLE_INTERVAL_SECONDS = 2.0
    }

    object Reminder {
        const val POSTURE_INTERVAL_MIN = 15
        const val BREAK_INTERVAL_MIN = 45
        const val FATIGUE_INTERVAL_MIN = 30
        const val MASSAGE_INTERVAL_MIN = 60
        const val HYDRATION_INTERVAL_MIN = 60
        const val EYE_REST_INTERVAL_MIN = 30
    }
}
