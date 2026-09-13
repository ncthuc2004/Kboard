package com.kaius.keyboard.ime

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.kaius.keyboard.engine.HidKeyCodes
import com.kaius.keyboard.network.LanBridge
import com.kaius.keyboard.network.LanProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress

class KaiusImeService : InputMethodService() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var listenJob: Job? = null
    private var socket: DatagramSocket? = null

    private var statusView: TextView? = null
    private var telexToggleBtn: Button? = null

    private var currentWord: String = ""
    private var isTelexEnabled: Boolean = true

    override fun onCreate() {
        super.onCreate()

        // 1. Hook into in-process bridge from LanServer
        LanBridge.onKeyReceived = { code, mod ->
            scope.launch(Dispatchers.Main) {
                dispatchKeyToInput(code, mod)
            }
        }

        // 2. Also run standalone UDP listener for when Kaius App is closed
        startStandaloneListener()
    }

    override fun onStartInputView(info: android.view.inputmethod.EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        currentWord = ""
        updateStatus("Sẵn sàng nhận phím")
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        currentWord = ""
    }

    override fun onCreateInputView(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(0xFF0F1117.toInt())
            setPadding(16, 8, 16, 8)
        }

        statusView = TextView(this).apply {
            text = "⌨️ Kaius LAN: Sẵn sàng nhận phím"
            setTextColor(0xFF00E5FF.toInt())
            textSize = 11f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        root.addView(statusView)

        // Telex Toggle Button
        telexToggleBtn = Button(this).apply {
            textSize = 10f
            setPadding(16, 4, 16, 4)
            updateTelexButtonUI(this)
            setOnClickListener {
                isTelexEnabled = !isTelexEnabled
                currentWord = ""
                updateTelexButtonUI(this)
            }
        }
        root.addView(telexToggleBtn)

        return root
    }

    private fun updateTelexButtonUI(btn: Button) {
        btn.text = if (isTelexEnabled) "🇻🇳 Tiếng Việt: BẬT" else "🇺🇸 English"
        val bg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 8f
            setColor(if (isTelexEnabled) 0xFF1B3A4B.toInt() else 0xFF222634.toInt())
            setStroke(1, if (isTelexEnabled) 0xFF00E5FF.toInt() else 0xFF444D66.toInt())
        }
        btn.background = bg
        btn.setTextColor(if (isTelexEnabled) 0xFF00E5FF.toInt() else 0xFF8E99B3.toInt())
    }

    private fun updateStatus(msg: String) {
        statusView?.text = "⌨️ Kaius LAN: $msg"
    }

    private fun startStandaloneListener() {
        if (listenJob != null && listenJob?.isActive == true) return

        listenJob = scope.launch(Dispatchers.IO) {
            try {
                val sock = DatagramSocket(null).apply {
                    reuseAddress = true
                    bind(InetSocketAddress(LanProtocol.PORT))
                }
                socket = sock
                val buffer = ByteArray(1024)

                while (isActive && !sock.isClosed) {
                    val packet = DatagramPacket(buffer, buffer.size)
                    sock.receive(packet)
                    val jsonStr = String(packet.data, 0, packet.length, Charsets.UTF_8).trim()

                    val json = JSONObject(jsonStr)
                    val action = json.optString("a", "")
                    if (action == LanProtocol.ACTION_KEY_DOWN) {
                        val code = json.optInt("k", 0).toByte()
                        val mod = json.optInt("m", 0).toByte()
                        scope.launch(Dispatchers.Main) {
                            dispatchKeyToInput(code, mod)
                        }
                    }
                }
            } catch (_: Exception) {
                // Port bound by LanServer, which automatically forwards to LanBridge!
            }
        }
    }

    private fun dispatchKeyToInput(code: Byte, mod: Byte) {
        val ic = currentInputConnection ?: return
        val isShift = (mod.toInt() and HidKeyCodes.MOD_LEFT_SHIFT.toInt() != 0) ||
                (mod.toInt() and HidKeyCodes.MOD_RIGHT_SHIFT.toInt() != 0)

        when (code) {
            HidKeyCodes.KEY_BACKSPACE -> {
                if (currentWord.isNotEmpty()) {
                    currentWord = currentWord.dropLast(1)
                }
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL))
                updateStatus("⌫ Xóa")
            }
            HidKeyCodes.KEY_ENTER -> {
                currentWord = ""
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                updateStatus("↵ Xuống dòng")
            }
            HidKeyCodes.KEY_SPACE -> {
                currentWord = ""
                ic.commitText(" ", 1)
                updateStatus("␣ Dấu cách")
            }
            HidKeyCodes.KEY_TAB -> {
                currentWord = ""
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_TAB))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_TAB))
                updateStatus("Tab")
            }
            HidKeyCodes.KEY_ESC -> {
                currentWord = ""
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ESCAPE))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ESCAPE))
            }
            HidKeyCodes.KEY_LEFT_ARROW -> {
                currentWord = ""
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_LEFT))
            }
            HidKeyCodes.KEY_RIGHT_ARROW -> {
                currentWord = ""
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_RIGHT))
            }
            HidKeyCodes.KEY_UP_ARROW -> {
                currentWord = ""
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_UP))
            }
            HidKeyCodes.KEY_DOWN_ARROW -> {
                currentWord = ""
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_DOWN))
            }
            else -> {
                val char = getCharForHidKey(code, isShift)
                if (char != null) {
                    if (isTelexEnabled && (char.isLetter() || char == 'w' || char == 'W')) {
                        // Apply Vietnamese Telex transformation
                        val newWord = VietnameseTelexEngine.processKey(currentWord, char)
                        if (newWord != null) {
                            val deleteLen = currentWord.length
                            currentWord = newWord
                            ic.deleteSurroundingText(deleteLen, 0)
                            ic.commitText(newWord, 1)
                            updateStatus("Đã gõ: $newWord")
                        } else {
                            currentWord += char
                            ic.commitText(char.toString(), 1)
                            updateStatus("Đã gõ: $currentWord")
                        }
                    } else {
                        if (!char.isLetter()) {
                            currentWord = "" // Reset word on numbers or punctuation
                        }
                        ic.commitText(char.toString(), 1)
                        updateStatus("Đã gõ: '$char'")
                    }
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
        LanBridge.onKeyReceived = null
        try {
            socket?.close()
        } catch (_: Exception) {}
        listenJob?.cancel()
    }
}
