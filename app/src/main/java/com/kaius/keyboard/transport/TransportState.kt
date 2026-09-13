package com.kaius.keyboard.transport

enum class TransportMode {
    BLUETOOTH_HID,
    WIFI_LAN
}

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    REGISTERING,
    REGISTERED,
    FAILED
}

data class LogEntry(
    val id: Long = System.nanoTime(),
    val timestamp: String,
    val message: String,
    val isError: Boolean = false
)

data class TransportState(
    val mode: TransportMode = TransportMode.BLUETOOTH_HID,
    val status: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val statusMessage: String = "Khởi tạo...",
    val targetName: String? = null,
    val targetAddress: String? = null,
    val wifiTargetIp: String = "192.168.43.1",
    val wifiTargetPort: Int = 8964,
    val isScreenAwake: Boolean = true,
    val logs: List<LogEntry> = emptyList()
)
