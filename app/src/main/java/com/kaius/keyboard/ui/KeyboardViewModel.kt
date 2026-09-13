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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
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

    // Language & Telex
    private val _isTelexEnabled = MutableStateFlow(prefs.getBoolean("is_telex_enabled", true))
    val isTelexEnabled: StateFlow<Boolean> = _isTelexEnabled.asStateFlow()

    // RGB Backlight & Visuals
    private val _rgbTheme = MutableStateFlow(prefs.getString("rgb_theme", "DARK_INDUSTRIAL") ?: "DARK_INDUSTRIAL")
    val rgbTheme: StateFlow<String> = _rgbTheme.asStateFlow()

    private val _isReactiveGlow = MutableStateFlow(prefs.getBoolean("reactive_glow", true))
    val isReactiveGlow: StateFlow<Boolean> = _isReactiveGlow.asStateFlow()

    // Haptics & Sound Feedback
    private val _isHapticEnabled = MutableStateFlow(prefs.getBoolean("haptic_enabled", true))
    val isHapticEnabled: StateFlow<Boolean> = _isHapticEnabled.asStateFlow()

    private val _hapticStrength = MutableStateFlow(prefs.getString("haptic_strength", "MEDIUM") ?: "MEDIUM")
    val hapticStrength: StateFlow<String> = _hapticStrength.asStateFlow()

    private val _isSoundEnabled = MutableStateFlow(prefs.getBoolean("sound_enabled", false))
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    // Hardware Key Repeat
    private val _repeatDelayMs = MutableStateFlow(prefs.getLong("repeat_delay_ms", 380L))
    val repeatDelayMs: StateFlow<Long> = _repeatDelayMs.asStateFlow()

    private val _repeatIntervalMs = MutableStateFlow(prefs.getLong("repeat_interval_ms", 45L))
    val repeatIntervalMs: StateFlow<Long> = _repeatIntervalMs.asStateFlow()

    // Settings Modal State
    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()
    private val _settingsInitialTab = MutableStateFlow(0)
    val settingsInitialTab: StateFlow<Int> = _settingsInitialTab.asStateFlow()

    // Keyboard Active / Standby State (Bật / Tắt Bàn phím khi không dùng)
    private val _isKeyboardActive = MutableStateFlow(true)
    val isKeyboardActive: StateFlow<Boolean> = _isKeyboardActive.asStateFlow()

    fun toggleKeyboardActive() {
        setKeyboardActive(!_isKeyboardActive.value)
    }

    fun setKeyboardActive(active: Boolean) {
        _isKeyboardActive.value = active
        if (active) {
            if (currentMode.value == TransportMode.WIFI_LAN) {
                lanDiscovery.startDiscovery()
            }
        } else {
            lanDiscovery.stopDiscovery()
        }
    }

    private val _myLocalIp = MutableStateFlow(lanServer.getLocalIpAddress())
    val myLocalIp: StateFlow<String> = _myLocalIp.asStateFlow()

    init {
        val telexOn = _isTelexEnabled.value
        transportManager.wifiTransport.isTelexEnabled = telexOn
        transportManager.initialize()
        refreshPairedDevices()
        lanDiscovery.startDiscovery()

        // Periodically refresh my local IP to keep UI in sync with Wi-Fi / Hotspot changes
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(3000L)
                val ip = lanServer.getLocalIpAddress()
                if (ip != _myLocalIp.value && ip != "127.0.0.1") {
                    _myLocalIp.value = ip
                }
            }
        }

        // Auto-connect to discovered receiver on the same network
        viewModelScope.launch {
            lanDiscovery.discoveredReceivers.collect { receivers ->
                if (receivers.size == 1) {
                    val single = receivers.first()
                    val currentTarget = transportManager.wifiTransport.state.value.wifiTargetIp
                    if (currentTarget != single.ip) {
                        updateWifiTarget(single.ip, single.port)
                    }
                }
            }
        }
    }

    fun openSettings(tab: Int = 0) {
        _settingsInitialTab.value = tab
        _showSettingsDialog.value = true
    }

    fun closeSettings() {
        _showSettingsDialog.value = false
    }

    fun toggleTelex() {
        setTelexEnabled(!_isTelexEnabled.value)
    }

    fun setTelexEnabled(enabled: Boolean) {
        _isTelexEnabled.value = enabled
        prefs.edit().putBoolean("is_telex_enabled", enabled).apply()
        transportManager.sendTelexConfig(enabled)
    }

    fun setRgbTheme(themeId: String) {
        _rgbTheme.value = themeId
        prefs.edit().putString("rgb_theme", themeId).apply()
    }

    fun setReactiveGlow(enabled: Boolean) {
        _isReactiveGlow.value = enabled
        prefs.edit().putBoolean("reactive_glow", enabled).apply()
    }

    fun setHapticEnabled(enabled: Boolean) {
        _isHapticEnabled.value = enabled
        prefs.edit().putBoolean("haptic_enabled", enabled).apply()
    }

    fun setHapticStrength(strength: String) {
        _hapticStrength.value = strength
        prefs.edit().putString("haptic_strength", strength).apply()
    }

    fun setSoundEnabled(enabled: Boolean) {
        _isSoundEnabled.value = enabled
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
    }

    fun setRepeatDelayMs(delay: Long) {
        _repeatDelayMs.value = delay
        prefs.edit().putLong("repeat_delay_ms", delay).apply()
    }

    fun setRepeatIntervalMs(interval: Long) {
        _repeatIntervalMs.value = interval
        prefs.edit().putLong("repeat_interval_ms", interval).apply()
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
        if (show) openSettings(4)
        else _showSettingsDialog.value = false
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
