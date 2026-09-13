package com.kaius.keyboard.transport.wifi

import com.kaius.keyboard.transport.ConnectionStatus
import com.kaius.keyboard.transport.InputTransport
import com.kaius.keyboard.transport.LogEntry
import com.kaius.keyboard.transport.TransportMode
import com.kaius.keyboard.transport.TransportState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WifiLanTransport(
    private val scope: CoroutineScope
) : InputTransport {

    private var socket: DatagramSocket? = null
    private var targetIp: String = "192.168.43.1" // Default hotspot gateway of Android
    private var targetPort: Int = 8964
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    private val _state = MutableStateFlow(
        TransportState(
            mode = TransportMode.WIFI_LAN,
            status = ConnectionStatus.DISCONNECTED,
            statusMessage = "Chưa kết nối Wi-Fi LAN",
            wifiTargetIp = targetIp,
            wifiTargetPort = targetPort
        )
    )
    override val state: StateFlow<TransportState> = _state.asStateFlow()

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

    fun updateTarget(ip: String, port: Int) {
        targetIp = ip
        targetPort = port
        _state.update {
            it.copy(
                wifiTargetIp = ip,
                wifiTargetPort = port,
                targetAddress = "$ip:$port",
                statusMessage = "Đích: $ip:$port"
            )
        }
        log("Đã cập nhật địa chỉ đích Wi-Fi: $ip:$port")
    }

    override fun initialize() {
        scope.launch(Dispatchers.IO) {
            try {
                if (socket == null || socket?.isClosed == true) {
                    socket = DatagramSocket()
                    socket?.broadcast = true
                }
                log("Đã mở UDP Socket thành công trên cổng nội bộ ${socket?.localPort}")
                _state.update {
                    it.copy(
                        status = ConnectionStatus.CONNECTED,
                        statusMessage = "Wi-Fi LAN sẵn sàng -> $targetIp:$targetPort",
                        targetName = "Host $targetIp",
                        targetAddress = "$targetIp:$targetPort"
                    )
                }
            } catch (e: Exception) {
                log("Lỗi mở UDP socket: ${e.message}", isError = true)
                _state.update {
                    it.copy(
                        status = ConnectionStatus.FAILED,
                        statusMessage = "Lỗi socket: ${e.message}"
                    )
                }
            }
        }
    }

    private fun sendJsonPacket(json: String) {
        scope.launch(Dispatchers.IO) {
            try {
                val sock = socket ?: run {
                    val s = DatagramSocket()
                    socket = s
                    s
                }
                val data = json.toByteArray(Charsets.UTF_8)
                val address = InetAddress.getByName(targetIp)
                val packet = DatagramPacket(data, data.size, address, targetPort)
                sock.send(packet)
            } catch (e: Exception) {
                log("Lỗi gửi gói UDP: ${e.message}", isError = true)
            }
        }
    }

    override fun sendKeyDown(keyCode: Byte, modifiers: Byte) {
        val json = """{"a":"down","k":${keyCode.toInt() and 0xFF},"m":${modifiers.toInt() and 0xFF}}"""
        sendJsonPacket(json)
    }

    override fun sendKeyUp(keyCode: Byte, modifiers: Byte) {
        val json = """{"a":"up","k":${keyCode.toInt() and 0xFF},"m":${modifiers.toInt() and 0xFF}}"""
        sendJsonPacket(json)
    }

    override fun sendKeyTap(keyCode: Byte, modifiers: Byte) {
        val json = """{"a":"tap","k":${keyCode.toInt() and 0xFF},"m":${modifiers.toInt() and 0xFF}}"""
        sendJsonPacket(json)
    }

    override fun sendRawReport(modifiers: Byte, keyCodes: ByteArray) {
        val keysString = keyCodes.joinToString(",") { (it.toInt() and 0xFF).toString() }
        val json = """{"a":"report","m":${modifiers.toInt() and 0xFF},"keys":[$keysString]}"""
        sendJsonPacket(json)
    }

    fun sendPing() {
        val now = System.currentTimeMillis()
        val json = """{"a":"ping","t":$now}"""
        sendJsonPacket(json)
        log("Đã gửi gói Ping tới $targetIp:$targetPort")
    }

    override fun release() {
        try {
            socket?.close()
            socket = null
            log("Đã đóng socket Wi-Fi LAN")
        } catch (e: Exception) {
            log("Lỗi release socket: ${e.message}", isError = true)
        }
    }
}
