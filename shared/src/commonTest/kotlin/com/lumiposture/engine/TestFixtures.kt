package com.lumiposture.engine

import com.lumiposture.model.PressurePoint

/// 测试夹具 — 生成用于单元测试的压力矩阵

/// 完美对称压力分布 (左右压力相等)
fun symmetricPressureMap(): List<PressurePoint> =
    (0 until 8).flatMap { r ->
        (0 until 8).map { c ->
            PressurePoint(r, c, 0.5)
        }
    }

/// 不对称压力分布
/// @param leftHeavy true = 左侧压力大, false = 右侧压力大
/// @param extreme true = 极端不对称 (一侧 1.0, 另一侧 0.0)
fun asymmetricPressureMap(leftHeavy: Boolean = true, extreme: Boolean = false): List<PressurePoint> {
    val leftVal = if (extreme) { if (leftHeavy) 1.0 else 0.0 } else { if (leftHeavy) 0.8 else 0.2 }
    val rightVal = if (extreme) { if (leftHeavy) 0.0 else 1.0 } else { if (leftHeavy) 0.2 else 0.8 }
    return (0 until 8).flatMap { r ->
        (0 until 8).map { c ->
            val v = if (c < 4) leftVal else rightVal
            PressurePoint(r, c, v)
        }
    }
}

/// 50% 不对称度 (symmetryIndex ≈ 0.5)
fun halfAsymmetryPressureMap(): List<PressurePoint> {
    return (0 until 8).flatMap { r ->
        (0 until 8).map { c ->
            val v = if (c < 4) 1.0 else 0.0
            PressurePoint(r, c, v)
        }
    }
}
