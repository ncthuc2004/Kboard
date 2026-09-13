package com.kaius.keyboard.transport.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppQosSettings
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import com.kaius.keyboard.transport.ConnectionStatus
import com.kaius.keyboard.transport.InputTransport
import com.kaius.keyboard.transport.LogEntry
import com.kaius.keyboard.transport.TransportMode
import com.kaius.keyboard.transport.TransportState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

class BluetoothHidTransport(
    private val context: Context,
    private val scope: CoroutineScope
) : InputTransport {

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private var hidDevice: BluetoothHidDevice? = null
    private var connectedDevice: BluetoothDevice? = null
    private var isAppRegistered = false

    private val executor = Executors.newSingleThreadExecutor()
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    private val _state = MutableStateFlow(
        TransportState(
            mode = TransportMode.BLUETOOTH_HID,
            status = ConnectionStatus.DISCONNECTED,
            statusMessage = "Chưa kết nối Bluetooth"
        )
    )
    override val state: StateFlow<TransportState> = _state.asStateFlow()

    companion object {
        const val REPORT_ID_KEYBOARD: Byte = 1
        const val SUBCLASS_KEYBOARD: Byte = 0x40.toByte()

        // Standard USB HID Keyboard Report Descriptor
        val KEYBOARD_REPORT_DESCRIPTOR = byteArrayOf(
            0x05.toByte(), 0x01.toByte(),       // USAGE_PAGE (Generic Desktop)
            0x09.toByte(), 0x06.toByte(),       // USAGE (Keyboard)
            0xA1.toByte(), 0x01.toByte(),       // COLLECTION (Application)
            0x85.toByte(), REPORT_ID_KEYBOARD,  //   REPORT_ID (1)
            0x05.toByte(), 0x07.toByte(),       //   USAGE_PAGE (Keyboard/Keypad)
            0x19.toByte(), 0xE0.toByte(),       //   USAGE_MINIMUM (Keyboard LeftControl)
            0x29.toByte(), 0xE7.toByte(),       //   USAGE_MAXIMUM (Keyboard Right GUI)
            0x15.toByte(), 0x00.toByte(),       //   LOGICAL_MINIMUM (0)
            0x25.toByte(), 0x01.toByte(),       //   LOGICAL_MAXIMUM (1)
            0x75.toByte(), 0x01.toByte(),       //   REPORT_SIZE (1)
            0x95.toByte(), 0x08.toByte(),       //   REPORT_COUNT (8)
            0x81.toByte(), 0x02.toByte(),       //   INPUT (Data,Var,Abs) - Modifiers
            0x95.toByte(), 0x01.toByte(),       //   REPORT_COUNT (1)
            0x75.toByte(), 0x08.toByte(),       //   REPORT_SIZE (8)
            0x81.toByte(), 0x01.toByte(),       //   INPUT (Cnst,Ary,Abs) - Reserved byte
            0x95.toByte(), 0x05.toByte(),       //   REPORT_COUNT (5)
            0x75.toByte(), 0x01.toByte(),       //   REPORT_SIZE (1)
            0x05.toByte(), 0x08.toByte(),       //   USAGE_PAGE (LEDs)
            0x19.toByte(), 0x01.toByte(),       //   USAGE_MINIMUM (Num Lock)
            0x29.toByte(), 0x05.toByte(),       //   USAGE_MAXIMUM (Kana)
            0x91.toByte(), 0x02.toByte(),       //   OUTPUT (Data,Var,Abs) - LEDs
            0x95.toByte(), 0x01.toByte(),       //   REPORT_COUNT (1)
            0x75.toByte(), 0x03.toByte(),       //   REPORT_SIZE (3)
            0x91.toByte(), 0x01.toByte(),       //   OUTPUT (Cnst,Ary,Abs) - LED padding
            0x95.toByte(), 0x06.toByte(),       //   REPORT_COUNT (6)
            0x75.toByte(), 0x08.toByte(),       //   REPORT_SIZE (8)
            0x15.toByte(), 0x00.toByte(),       //   LOGICAL_MINIMUM (0)
            0x25.toByte(), 0x65.toByte(),       //   LOGICAL_MAXIMUM (101)
            0x05.toByte(), 0x07.toByte(),       //   USAGE_PAGE (Keyboard/Keypad)
            0x19.toByte(), 0x00.toByte(),       //   USAGE_MINIMUM (0)
            0x29.toByte(), 0x65.toByte(),       //   USAGE_MAXIMUM (101)
            0x81.toByte(), 0x00.toByte(),       //   INPUT (Data,Ary,Abs) - 6 keycodes
            0xC0.toByte()                       // END_COLLECTION
        )
    }

    private fun log(message: String, isError: Boolean = false) {
        val entry = LogEntry(
            timestamp = timeFormat.format(Date()),
            message = message,
            isError = isError
        )
        _state.update { curr ->
            val updatedLogs = (listOf(entry) + curr.logs).take(100)
            curr.copy(logs = updatedLogs)
        }
    }

    private val profileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                hidDevice = proxy as? BluetoothHidDevice
                log("Đã kết nối Bluetooth HID Profile Proxy thành công")
                registerHidApp()
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                hidDevice = null
                isAppRegistered = false
                log("Bluetooth HID Profile Proxy bị ngắt kết nối", isError = true)
                _state.update {
                    it.copy(
                        status = ConnectionStatus.DISCONNECTED,
                        statusMessage = "Mất profile HID",
                        targetName = null
                    )
                }
            }
        }
    }

    private val hidCallback = object : BluetoothHidDevice.Callback() {
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            isAppRegistered = registered
            log("onAppStatusChanged: registered = $registered, device = ${pluggedDevice?.name ?: "none"}")
            _state.update {
                it.copy(
                    status = if (registered) ConnectionStatus.REGISTERED else ConnectionStatus.FAILED,
                    statusMessage = if (registered) "Đã đăng ký HID. Sẵn sàng pair!" else "Đăng ký HID thất bại"
                )
            }
            if (registered && pluggedDevice != null) {
                connect(pluggedDevice)
            }
        }

        override fun onConnectionStateChanged(device: BluetoothDevice, state: Int) {
            val devName = try { device.name ?: device.address } catch (e: SecurityException) { device.address }
            when (state) {
                BluetoothProfile.STATE_CONNECTED -> {
                    connectedDevice = device
                    log("Đã kết nối với host: $devName ($state)")
                    _state.update {
                        it.copy(
                            status = ConnectionStatus.CONNECTED,
                            statusMessage = "Đã kết nối: $devName",
                            targetName = devName,
                            targetAddress = device.address
                        )
                    }
                }
                BluetoothProfile.STATE_CONNECTING -> {
                    log("Đang kết nối tới $devName...")
                    _state.update {
                        it.copy(
                            status = ConnectionStatus.CONNECTING,
                            statusMessage = "Đang kết nối: $devName"
                        )
                    }
                }
                BluetoothProfile.STATE_DISCONNECTING -> {
                    log("Đang ngắt kết nối với $devName...")
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    log("Đã ngắt kết nối với $devName")
                    if (connectedDevice?.address == device.address) {
                        connectedDevice = null
                    }
                    _state.update {
                        it.copy(
                            status = if (isAppRegistered) ConnectionStatus.REGISTERED else ConnectionStatus.DISCONNECTED,
                            statusMessage = if (isAppRegistered) "Sẵn sàng kết nối host" else "Chưa kết nối",
                            targetName = null,
                            targetAddress = null
                        )
                    }
                }
            }
        }

        override fun onGetReport(device: BluetoothDevice?, type: Byte, id: Byte, bufferSize: Int) {
            log("onGetReport: type=$type, id=$id, bufferSize=$bufferSize")
            device?.let {
                try {
                    hidDevice?.replyReport(it, type, id, ByteArray(8))
                } catch (e: Exception) {
                    log("replyReport error: ${e.message}", isError = true)
                }
            }
        }

        override fun onSetReport(device: BluetoothDevice?, type: Byte, id: Byte, data: ByteArray?) {
            device?.let {
                try {
                    hidDevice?.reportError(it, BluetoothHidDevice.ERROR_RSP_SUCCESS)
                } catch (e: Exception) {
                    log("reportError error: ${e.message}", isError = true)
                }
            }
        }

        override fun onSetProtocol(device: BluetoothDevice?, protocol: Byte) {
            device?.let {
                try {
                    hidDevice?.reportError(it, BluetoothHidDevice.ERROR_RSP_SUCCESS)
                } catch (e: Exception) {
                    log("onSetProtocol error: ${e.message}", isError = true)
                }
            }
        }

        override fun onVirtualCableUnplug(device: BluetoothDevice?) {
            log("onVirtualCableUnplug nhận từ host")
            connectedDevice = null
            _state.update {
                it.copy(
                    status = ConnectionStatus.REGISTERED,
                    statusMessage = "Cáp ảo đã rút (Host ngắt)",
                    targetName = null
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun initialize() {
        if (bluetoothAdapter == null) {
            log("Thiết bị không hỗ trợ Bluetooth!", isError = true)
            _state.update { it.copy(status = ConnectionStatus.FAILED, statusMessage = "Không có Bluetooth") }
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            log("Bluetooth đang tắt. Vui lòng bật Bluetooth!", isError = true)
            _state.update { it.copy(status = ConnectionStatus.FAILED, statusMessage = "Bluetooth đang tắt") }
            return
        }

        log("Đang yêu cầu Bluetooth HID Device Profile Proxy...")
        _state.update { it.copy(status = ConnectionStatus.REGISTERING, statusMessage = "Đang khởi tạo HID Proxy...") }

        try {
            bluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.HID_DEVICE)
        } catch (e: Exception) {
            log("Lỗi khi lấy ProfileProxy: ${e.message}", isError = true)
            _state.update { it.copy(status = ConnectionStatus.FAILED, statusMessage = "Lỗi getProfileProxy: ${e.message}") }
        }
    }

    @SuppressLint("MissingPermission")
    fun registerHidApp() {
        val hid = hidDevice ?: run {
            log("hidDevice proxy rỗng, không thể đăng ký!", isError = true)
            return
        }

        val sdp = BluetoothHidDeviceAppSdpSettings(
            "Kaius Keyboard",
            "Kaius Bluetooth HID Keyboard",
            "Kaius Inc",
            SUBCLASS_KEYBOARD,
            KEYBOARD_REPORT_DESCRIPTOR
        )

        val qos = BluetoothHidDeviceAppQosSettings(
            BluetoothHidDeviceAppQosSettings.SERVICE_BEST_EFFORT,
            800,
            9,
            0,
            11250,
            BluetoothHidDeviceAppQosSettings.MAX
        )

        log("Bắt đầu đăng ký HID App (registerApp)...")
        _state.update { it.copy(status = ConnectionStatus.REGISTERING, statusMessage = "Đang đăng ký HID App...") }

        try {
            val success = hid.registerApp(sdp, qos, qos, executor, hidCallback)
            if (!success) {
                log("registerApp trả về FALSE! MIUI có thể đang chặn hoặc đã có app đăng ký.", isError = true)
                _state.update { it.copy(status = ConnectionStatus.FAILED, statusMessage = "registerApp trả về FALSE") }
            } else {
                log("registerApp gửi thành công, đang chờ callback...")
            }
        } catch (e: Exception) {
            log("Exception khi gọi registerApp: ${e.message}", isError = true)
            _state.update { it.copy(status = ConnectionStatus.FAILED, statusMessage = "Exception: ${e.message}") }
        }
    }

    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice) {
        val hid = hidDevice ?: return
        log("Thực hiện kết nối tới host: ${device.name ?: device.address}")
        try {
            hid.connect(device)
        } catch (e: Exception) {
            log("Lỗi connect: ${e.message}", isError = true)
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        val hid = hidDevice ?: return
        val target = connectedDevice ?: return
        log("Ngắt kết nối với host: ${target.name ?: target.address}")
        try {
            hid.disconnect(target)
        } catch (e: Exception) {
            log("Lỗi disconnect: ${e.message}", isError = true)
        }
    }

    @SuppressLint("MissingPermission")
    override fun sendRawReport(modifiers: Byte, keyCodes: ByteArray) {
        val target = connectedDevice
        if (target == null) {
            log("Chưa kết nối Host. Không thể gửi phím!", isError = true)
            return
        }
        val hid = hidDevice
        if (hid == null) {
            log("hidDevice rỗng, không thể gửi!", isError = true)
            return
        }

        val report = ByteArray(8)
        report[0] = modifiers
        report[1] = 0x00 // reserved
        for (i in 0 until minOf(6, keyCodes.size)) {
            report[2 + i] = keyCodes[i]
        }

        try {
            val success = hid.sendReport(target, REPORT_ID_KEYBOARD.toInt(), report)
            if (!success) {
                log("sendReport thất bại!", isError = true)
            }
        } catch (e: Exception) {
            log("Lỗi sendReport: ${e.message}", isError = true)
        }
    }

    override fun sendKeyDown(keyCode: Byte, modifiers: Byte) {
        sendRawReport(modifiers, byteArrayOf(keyCode))
    }

    override fun sendKeyUp(keyCode: Byte, modifiers: Byte) {
        // Empty keys array, keeping current active modifiers if any
        sendRawReport(modifiers, byteArrayOf())
    }

    override fun sendKeyTap(keyCode: Byte, modifiers: Byte) {
        scope.launch(Dispatchers.IO) {
            sendKeyDown(keyCode, modifiers)
            delay(30)
            sendKeyUp(keyCode, modifiers)
        }
    }

    @SuppressLint("MissingPermission")
    override fun release() {
        try {
            if (isAppRegistered) {
                hidDevice?.unregisterApp()
                isAppRegistered = false
            }
            if (hidDevice != null && bluetoothAdapter != null) {
                bluetoothAdapter.closeProfileProxy(BluetoothProfile.HID_DEVICE, hidDevice)
                hidDevice = null
            }
            executor.shutdown()
        } catch (e: Exception) {
            log("Lỗi release: ${e.message}", isError = true)
        }
    }

    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<BluetoothDevice> {
        return try {
            bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
