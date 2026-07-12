package com.lumiposture.engine

import com.lumiposture.model.FatigueData
import com.lumiposture.model.PostureData

/// 疲劳计算引擎接口
///
/// 实现策略:
/// - FatigueCalculator: 时域加权线性公式 (当前实现)
/// - 未来: ML 模型引擎 (ONNX 推理)
interface IFatigueEngine {
    fun calculate(data: PostureData): FatigueData
}
