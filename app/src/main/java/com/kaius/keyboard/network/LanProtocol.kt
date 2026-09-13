package com.kaius.keyboard.network

object LanProtocol {
    const val PORT = 8964
    const val BROADCAST_IP = "255.255.255.255"

    // Action types
    const val ACTION_DISCOVER = "discover"
    const val ACTION_ANNOUNCE = "announce"
    const val ACTION_KEY_DOWN = "down"
    const val ACTION_KEY_UP = "up"
    const val ACTION_KEY_TAP = "tap"
    const val ACTION_PING = "ping"
}

data class DiscoveredReceiver(
    val name: String,
    val ip: String,
    val port: Int = LanProtocol.PORT,
    val lastSeenTimestamp: Long = System.currentTimeMillis()
)
