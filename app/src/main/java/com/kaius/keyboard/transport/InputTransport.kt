package com.kaius.keyboard.transport

import kotlinx.coroutines.flow.StateFlow

interface InputTransport {
    val state: StateFlow<TransportState>

    fun initialize()
    fun release()

    fun sendKeyDown(keyCode: Byte, modifiers: Byte)
    fun sendKeyUp(keyCode: Byte, modifiers: Byte)
    fun sendKeyTap(keyCode: Byte, modifiers: Byte)
    fun sendRawReport(modifiers: Byte, keyCodes: ByteArray)
}
