package com.lumiposture.platform

// iOS BLE adapter via Kable (CoreBluetooth)
import com.juul.kable.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

actual class BleAdapter {
    private val _connectedDevice = MutableStateFlow<String?>(null)
    val connectedDevice: StateFlow<String?> = _connectedDevice

    private var peripheral: Peripheral? = null
    private var connectionScope: CoroutineScope? = null

    actual fun scan(): Flow<ScanResult> {
        val scanner = Scanner {
            filters { match { namePrefix = listOf(com.lumiposture.data.LumiBleProtocol.DEVICE_NAME_PREFIX) } }
        }
        return scanner.advertisements
            .map { ad -> ScanResult(ad.name, ad.address, ad.rssi) }
            .distinctUntilChanged { it.address }
    }

    actual suspend fun connect(address: String) {
        disconnect()
        val p = Peripheral(address)
        val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        scope.launch {
            p.connect()
            _connectedDevice.value = address
        }
        peripheral = p
        connectionScope = scope
    }

    actual fun disconnect() {
        connectionScope?.cancel()
        connectionScope = null
        peripheral = null
        _connectedDevice.value = null
    }

    actual fun observeNotifications(serviceUuid: String, charUuid: String): Flow<ByteArray> {
        val p = peripheral ?: return emptyFlow()
        // Kable iOS 使用 platform.Foundation.NSUUID (真实 ObjC 互操作)
        return p.observe(platform.Foundation.NSUUID(uuidString = charUuid))
            .mapNotNull { it?.toByteArray() }
    }

    actual suspend fun writeCharacteristic(serviceUuid: String, charUuid: String, data: ByteArray) {
        peripheral?.write(platform.Foundation.NSUUID(uuidString = charUuid), data)
    }
}
