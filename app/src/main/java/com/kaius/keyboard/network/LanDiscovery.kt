package com.kaius.keyboard.network

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
import java.net.InterfaceAddress
import java.net.NetworkInterface

class LanDiscovery(private val scope: CoroutineScope) {

    private val _discoveredReceivers = MutableStateFlow<List<DiscoveredReceiver>>(emptyList())
    val discoveredReceivers: StateFlow<List<DiscoveredReceiver>> = _discoveredReceivers.asStateFlow()

    private var discoveryJob: Job? = null
    private var socket: DatagramSocket? = null

    fun startDiscovery() {
        if (discoveryJob?.isActive == true) return

        discoveryJob = scope.launch(Dispatchers.IO) {
            try {
                val sock = DatagramSocket()
                sock.broadcast = true
                socket = sock

                // Send discovery probe
                sendProbe(sock)

                val buffer = ByteArray(1024)

                // Listen for announcement responses
                while (isActive && !sock.isClosed) {
                    val packet = DatagramPacket(buffer, buffer.size)
                    sock.receive(packet)

                    val jsonStr = String(packet.data, 0, packet.length, Charsets.UTF_8).trim()
                    try {
                        val json = JSONObject(jsonStr)
                        if (json.optString("a") == LanProtocol.ACTION_ANNOUNCE) {
                            val name = json.optString("name", "Unknown Receiver")
                            val ip = packet.address.hostAddress ?: json.optString("ip", "")
                            val port = json.optInt("port", LanProtocol.PORT)

                            val receiver = DiscoveredReceiver(
                                name = name,
                                ip = ip,
                                port = port
                            )

                            _discoveredReceivers.update { curr ->
                                val filtered = curr.filterNot { it.ip == ip }
                                (filtered + receiver)
                            }
                        }
                    } catch (_: Exception) {}
                }
            } catch (_: Exception) {}
        }

        // Periodic probe broadcast while discovering
        scope.launch(Dispatchers.IO) {
            while (isActive && discoveryJob?.isActive == true) {
                delay(3000)
                socket?.let { sendProbe(it) }
            }
        }
    }

    private fun sendProbe(sock: DatagramSocket) {
        try {
            val json = JSONObject().apply {
                put("a", LanProtocol.ACTION_DISCOVER)
            }
            val data = json.toString().toByteArray(Charsets.UTF_8)

            // Broadcast to 255.255.255.255
            val globalBroadcast = InetAddress.getByName(LanProtocol.BROADCAST_IP)
            sock.send(DatagramPacket(data, data.size, globalBroadcast, LanProtocol.PORT))

            // Also broadcast on each interface's specific broadcast address
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                if (iface.isLoopback || !iface.isUp) continue
                for (interfaceAddress in iface.interfaceAddresses) {
                    val broadcast = interfaceAddress.broadcast
                    if (broadcast != null) {
                        try {
                            sock.send(DatagramPacket(data, data.size, broadcast, LanProtocol.PORT))
                        } catch (_: Exception) {}
                    }
                }
            }
        } catch (_: Exception) {}
    }

    fun stopDiscovery() {
        discoveryJob?.cancel()
        discoveryJob = null
        try {
            socket?.close()
            socket = null
        } catch (_: Exception) {}
    }
}
