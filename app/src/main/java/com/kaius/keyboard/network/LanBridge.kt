package com.kaius.keyboard.network

/**
 * Shared in-process bridge so that LanServer (running in UI/Background) and
 * KaiusImeService (running as system IME) can seamlessly exchange received keys
 * without socket bind collisions.
 */
object LanBridge {
    @Volatile
    var onKeyReceived: ((action: String, code: Byte, mod: Byte) -> Unit)? = null

    @Volatile
    var isTelexEnabled: Boolean = true

    @Volatile
    var onTelexChanged: ((Boolean) -> Unit)? = null
}
