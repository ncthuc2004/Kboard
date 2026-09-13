package com.kaius.keyboard.engine

import com.kaius.keyboard.transport.TransportManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InputEngine(
    private val transportManager: TransportManager,
    private val scope: CoroutineScope
) {
    // Current bitmask of active modifiers
    private val _modifiers = MutableStateFlow<Byte>(HidKeyCodes.MOD_NONE)
    val modifiers: StateFlow<Byte> = _modifiers.asStateFlow()

    // Sticky modifier mode (toggle on/off)
    fun toggleModifier(modMask: Byte) {
        _modifiers.update { current ->
            if ((current.toInt() and modMask.toInt()) != 0) {
                (current.toInt() and modMask.toInt().inv()).toByte()
            } else {
                (current.toInt() or modMask.toInt()).toByte()
            }
        }
    }

    fun isModifierActive(modMask: Byte): Boolean {
        return (_modifiers.value.toInt() and modMask.toInt()) != 0
    }

    fun clearModifiers() {
        _modifiers.value = HidKeyCodes.MOD_NONE
    }

    fun onKeyDown(keyCode: Byte) {
        transportManager.sendKeyDown(keyCode, _modifiers.value)
    }

    fun onKeyUp(keyCode: Byte) {
        transportManager.sendKeyUp(keyCode, _modifiers.value)
    }

    fun onKeyTap(keyCode: Byte) {
        val currentMod = _modifiers.value
        transportManager.sendKeyTap(keyCode, currentMod)
    }

    fun sendMacro(modMask: Byte, keyCode: Byte) {
        scope.launch(Dispatchers.IO) {
            transportManager.sendKeyDown(keyCode, modMask)
            delay(35)
            transportManager.sendKeyUp(keyCode, modMask)
            delay(10)
            transportManager.sendRawReport(HidKeyCodes.MOD_NONE, byteArrayOf())
        }
    }
}
