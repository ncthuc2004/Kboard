package com.kaius.keyboard.ui

import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kaius.keyboard.engine.InputEngine
import com.kaius.keyboard.network.DiscoveredReceiver
import com.kaius.keyboard.network.LanDiscovery
import com.kaius.keyboard.network.LanServer
import com.kaius.keyboard.network.ReceiverState
import com.kaius.keyboard.transport.ConnectionStatus
import com.kaius.keyboard.transport.TransportManager
import com.kaius.keyboard.transport.TransportMode
import com.kaius.keyboard.transport.TransportState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

enum class AppRole {
    KEYBOARD,
    RECEIVER
}

class KeyboardViewModel(application: Application) : AndroidViewModel(application) {

    val transportManager = TransportManager(application.applicationContext, viewModelScope)
    val inputEngine = InputEngine(transportManager, viewModelScope)

    // LAN Server (Receiver mode engine) and LAN Discovery (Auto-find receiver)
    val lanServer = LanServer(viewModelScope)
    val lanDiscovery = LanDiscovery(viewModelScope)

    private val _appRole = MutableStateFlow(AppRole.KEYBOARD)
    val appRole: StateFlow<AppRole> = _appRole.asStateFlow()

    val currentMode: StateFlow<TransportMode> = transportManager.currentMode

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val transportState: StateFlow<TransportState> = transportManager.currentMode
        .flatMapLatest { mode ->
            when (mode) {
                TransportMode.BLUETOOTH_HID -> transportManager.bluetoothTransport.state
                TransportMode.WIFI_LAN -> transportManager.wifiTransport.state
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            TransportState(
                mode = TransportMode.BLUETOOTH_HID,
                status = ConnectionStatus.DISCONNECTED,
                statusMessage = "Khởi tạo..."
            )
        )

    val receiverState: StateFlow<ReceiverState> = lanServer.state
    val discoveredReceivers: StateFlow<List<DiscoveredReceiver>> = lanDiscovery.discoveredReceivers
    val isLanScanning: StateFlow<Boolean> = lanDiscovery.isScanning

    val modifiers: StateFlow<Byte> = inputEngine.modifiers

    private val _pairedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val pairedDevices: StateFlow<List<BluetoothDevice>> = _pairedDevices.asStateFlow()

    private val _isDiagnosticsVisible = MutableStateFlow(false)
    val isDiagnosticsVisible: StateFlow<Boolean> = _isDiagnosticsVisible.asStateFlow()

    private val _showWifiDialog = MutableStateFlow(false)
    val showWifiDialog: StateFlow<Boolean> = _showWifiDialog.asStateFlow()

    private val _showPairedDialog = MutableStateFlow(false)
    val showPairedDialog: StateFlow<Boolean> = _showPairedDialog.asStateFlow()

    private val prefs = application.getSharedPreferences("kboard_settings", android.content.Context.MODE_PRIVATE)
    private val _isTelexEnabled = MutableStateFlow(prefs.getBoolean("is_telex_enabled", true))
    val isTelexEnabled: StateFlow<Boolean> = _isTelexEnabled.asStateFlow()

    init {
        val telexOn = _isTelexEnabled.value
        transportManager.wifiTransport.isTelexEnabled = telexOn
        transportManager.initialize()
        refreshPairedDevices()
        lanDiscovery.startDiscovery()
    }

    fun toggleTelex() {
        setTelexEnabled(!_isTelexEnabled.value)
    }

    fun setTelexEnabled(enabled: Boolean) {
        _isTelexEnabled.value = enabled
        prefs.edit().putBoolean("is_telex_enabled", enabled).apply()
        transportManager.sendTelexConfig(enabled)
    }

    fun setAppRole(role: AppRole) {
        _appRole.value = role
        if (role == AppRole.RECEIVER) {
            lanDiscovery.stopDiscovery()
            lanServer.start()
        } else {
            lanServer.stop()
            lanDiscovery.refreshScan()
        }
    }

    fun switchMode(mode: TransportMode) {
        transportManager.setMode(mode)
        if (mode == TransportMode.WIFI_LAN) {
            lanDiscovery.refreshScan()
        }
    }

    fun refreshLanDiscovery() {
        lanDiscovery.refreshScan()
    }

    fun connectToDiscoveredReceiver(receiver: DiscoveredReceiver) {
        updateWifiTarget(receiver.ip, receiver.port)
    }

    fun toggleDiagnostics() {
        _isDiagnosticsVisible.value = !_isDiagnosticsVisible.value
    }

    fun setShowWifiDialog(show: Boolean) {
        _showWifiDialog.value = show
    }

    fun setShowPairedDialog(show: Boolean) {
        if (show) refreshPairedDevices()
        _showPairedDialog.value = show
    }

    fun refreshPairedDevices() {
        _pairedDevices.value = transportManager.getPairedBluetoothDevices()
    }

    fun connectDevice(device: BluetoothDevice) {
        transportManager.connectBluetooth(device)
        setShowPairedDialog(false)
    }

    fun disconnectBluetooth() {
        transportManager.disconnectBluetooth()
    }

    fun reRegisterHid() {
        transportManager.registerBluetoothHid()
    }

    fun updateWifiTarget(ip: String, port: Int) {
        transportManager.wifiTransport.updateTarget(ip, port)
        setShowWifiDialog(false)
    }

    fun sendWifiPing() {
        transportManager.wifiTransport.sendPing()
    }

    fun toggleModifier(modMask: Byte) {
        inputEngine.toggleModifier(modMask)
    }

    fun onKeyDown(keyCode: Byte) {
        inputEngine.onKeyDown(keyCode)
    }

    fun onKeyUp(keyCode: Byte) {
        inputEngine.onKeyUp(keyCode)
    }

    fun onKeyTap(keyCode: Byte) {
        inputEngine.onKeyTap(keyCode)
    }

    fun sendMacro(modMask: Byte, keyCode: Byte) {
        inputEngine.sendMacro(modMask, keyCode)
    }

    fun clearAccumulatedText() {
        lanServer.clearAccumulatedText()
    }

    fun clearEventHistory() {
        lanServer.clearHistory()
    }

    fun restartReceiver() {
        lanServer.stop()
        lanServer.start()
    }

    override fun onCleared() {
        super.onCleared()
        transportManager.release()
        lanServer.stop()
        lanDiscovery.stopDiscovery()
    }
}
