package com.kaius.keyboard.transport

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.kaius.keyboard.transport.bluetooth.BluetoothHidTransport
import com.kaius.keyboard.transport.wifi.WifiLanTransport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TransportManager(
    context: Context,
    private val scope: CoroutineScope
) {
    val bluetoothTransport = BluetoothHidTransport(context, scope)
    val wifiTransport = WifiLanTransport(scope)

    private val _currentMode = MutableStateFlow(TransportMode.BLUETOOTH_HID)
    val currentMode: StateFlow<TransportMode> = _currentMode.asStateFlow()

    val currentTransport: InputTransport
        get() = when (_currentMode.value) {
            TransportMode.BLUETOOTH_HID -> bluetoothTransport
            TransportMode.WIFI_LAN -> wifiTransport
        }

    val activeState: StateFlow<TransportState>
        get() = when (_currentMode.value) {
            TransportMode.BLUETOOTH_HID -> bluetoothTransport.state
            TransportMode.WIFI_LAN -> wifiTransport.state
        }

    fun setMode(mode: TransportMode) {
        if (_currentMode.value == mode) return
        _currentMode.value = mode
        when (mode) {
            TransportMode.BLUETOOTH_HID -> {
                bluetoothTransport.initialize()
            }
            TransportMode.WIFI_LAN -> {
                wifiTransport.initialize()
            }
        }
    }

    fun initialize() {
        bluetoothTransport.initialize()
    }

    fun release() {
        bluetoothTransport.release()
        wifiTransport.release()
    }

    fun sendKeyDown(keyCode: Byte, modifiers: Byte) {
        currentTransport.sendKeyDown(keyCode, modifiers)
    }

    fun sendKeyUp(keyCode: Byte, modifiers: Byte) {
        currentTransport.sendKeyUp(keyCode, modifiers)
    }

    fun sendKeyTap(keyCode: Byte, modifiers: Byte) {
        currentTransport.sendKeyTap(keyCode, modifiers)
    }

    fun sendRawReport(modifiers: Byte, keyCodes: ByteArray) {
        currentTransport.sendRawReport(modifiers, keyCodes)
    }

    fun getPairedBluetoothDevices(): List<BluetoothDevice> {
        return bluetoothTransport.getPairedDevices()
    }

    fun connectBluetooth(device: BluetoothDevice) {
        bluetoothTransport.connect(device)
    }

    fun disconnectBluetooth() {
        bluetoothTransport.disconnect()
    }

    fun registerBluetoothHid() {
        bluetoothTransport.registerHidApp()
    }
}
