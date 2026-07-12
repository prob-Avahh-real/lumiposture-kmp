package com.lumiposture.data

import com.lumiposture.model.EcoMode
import com.lumiposture.model.MassageMode
import com.lumiposture.model.PostureData
import kotlinx.coroutines.flow.Flow

/// 坐姿数据仓库接口
interface IPostureRepository {
    /// 订阅实时坐姿数据流 (10Hz 降采样)
    fun postureStream(): Flow<PostureData>

    /// 设置按摩模式
    suspend fun setMassageMode(mode: MassageMode)

    /// 设置节能模式
    suspend fun setEcoMode(mode: EcoMode)

    /// 设置加热状态
    suspend fun setHeater(enabled: Boolean, temp: Int)
}
