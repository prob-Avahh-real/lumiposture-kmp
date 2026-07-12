package com.lumiposture.data

import com.lumiposture.model.MassageMode
import com.lumiposture.model.EcoMode

/// BLE 协议常量 (TI CC254x 芯片, 0xFFE0-0xFFE3 UUID)
///
/// 服务 UUID:
///   - FFE0: 设备服务 (控制命令)
///   - FFE2: 坐姿服务 (压力矩阵通知)
///
/// 特征 UUID:
///   - FFE1: 写入特征 (发送命令: 按摩/节能/加热)
///   - FFE3: 通知特征 (68 字节帧: 64 压力 + 4 传感器)
object LumiBleProtocol {
    const val DEVICE_SERVICE = "0000ffe0-0000-1000-8000-00805f9b34fb"
    const val COMMAND_CHAR = "0000ffe1-0000-1000-8000-00805f9b34fb"
    const val POSTURE_SERVICE = "0000ffe2-0000-1000-8000-00805f9b34fb"
    const val POSTURE_CHAR = "0000ffe3-0000-1000-8000-00805f9b34fb"
    const val DEVICE_NAME_PREFIX = "LUMI"
    const val DOWNSAMPLE_MS = 100L  // 10Hz

    // 命令协议
    fun massageCommand(mode: MassageMode): ByteArray = byteArrayOf(0xA0.toByte(), mode.command.toByte())
    fun ecoCommand(mode: EcoMode): ByteArray = byteArrayOf(0xA1.toByte(), mode.ordinal.toByte())
    fun heaterCommand(enabled: Boolean, temp: Int): ByteArray =
        byteArrayOf(0xA2.toByte(), (if (enabled) 1 else 0).toByte(), temp.coerceIn(35, 45).toByte())
}
