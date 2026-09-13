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
import java.net.NetworkInterface

class LanDiscovery(private val scope: CoroutineScope) {

    companion object {
        const val RECEIVER_TTL_MS = 6000L // Purge receivers not seen within 6s (~2 probe cycles)
        const val PROBE_INTERVAL_MS = 2500L
        const val CLEANUP_INTERVAL_MS = 2000L
    }

    private val _discoveredReceivers = MutableStateFlow<List<DiscoveredReceiver>>(emptyList())
    val discoveredReceivers: StateFlow<List<DiscoveredReceiver>> = _discoveredReceivers.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private var listenJob: Job? = null
    private var probeJob: Job? = null
    private var cleanupJob: Job? = null
    private var scanAnimJob: Job? = null
    private var socket: DatagramSocket? = null

    fun startDiscovery() {
        if (listenJob?.isActive == true) return

        listenJob = scope.launch(Dispatchers.IO) {
            try {
                val sock = DatagramSocket().apply {
                    broadcast = true
                    reuseAddress = true
                }
                socket = sock

                // Initial probe
                sendProbe(sock)

                val buffer = ByteArray(2048)

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

                            if (ip.isNotEmpty()) {
                                val receiver = DiscoveredReceiver(
                                    name = name,
                                    ip = ip,
                                    port = port,
                                    lastSeenTimestamp = System.currentTimeMillis()
                                )

                                _discoveredReceivers.update { curr ->
                                    val filtered = curr.filterNot { it.ip == ip }
                                    filtered + receiver
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }
            } catch (_: Exception) {
            } finally {
                try {
                    socket?.close()
                } catch (_: Exception) {}
                socket = null
            }
        }

        // Periodic probe broadcast while discovering
        probeJob?.cancel()
        probeJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(PROBE_INTERVAL_MS)
                socket?.let { sendProbe(it) }
            }
        }

        // Periodic eviction loop: purge receivers not heard from within RECEIVER_TTL_MS
        cleanupJob?.cancel()
        cleanupJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(CLEANUP_INTERVAL_MS)
                val now = System.currentTimeMillis()
                _discoveredReceivers.update { curr ->
                    curr.filter { (now - it.lastSeenTimestamp) < RECEIVER_TTL_MS }
                }
            }
        }
    }

    /**
     * Clear stale/dead receivers immediately and trigger a fresh broadcast discovery probe.
     */
    fun refreshScan() {
        // 1. Immediately wipe the list so offline devices vanish from the UI
        _discoveredReceivers.value = emptyList()

        // 2. Trigger scan pulse for UI feedback
        scanAnimJob?.cancel()
        scanAnimJob = scope.launch {
            _isScanning.value = true
            delay(1200L)
            _isScanning.value = false
        }

        // 3. Ensure listener is active and fire broadcast probe immediately
        if (listenJob?.isActive != true || socket == null || socket?.isClosed == true) {
            stopDiscovery()
            startDiscovery()
        } else {
            scope.launch(Dispatchers.IO) {
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

            // 1. Broadcast to 255.255.255.255
            try {
                val globalBroadcast = InetAddress.getByName(LanProtocol.BROADCAST_IP)
                sock.send(DatagramPacket(data, data.size, globalBroadcast, LanProtocol.PORT))
            } catch (_: Exception) {}

            // 2. Broadcast on each interface's specific broadcast address
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return
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
        listenJob?.cancel()
        listenJob = null
        probeJob?.cancel()
        probeJob = null
        cleanupJob?.cancel()
        cleanupJob = null
        scanAnimJob?.cancel()
        _isScanning.value = false
        try {
            socket?.close()
            socket = null
        } catch (_: Exception) {}
    }
}

