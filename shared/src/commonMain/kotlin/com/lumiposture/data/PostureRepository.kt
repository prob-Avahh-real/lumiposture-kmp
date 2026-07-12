package com.lumiposture.data

import com.lumiposture.model.*
import com.lumiposture.platform.BleAdapter
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock

/// 坐姿数据仓库 — BLE 帧解析 + 命令路由
///
/// 职责:
/// - 订阅 BLE 通知流，降采样 (10Hz)
/// - 解析 68 字节帧为 PostureData
/// - 通过 BLE 写入特征发送设备命令
class PostureRepository(private val ble: BleAdapter) : IPostureRepository {
    private var lastRxTime = 0L

    override fun postureStream(): Flow<PostureData> = flow {
        ble.observeNotifications(
            LumiBleProtocol.POSTURE_SERVICE,
            LumiBleProtocol.POSTURE_CHAR
        ).collect { bytes ->
            val now = currentTimeMillis()
            if (now - lastRxTime >= LumiBleProtocol.DOWNSAMPLE_MS) {
                lastRxTime = now
                PostureFrameParser.parse(bytes, now)
                    .onSuccess { emit(it) }
                    .onFailure { /* 跳过损坏帧, 生产环境应记录至遥测 */ }
            }
        }
    }

    override suspend fun setMassageMode(mode: MassageMode) =
        sendCommand(LumiBleProtocol.massageCommand(mode))

    override suspend fun setEcoMode(mode: EcoMode) =
        sendCommand(LumiBleProtocol.ecoCommand(mode))

    override suspend fun setHeater(enabled: Boolean, temp: Int) =
        sendCommand(LumiBleProtocol.heaterCommand(enabled, temp))

    private suspend fun sendCommand(command: ByteArray) {
        ble.writeCharacteristic(LumiBleProtocol.DEVICE_SERVICE, LumiBleProtocol.COMMAND_CHAR, command)
    }

    companion object {
        fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
    }
}
