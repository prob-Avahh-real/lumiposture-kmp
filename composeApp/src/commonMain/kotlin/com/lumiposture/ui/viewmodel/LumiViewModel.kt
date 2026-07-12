package com.lumiposture.ui.viewmodel

import com.lumiposture.data.*
import com.lumiposture.engine.IFatigueEngine
import com.lumiposture.model.*
import com.lumiposture.platform.BleAdapter
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/// 主 ViewModel — 编排 BLE + 数据流 + 疲劳计算
///
/// 生命周期: 与 Compose 组件绑定, dispose() 清理协程
class LumiViewModel(
    private val bleAdapter: BleAdapter,
    private val fatigueEngine: IFatigueEngine,
    val appState: AppState = AppState()
) {
    private val repository = PostureRepository(bleAdapter)
    private var scope: CoroutineScope? = null

    val deviceState: StateFlow<DeviceState> = appState.deviceState
    val postureData: StateFlow<PostureData?> = appState.postureData
    val fatigueData: StateFlow<FatigueData?> = appState.fatigueData
    val settings: StateFlow<UserSettings> = appState.settings
    val connectedDevice: StateFlow<String?> = bleAdapter.connectedDevice

    /// 开始监测 — 连接设备, 订阅数据流
    fun startMonitoring(address: String) {
        scope?.cancel()
        scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        appState.deviceState.value = appState.deviceState.value.copy(
            connectionState = DeviceConnectionState.CONNECTING
        )
        scope!!.launch {
            try {
                bleAdapter.connect(address)
                appState.deviceState.value = appState.deviceState.value.copy(
                    connectionState = DeviceConnectionState.CONNECTED
                )
                repository.postureStream().collect { posture ->
                    val fatigue = fatigueEngine.calculate(posture)
                    appState.update(posture, fatigue)
                }
            } catch (e: Exception) {
                appState.deviceState.value = appState.deviceState.value.copy(
                    connectionState = DeviceConnectionState.DISCONNECTED
                )
            }
        }
    }

    fun stopMonitoring() {
        scope?.cancel()
        scope = null
        bleAdapter.disconnect()
        appState.deviceState.value = appState.deviceState.value.copy(
            connectionState = DeviceConnectionState.DISCONNECTED
        )
    }

    suspend fun setMassageMode(mode: MassageMode) {
        repository.setMassageMode(mode)
        appState.deviceState.value = appState.deviceState.value.copy(massageMode = mode)
    }

    suspend fun setEcoMode(mode: EcoMode) {
        repository.setEcoMode(mode)
        appState.deviceState.value = appState.deviceState.value.copy(ecoMode = mode)
    }

    suspend fun setHeater(enabled: Boolean, temp: Int) {
        repository.setHeater(enabled, temp)
        appState.deviceState.value = appState.deviceState.value.copy(
            heaterEnabled = enabled, heaterTemp = temp
        )
    }

    fun dispose() {
        stopMonitoring()
    }
}
