package com.kaius.keyboard.network

import android.os.Build
import com.kaius.keyboard.engine.HidKeyCodes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ReceivedKeyEvent(
    val id: Long = System.nanoTime(),
    val timestamp: String,
    val action: String,
    val keyCode: Byte,
    val keyName: String,
    val modifiers: Byte,
    val senderIp: String
)

data class ReceiverState(
    val isRunning: Boolean = false,
    val localIp: String = "0.0.0.0",
    val port: Int = LanProtocol.PORT,
    val deviceName: String = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
    val lastEvent: ReceivedKeyEvent? = null,
    val activeModifiers: Byte = 0,
    val activeKeys: Set<Byte> = emptySet(),
    val eventHistory: List<ReceivedKeyEvent> = emptyList(),
    val accumulatedText: String = "",
    val connectedSenders: Set<String> = emptySet(),
    val totalPacketsReceived: Long = 0
)

class LanServer(private val scope: CoroutineScope) {

    private var socket: DatagramSocket? = null
    private var listenJob: Job? = null
    private var refreshIpJob: Job? = null
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    private val _state = MutableStateFlow(ReceiverState())
    val state: StateFlow<ReceiverState> = _state.asStateFlow()

    fun start() {
        if (_state.value.isRunning) return

        val localIp = getLocalIpAddress()
        _state.update {
            it.copy(
                isRunning = true,
                localIp = localIp
            )
        }

        // Periodically refresh IP so that if hotspot or Wi-Fi is enabled after launch, IP updates automatically
        refreshIpJob?.cancel()
        refreshIpJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(2500L)
                val currentIp = getLocalIpAddress()
                if (currentIp != "127.0.0.1" && currentIp != _state.value.localIp) {
                    _state.update { it.copy(localIp = currentIp) }
                }
            }
        }

        listenJob = scope.launch(Dispatchers.IO) {
            try {
                // Must configure reuseAddress BEFORE binding to prevent BindException with KaiusImeService!
                val sock = DatagramSocket(null).apply {
                    reuseAddress = true
                    bind(InetSocketAddress(LanProtocol.PORT))
                }
                socket = sock

                val buffer = ByteArray(2048)

                while (isActive && !sock.isClosed) {
                    val packet = DatagramPacket(buffer, buffer.size)
                    sock.receive(packet)

                    val rawMsg = String(packet.data, 0, packet.length, Charsets.UTF_8).trim()
                    val senderIp = packet.address.hostAddress ?: "unknown"

                    handleIncomingPacket(rawMsg, packet.address, packet.port, senderIp, sock)
                }
            } catch (e: Exception) {
                if (isActive) {
                    _state.update { it.copy(isRunning = false) }
                }
            }
        }
    }

    private fun handleIncomingPacket(
        jsonStr: String,
        senderAddr: InetAddress,
        senderPort: Int,
        senderIp: String,
        sock: DatagramSocket
    ) {
        try {
            val json = JSONObject(jsonStr)
            val action = json.optString("a", "")

            when (action) {
                LanProtocol.ACTION_DISCOVER -> {
                    // Respond with Announce
                    val announceJson = JSONObject().apply {
                        put("a", LanProtocol.ACTION_ANNOUNCE)
                        put("name", _state.value.deviceName)
                        put("ip", _state.value.localIp)
                        put("port", LanProtocol.PORT)
                    }
                    val data = announceJson.toString().toByteArray(Charsets.UTF_8)
                    val responsePacket = DatagramPacket(data, data.size, senderAddr, senderPort)
                    sock.send(responsePacket)
                }

                LanProtocol.ACTION_PING -> {
                    _state.update { curr ->
                        curr.copy(
                            connectedSenders = curr.connectedSenders + senderIp,
                            totalPacketsReceived = curr.totalPacketsReceived + 1
                        )
                    }
                }

                LanProtocol.ACTION_SET_TELEX -> {
                    val enabled = json.optInt("tx", 1) == 1 || json.optBoolean("enabled", true)
                    LanBridge.isTelexEnabled = enabled
                    LanBridge.onTelexChanged?.invoke(enabled)
                }

                LanProtocol.ACTION_KEY_DOWN, LanProtocol.ACTION_KEY_UP, LanProtocol.ACTION_KEY_TAP -> {
                    if (json.has("tx")) {
                        val enabled = json.optInt("tx", 1) == 1
                        if (LanBridge.isTelexEnabled != enabled) {
                            LanBridge.isTelexEnabled = enabled
                            LanBridge.onTelexChanged?.invoke(enabled)
                        }
                    }

                    val code = json.optInt("k", 0).toByte()
                    val mod = json.optInt("m", 0).toByte()
                    val keyName = if (code != HidKeyCodes.KEY_NONE) getKeyName(code) else getModifierName(mod)

                    val event = ReceivedKeyEvent(
                        timestamp = timeFormat.format(Date()),
                        action = action,
                        keyCode = code,
                        keyName = keyName,
                        modifiers = mod,
                        senderIp = senderIp
                    )

                    LanBridge.onKeyReceived?.invoke(action, code, mod)

                    _state.update { curr ->
                        val updatedKeys = when (action) {
                            LanProtocol.ACTION_KEY_DOWN -> if (code != HidKeyCodes.KEY_NONE) curr.activeKeys + code else curr.activeKeys
                            LanProtocol.ACTION_KEY_UP -> if (code != HidKeyCodes.KEY_NONE) curr.activeKeys - code else curr.activeKeys
                            else -> curr.activeKeys
                        }

                        // Update text accumulator for tap or down
                        val newText = if (action == LanProtocol.ACTION_KEY_DOWN || action == LanProtocol.ACTION_KEY_TAP) {
                            formatAccumulatedText(curr.accumulatedText, code, mod)
                        } else {
                            curr.accumulatedText
                        }

                        curr.copy(
                            lastEvent = event,
                            activeModifiers = mod,
                            activeKeys = updatedKeys,
                            eventHistory = (listOf(event) + curr.eventHistory).take(60),
                            accumulatedText = newText,
                            connectedSenders = curr.connectedSenders + senderIp,
                            totalPacketsReceived = curr.totalPacketsReceived + 1
                        )
                    }
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun getModifierName(mod: Byte): String {
        val parts = mutableListOf<String>()
        val m = mod.toInt()
        if ((m and (HidKeyCodes.MOD_LEFT_CTRL.toInt() or HidKeyCodes.MOD_RIGHT_CTRL.toInt())) != 0) parts.add("Ctrl")
        if ((m and (HidKeyCodes.MOD_LEFT_SHIFT.toInt() or HidKeyCodes.MOD_RIGHT_SHIFT.toInt())) != 0) parts.add("Shift")
        if ((m and (HidKeyCodes.MOD_LEFT_ALT.toInt() or HidKeyCodes.MOD_RIGHT_ALT.toInt())) != 0) parts.add("Alt")
        if ((m and (HidKeyCodes.MOD_LEFT_GUI.toInt() or HidKeyCodes.MOD_RIGHT_GUI.toInt())) != 0) parts.add("Win")
        return if (parts.isEmpty()) "NONE" else parts.joinToString("+")
    }

    private fun formatAccumulatedText(currentText: String, code: Byte, mod: Byte): String {
        val isCtrl = (mod.toInt() and (HidKeyCodes.MOD_LEFT_CTRL.toInt() or HidKeyCodes.MOD_RIGHT_CTRL.toInt())) != 0
        val isAlt = (mod.toInt() and (HidKeyCodes.MOD_LEFT_ALT.toInt() or HidKeyCodes.MOD_RIGHT_ALT.toInt())) != 0
        val isGui = (mod.toInt() and (HidKeyCodes.MOD_LEFT_GUI.toInt() or HidKeyCodes.MOD_RIGHT_GUI.toInt())) != 0
        if (isCtrl || isAlt || isGui) {
            return currentText // Do not accumulate text on modifier shortcuts
        }

        val isShift = (mod.toInt() and HidKeyCodes.MOD_LEFT_SHIFT.toInt() != 0) ||
                (mod.toInt() and HidKeyCodes.MOD_RIGHT_SHIFT.toInt() != 0)

        return when (code) {
            HidKeyCodes.KEY_BACKSPACE -> if (currentText.isNotEmpty()) currentText.dropLast(1) else ""
            HidKeyCodes.KEY_SPACE -> "$currentText "
            HidKeyCodes.KEY_ENTER -> "$currentText\n"
            in HidKeyCodes.KEY_A..HidKeyCodes.KEY_Z -> {
                val charOffset = code - HidKeyCodes.KEY_A
                val char = ('a'.code + charOffset).toChar()
                currentText + if (isShift) char.uppercaseChar() else char
            }
            in HidKeyCodes.KEY_1..HidKeyCodes.KEY_9 -> {
                val num = ('1'.code + (code - HidKeyCodes.KEY_1)).toChar()
                currentText + num
            }
            HidKeyCodes.KEY_0 -> currentText + "0"
            else -> currentText
        }
    }

    fun clearAccumulatedText() {
        _state.update { it.copy(accumulatedText = "") }
    }

    fun clearHistory() {
        _state.update { it.copy(eventHistory = emptyList(), totalPacketsReceived = 0) }
    }

    fun stop() {
        listenJob?.cancel()
        listenJob = null
        refreshIpJob?.cancel()
        refreshIpJob = null
        try {
            socket?.close()
            socket = null
        } catch (_: Exception) {}
        _state.update { it.copy(isRunning = false) }
    }

    private fun getKeyName(code: Byte): String {
        return when (code) {
            HidKeyCodes.KEY_NONE -> "NONE"
            HidKeyCodes.KEY_A -> "A"
            HidKeyCodes.KEY_B -> "B"
            HidKeyCodes.KEY_C -> "C"
            HidKeyCodes.KEY_D -> "D"
            HidKeyCodes.KEY_E -> "E"
            HidKeyCodes.KEY_F -> "F"
            HidKeyCodes.KEY_G -> "G"
            HidKeyCodes.KEY_H -> "H"
            HidKeyCodes.KEY_I -> "I"
            HidKeyCodes.KEY_J -> "J"
            HidKeyCodes.KEY_K -> "K"
            HidKeyCodes.KEY_L -> "L"
            HidKeyCodes.KEY_M -> "M"
            HidKeyCodes.KEY_N -> "N"
            HidKeyCodes.KEY_O -> "O"
            HidKeyCodes.KEY_P -> "P"
            HidKeyCodes.KEY_Q -> "Q"
            HidKeyCodes.KEY_R -> "R"
            HidKeyCodes.KEY_S -> "S"
            HidKeyCodes.KEY_T -> "T"
            HidKeyCodes.KEY_U -> "U"
            HidKeyCodes.KEY_V -> "V"
            HidKeyCodes.KEY_W -> "W"
            HidKeyCodes.KEY_X -> "X"
            HidKeyCodes.KEY_Y -> "Y"
            HidKeyCodes.KEY_Z -> "Z"
            HidKeyCodes.KEY_1 -> "1"
            HidKeyCodes.KEY_2 -> "2"
            HidKeyCodes.KEY_3 -> "3"
            HidKeyCodes.KEY_4 -> "4"
            HidKeyCodes.KEY_5 -> "5"
            HidKeyCodes.KEY_6 -> "6"
            HidKeyCodes.KEY_7 -> "7"
            HidKeyCodes.KEY_8 -> "8"
            HidKeyCodes.KEY_9 -> "9"
            HidKeyCodes.KEY_0 -> "0"
            HidKeyCodes.KEY_ENTER -> "ENTER"
            HidKeyCodes.KEY_ESC -> "ESC"
            HidKeyCodes.KEY_BACKSPACE -> "BACKSPACE"
            HidKeyCodes.KEY_TAB -> "TAB"
            HidKeyCodes.KEY_SPACE -> "SPACE"
            HidKeyCodes.KEY_UP_ARROW -> "▲ UP"
            HidKeyCodes.KEY_DOWN_ARROW -> "▼ DOWN"
            HidKeyCodes.KEY_LEFT_ARROW -> "◄ LEFT"
            HidKeyCodes.KEY_RIGHT_ARROW -> "► RIGHT"
            else -> "KEY(0x${(code.toInt() and 0xFF).toString(16).uppercase()})"
        }
    }

    fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return "127.0.0.1"
            val ipList = mutableListOf<Pair<String, String>>() // (ifaceName, ip)

            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                if (iface.isLoopback || !iface.isUp) continue

                val addresses = iface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    val ip = addr.hostAddress
                    if (!addr.isLoopbackAddress && ip != null && !ip.contains(":")) {
                        ipList.add(Pair(iface.name.lowercase(Locale.ROOT), ip))
                    }
                }
            }

            // Priority 1: Exact Android Hotspot Host IP
            ipList.find { it.second == "192.168.43.1" }?.let { return it.second }

            // Priority 2: Hotspot/AP interfaces (ap*, softap*, swlan*, rndis*)
            ipList.find { (name, _) ->
                name.startsWith("ap") || name.startsWith("softap") || name.startsWith("swlan") || name.startsWith("rndis")
            }?.let { return it.second }

            // Priority 3: Standard Wi-Fi interfaces (wlan*)
            ipList.find { (name, _) -> name.startsWith("wlan") }?.let { return it.second }

            // Priority 4: Private LAN IPs that are NOT cellular carrier modems (rmnet*, ccmni*, pdp*, dummy*, wwan*)
            ipList.find { (name, _) ->
                !name.startsWith("rmnet") && !name.startsWith("ccmni") && !name.startsWith("pdp") && !name.startsWith("dummy") && !name.startsWith("wwan")
            }?.let { return it.second }

            // Fallback: First non-loopback IPv4
            if (ipList.isNotEmpty()) {
                return ipList.first().second
            }
        } catch (_: Exception) {}
        return "127.0.0.1"
    }
}
