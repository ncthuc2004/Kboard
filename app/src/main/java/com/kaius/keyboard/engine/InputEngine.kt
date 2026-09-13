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
        val next = if ((_modifiers.value.toInt() and modMask.toInt()) != 0) {
            (_modifiers.value.toInt() and modMask.toInt().inv()).toByte()
        } else {
            (_modifiers.value.toInt() or modMask.toInt()).toByte()
        }
        _modifiers.value = next

        scope.launch(Dispatchers.IO) {
            val isNowActive = (next.toInt() and modMask.toInt()) != 0
            if (isNowActive) {
                transportManager.sendKeyDown(HidKeyCodes.KEY_NONE, next)
            } else {
                transportManager.sendKeyUp(HidKeyCodes.KEY_NONE, next)
            }
            transportManager.sendRawReport(next, byteArrayOf())
        }
    }

    fun isModifierActive(modMask: Byte): Boolean {
        return (_modifiers.value.toInt() and modMask.toInt()) != 0
    }

    fun clearModifiers() {
        _modifiers.value = HidKeyCodes.MOD_NONE
        scope.launch(Dispatchers.IO) {
            transportManager.sendKeyUp(HidKeyCodes.KEY_NONE, HidKeyCodes.MOD_NONE)
            transportManager.sendRawReport(HidKeyCodes.MOD_NONE, byteArrayOf())
        }
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
            if (keyCode == HidKeyCodes.KEY_NONE) {
                // Modifier tap (e.g. Win key tap) - fast 15ms
                transportManager.sendKeyDown(HidKeyCodes.KEY_NONE, modMask)
                delay(15)
                transportManager.sendKeyUp(HidKeyCodes.KEY_NONE, HidKeyCodes.MOD_NONE)
            } else {
                // Step 1: Hold modifier down first (matches tapping Ctrl on keyboard)
                transportManager.sendKeyDown(HidKeyCodes.KEY_NONE, modMask)
                delay(20)
                // Step 2: Tap the key while modifier is held down
                transportManager.sendKeyDown(keyCode, modMask)
                delay(25)
                transportManager.sendKeyUp(keyCode, modMask)
                delay(15)
                // Step 3: Release the modifier
                transportManager.sendKeyUp(HidKeyCodes.KEY_NONE, HidKeyCodes.MOD_NONE)
                transportManager.sendRawReport(HidKeyCodes.MOD_NONE, byteArrayOf())
            }
        }
    }
}
