package com.kaius.keyboard.ime

import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.kaius.keyboard.engine.HidKeyCodes
import com.kaius.keyboard.network.LanProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket

class KaiusImeService : InputMethodService() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var listenJob: Job? = null
    private var socket: DatagramSocket? = null
    private var statusView: TextView? = null

    override fun onCreate() {
        super.onCreate()
        startLanListener()
    }

    override fun onCreateInputView(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(0xFF0F1117.toInt())
            setPadding(24, 12, 24, 12)
        }

        statusView = TextView(this).apply {
            text = "⌨️ Kaius LAN Keyboard: Đang nhận phím từ xa..."
            setTextColor(0xFF00E5FF.toInt())
            textSize = 12f
        }

        root.addView(statusView)
        return root
    }

    private fun startLanListener() {
        if (listenJob != null && listenJob?.isActive == true) return

        listenJob = scope.launch {
            try {
                val sock = DatagramSocket(LanProtocol.PORT).apply {
                    reuseAddress = true
                    broadcast = true
                }
                socket = sock
                val buffer = ByteArray(1024)

                while (isActive && !sock.isClosed) {
                    val packet = DatagramPacket(buffer, buffer.size)
                    sock.receive(packet)
                    val jsonStr = String(packet.data, 0, packet.length, Charsets.UTF_8).trim()
                    handlePacket(jsonStr)
                }
            } catch (e: Exception) {
                // Port might be shared with in-app server if both running, but IME takes precedence when typing
            }
        }
    }

    private fun handlePacket(jsonStr: String) {
        try {
            val json = JSONObject(jsonStr)
            val action = json.optString("a", "")
            val code = json.optInt("k", 0).toByte()
            val mod = json.optInt("m", 0).toByte()

            if (action == LanProtocol.ACTION_KEY_DOWN) {
                dispatchKeyToInputConnection(code, mod)
            }
        } catch (_: Exception) {
        }
    }

    private fun dispatchKeyToInputConnection(code: Byte, mod: Byte) {
        val ic = currentInputConnection ?: return
        val isShift = (mod.toInt() and HidKeyCodes.MOD_LEFT_SHIFT.toInt() != 0) ||
                (mod.toInt() and HidKeyCodes.MOD_RIGHT_SHIFT.toInt() != 0)

        when (code) {
            HidKeyCodes.KEY_BACKSPACE -> {
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL))
            }
            HidKeyCodes.KEY_ENTER -> {
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
            }
            HidKeyCodes.KEY_SPACE -> {
                ic.commitText(" ", 1)
            }
            HidKeyCodes.KEY_TAB -> {
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_TAB))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_TAB))
            }
            HidKeyCodes.KEY_ESC -> {
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ESCAPE))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ESCAPE))
            }
            HidKeyCodes.KEY_LEFT_ARROW -> {
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_LEFT))
            }
            HidKeyCodes.KEY_RIGHT_ARROW -> {
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_RIGHT))
            }
            HidKeyCodes.KEY_UP_ARROW -> {
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_UP))
            }
            HidKeyCodes.KEY_DOWN_ARROW -> {
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_DOWN))
            }
            else -> {
                // Printable characters
                val char = getCharForHidKey(code, isShift)
                if (char != null) {
                    ic.commitText(char.toString(), 1)
                }
            }
        }
    }

    private fun getCharForHidKey(code: Byte, isShift: Boolean): Char? {
        return when (code) {
            in HidKeyCodes.KEY_A..HidKeyCodes.KEY_Z -> {
                val base = if (isShift) 'A'.code else 'a'.code
                (base + (code - HidKeyCodes.KEY_A)).toChar()
            }
            HidKeyCodes.KEY_1 -> if (isShift) '!' else '1'
            HidKeyCodes.KEY_2 -> if (isShift) '@' else '2'
            HidKeyCodes.KEY_3 -> if (isShift) '#' else '3'
            HidKeyCodes.KEY_4 -> if (isShift) '$' else '4'
            HidKeyCodes.KEY_5 -> if (isShift) '%' else '5'
            HidKeyCodes.KEY_6 -> if (isShift) '^' else '6'
            HidKeyCodes.KEY_7 -> if (isShift) '&' else '7'
            HidKeyCodes.KEY_8 -> if (isShift) '*' else '8'
            HidKeyCodes.KEY_9 -> if (isShift) '(' else '9'
            HidKeyCodes.KEY_0 -> if (isShift) ')' else '0'
            HidKeyCodes.KEY_MINUS -> if (isShift) '_' else '-'
            HidKeyCodes.KEY_EQUAL -> if (isShift) '+' else '='
            HidKeyCodes.KEY_LEFT_BRACKET -> if (isShift) '{' else '['
            HidKeyCodes.KEY_RIGHT_BRACKET -> if (isShift) '}' else ']'
            HidKeyCodes.KEY_BACKSLASH -> if (isShift) '|' else '\\'
            HidKeyCodes.KEY_SEMICOLON -> if (isShift) ':' else ';'
            HidKeyCodes.KEY_APOSTROPHE -> if (isShift) '"' else '\''
            HidKeyCodes.KEY_GRAVE -> if (isShift) '~' else '`'
            HidKeyCodes.KEY_COMMA -> if (isShift) '<' else ','
            HidKeyCodes.KEY_DOT -> if (isShift) '>' else '.'
            HidKeyCodes.KEY_SLASH -> if (isShift) '?' else '/'
            else -> null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            socket?.close()
        } catch (_: Exception) {}
        listenJob?.cancel()
    }
}
