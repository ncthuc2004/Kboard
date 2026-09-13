package com.kaius.keyboard.ime

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
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
    private var telexIndicator: TextView? = null

    private var currentWord: String = ""
    private var isTelexEnabled: Boolean = true

    override fun onCreate() {
        super.onCreate()

        // 1. Hook into in-process bridge from LanServer
        LanBridge.onKeyReceived = { action, code, mod ->
            scope.launch(Dispatchers.Main) {
                dispatchKeyToInput(action, code, mod)
            }
        }

        LanBridge.onTelexChanged = { enabled ->
            scope.launch(Dispatchers.Main) {
                isTelexEnabled = enabled
                currentWord = ""
                updateTelexBadge()
            }
        }

        // 2. Also run standalone UDP listener for when Kaius App is closed
        startStandaloneListener()
    }

    override fun onStartInputView(info: android.view.inputmethod.EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        currentWord = ""
        isTelexEnabled = LanBridge.isTelexEnabled
        updateTelexBadge()
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
            setPadding(16, 10, 16, 10)
        }

        statusView = TextView(this).apply {
            text = "Kboard v${com.kaius.keyboard.BuildConfig.VERSION_NAME}: Sẵn sàng nhận phím"
            setTextColor(0xFF00E5FF.toInt())
            textSize = 11f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        root.addView(statusView)

        // Sleek language mode indicator badge (controlled strictly from keyboard sender, read-only on receiver)
        telexIndicator = TextView(this).apply {
            textSize = 10f
            gravity = Gravity.CENTER
            setPadding(12, 6, 12, 6)
            isClickable = false
            isFocusable = false
            updateTelexBadge()
        }
        root.addView(telexIndicator)

        val spacer1 = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(10, 1)
        }
        root.addView(spacer1)

        // BUTTON: Switch back to normal keyboard (Gboard, Laban Key, etc.)
        val switchKeyboardBtn = TextView(this).apply {
            text = "Đổi bàn phím thường"
            setTextColor(0xFFE2E8F0.toInt())
            textSize = 10f
            gravity = Gravity.CENTER
            setPadding(14, 6, 14, 6)
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 6f
                setColor(0xFF1E222E.toInt())
                setStroke(1, 0xFF38BDF8.toInt())
            }
            background = bg
            setOnClickListener {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.showInputMethodPicker()
            }
        }
        root.addView(switchKeyboardBtn)

        val spacer2 = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(8, 1)
        }
        root.addView(spacer2)

        // BUTTON: Hide / Dismiss Keyboard
        val hideBtn = TextView(this).apply {
            text = "Ẩn"
            setTextColor(0xFF94A3B8.toInt())
            textSize = 10f
            gravity = Gravity.CENTER
            setPadding(12, 6, 12, 6)
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 6f
                setColor(0xFF171A24.toInt())
                setStroke(1, 0xFF2B3040.toInt())
            }
            background = bg
            setOnClickListener {
                requestHideSelf(0)
            }
        }
        root.addView(hideBtn)

        return root
    }

    private fun updateTelexBadge() {
        telexIndicator?.apply {
            text = if (isTelexEnabled) "VI" else "EN"
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 6f
                setColor(if (isTelexEnabled) 0xFF1B3A4B.toInt() else 0xFF222634.toInt())
                setStroke(1, if (isTelexEnabled) 0xFF00E5FF.toInt() else 0xFF444D66.toInt())
            }
            background = bg
            setTextColor(if (isTelexEnabled) 0xFF00E5FF.toInt() else 0xFF8E99B3.toInt())
        }
    }

    private fun updateStatus(msg: String) {
        statusView?.text = "Kboard v${com.kaius.keyboard.BuildConfig.VERSION_NAME} | $msg"
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
                    if (action == LanProtocol.ACTION_DISCOVER) {
                        val deviceName = "${android.os.Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${android.os.Build.MODEL}"
                        val announceJson = JSONObject().apply {
                            put("a", LanProtocol.ACTION_ANNOUNCE)
                            put("name", "$deviceName (Kboard IME)")
                            put("ip", packet.address.hostAddress ?: "")
                            put("port", LanProtocol.PORT)
                        }
                        val respData = announceJson.toString().toByteArray(Charsets.UTF_8)
                        val respPacket = DatagramPacket(respData, respData.size, packet.address, packet.port)
                        sock.send(respPacket)
                    } else if (action == LanProtocol.ACTION_SET_TELEX) {
                        val enabled = json.optInt("tx", 1) == 1 || json.optBoolean("enabled", true)
                        scope.launch(Dispatchers.Main) {
                            isTelexEnabled = enabled
                            currentWord = ""
                            updateTelexBadge()
                        }
                    } else if (action == LanProtocol.ACTION_KEY_DOWN || action == LanProtocol.ACTION_KEY_UP || action == LanProtocol.ACTION_KEY_TAP) {
                        if (json.has("tx")) {
                            val enabled = json.optInt("tx", 1) == 1
                            if (isTelexEnabled != enabled) {
                                isTelexEnabled = enabled
                                scope.launch(Dispatchers.Main) {
                                    updateTelexBadge()
                                }
                            }
                        }
                        val code = json.optInt("k", 0).toByte()
                        val mod = json.optInt("m", 0).toByte()
                        scope.launch(Dispatchers.Main) {
                            dispatchKeyToInput(action, code, mod)
                        }
                    }
                }
            } catch (_: Exception) {
                // Port bound by LanServer, which automatically forwards to LanBridge!
            }
        }
    }

    private fun dispatchKeyToInput(action: String, code: Byte, mod: Byte) {
        val isCtrl = (mod.toInt() and (HidKeyCodes.MOD_LEFT_CTRL.toInt() or HidKeyCodes.MOD_RIGHT_CTRL.toInt())) != 0
        val isShift = (mod.toInt() and (HidKeyCodes.MOD_LEFT_SHIFT.toInt() or HidKeyCodes.MOD_RIGHT_SHIFT.toInt())) != 0
        val isAlt = (mod.toInt() and (HidKeyCodes.MOD_LEFT_ALT.toInt() or HidKeyCodes.MOD_RIGHT_ALT.toInt())) != 0
        val isGui = (mod.toInt() and (HidKeyCodes.MOD_LEFT_GUI.toInt() or HidKeyCodes.MOD_RIGHT_GUI.toInt())) != 0

        val keyAction = when (action) {
            LanProtocol.ACTION_KEY_UP -> KeyEvent.ACTION_UP
            else -> KeyEvent.ACTION_DOWN
        }
        val isTap = (action == LanProtocol.ACTION_KEY_TAP)

        // 0. Handle Modifier-only events (code == KEY_NONE)
        if (code == HidKeyCodes.KEY_NONE) {
            val targetKey = when {
                isGui -> KeyEvent.KEYCODE_META_LEFT
                isCtrl -> KeyEvent.KEYCODE_CTRL_LEFT
                isAlt -> KeyEvent.KEYCODE_ALT_LEFT
                isShift -> KeyEvent.KEYCODE_SHIFT_LEFT
                else -> KeyEvent.KEYCODE_UNKNOWN
            }

            if (targetKey != KeyEvent.KEYCODE_UNKNOWN) {
                val modName = when (targetKey) {
                    KeyEvent.KEYCODE_META_LEFT -> "Win"
                    KeyEvent.KEYCODE_CTRL_LEFT -> "Ctrl"
                    KeyEvent.KEYCODE_ALT_LEFT -> "Alt"
                    KeyEvent.KEYCODE_SHIFT_LEFT -> "Shift"
                    else -> ""
                }

                if (isTap) {
                    injectKeyTap(targetKey, mod)
                    updateStatus("Phím $modName [TAP]")
                } else if (keyAction == KeyEvent.ACTION_DOWN) {
                    injectKeyEvent(KeyEvent.ACTION_DOWN, targetKey, mod)
                    updateStatus("Phím $modName [BẬT]")
                } else {
                    injectKeyEvent(KeyEvent.ACTION_UP, targetKey, mod)
                    updateStatus("Phím $modName [TẮT]")
                }
                return
            } else if (action == LanProtocol.ACTION_KEY_UP) {
                releaseAllModifiers()
                updateStatus("Nhả hết phím")
                return
            }
            return
        }

        // On key up of a regular key, inject ACTION_UP so apps, games and emulators detect release
        if (action == LanProtocol.ACTION_KEY_UP) {
            if (isCtrl && (code == HidKeyCodes.KEY_A || code == HidKeyCodes.KEY_C || code == HidKeyCodes.KEY_V || code == HidKeyCodes.KEY_X || code == HidKeyCodes.KEY_Z)) {
                return
            }
            if (isAlt && code == HidKeyCodes.KEY_TAB) {
                return
            }
            val androidKey = getAndroidKeyCode(code)
            if (androidKey != KeyEvent.KEYCODE_UNKNOWN) {
                injectKeyEvent(KeyEvent.ACTION_UP, androidKey, mod)
            }
            return
        }

        // 1. Handle Alt+Tab (Switch recent apps on Android)
        if (isAlt && code == HidKeyCodes.KEY_TAB) {
            currentWord = ""
            sendDownUpKeyEvents(KeyEvent.KEYCODE_APP_SWITCH)
            updateStatus("Chuyển ứng dụng [Alt+Tab]")
            return
        }

        // 2. Handle Win Combos (Win+D -> Home, Win+E -> Explorer/Files, etc.)
        if (isGui) {
            currentWord = ""
            when (code) {
                HidKeyCodes.KEY_D -> {
                    val intent = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    try { startActivity(intent) } catch (_: Exception) {}
                    injectKeyTap(KeyEvent.KEYCODE_D, mod)
                    updateStatus("Show Desktop [Win+D]")
                    return
                }
                HidKeyCodes.KEY_E, HidKeyCodes.KEY_F -> {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    try { startActivity(intent) } catch (_: Exception) {}
                    injectKeyTap(KeyEvent.KEYCODE_E, mod)
                    updateStatus("Mở Files [Win+E]")
                    return
                }
                HidKeyCodes.KEY_B -> {
                    val intent = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_APP_BROWSER)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    try { startActivity(intent) } catch (_: Exception) {}
                    updateStatus("Mở Browser [Win+B]")
                    return
                }
                else -> {
                    val androidKey = getAndroidKeyCode(code)
                    if (androidKey != KeyEvent.KEYCODE_UNKNOWN) {
                        injectKeyTap(androidKey, mod)
                        updateStatus("Win+Key($code)")
                        return
                    }
                }
            }
        }

        // 3. Handle PC Shortcuts when CTRL is active (Ctrl+C, Ctrl+V, Ctrl+A, Ctrl+X, Ctrl+Z)
        if (isCtrl) {
            currentWord = ""
            val ic = currentInputConnection
            when (code) {
                HidKeyCodes.KEY_C -> {
                    if (ic != null) {
                        ic.performContextMenuAction(android.R.id.copy)
                    } else {
                        injectKeyTap(KeyEvent.KEYCODE_C, mod)
                    }
                    updateStatus("Copy [Ctrl+C]")
                    return
                }
                HidKeyCodes.KEY_V -> {
                    if (ic != null) {
                        ic.performContextMenuAction(android.R.id.paste)
                    } else {
                        injectKeyTap(KeyEvent.KEYCODE_V, mod)
                    }
                    updateStatus("Paste [Ctrl+V]")
                    return
                }
                HidKeyCodes.KEY_A -> {
                    if (ic != null) {
                        ic.performContextMenuAction(android.R.id.selectAll)
                    } else {
                        injectKeyTap(KeyEvent.KEYCODE_A, mod)
                    }
                    updateStatus("Select All [Ctrl+A]")
                    return
                }
                HidKeyCodes.KEY_X -> {
                    if (ic != null) {
                        ic.performContextMenuAction(android.R.id.cut)
                    } else {
                        injectKeyTap(KeyEvent.KEYCODE_X, mod)
                    }
                    updateStatus("Cut [Ctrl+X]")
                    return
                }
                HidKeyCodes.KEY_Z -> {
                    if (ic != null) {
                        ic.performContextMenuAction(android.R.id.undo)
                    } else {
                        injectKeyTap(KeyEvent.KEYCODE_Z, mod)
                    }
                    updateStatus("Undo [Ctrl+Z]")
                    return
                }
                else -> {
                    val androidKey = getAndroidKeyCode(code)
                    if (androidKey != KeyEvent.KEYCODE_UNKNOWN) {
                        injectKeyTap(androidKey, mod)
                        updateStatus("Ctrl+Key($code)")
                        return
                    }
                }
            }
        }

        // 4. Handle ALT combos
        if (isAlt) {
            currentWord = ""
            val androidKey = getAndroidKeyCode(code)
            if (androidKey != KeyEvent.KEYCODE_UNKNOWN) {
                injectKeyTap(androidKey, mod)
                return
            }
        }

        val ic = currentInputConnection
        if (ic == null) {
            // No focused text field (e.g. user on Receiver screen, Game, Home): dispatch system keys directly
            val androidKey = getAndroidKeyCode(code)
            if (androidKey != KeyEvent.KEYCODE_UNKNOWN) {
                sendDownUpKeyEvents(androidKey)
            }
            return
        }

        // 5. Navigation, Editing, and Function Keys
        when (code) {
            HidKeyCodes.KEY_BACKSPACE -> {
                if (currentWord.isNotEmpty()) {
                    currentWord = currentWord.dropLast(1)
                }
                injectKeyTap(KeyEvent.KEYCODE_DEL, mod)
                updateStatus("Backspace")
            }
            HidKeyCodes.KEY_DELETE -> {
                currentWord = ""
                injectKeyTap(KeyEvent.KEYCODE_FORWARD_DEL, mod)
                updateStatus("Delete")
            }
            HidKeyCodes.KEY_ENTER -> {
                currentWord = ""
                injectKeyTap(KeyEvent.KEYCODE_ENTER, mod)
                updateStatus("Enter")
            }
            HidKeyCodes.KEY_SPACE -> {
                currentWord = ""
                ic.commitText(" ", 1)
                updateStatus("Space")
            }
            HidKeyCodes.KEY_TAB -> {
                currentWord = ""
                injectKeyTap(KeyEvent.KEYCODE_TAB, mod)
                updateStatus("Tab")
            }
            HidKeyCodes.KEY_ESC -> {
                currentWord = ""
                injectKeyTap(KeyEvent.KEYCODE_ESCAPE, mod)
                updateStatus("Esc")
            }
            HidKeyCodes.KEY_LEFT_ARROW -> {
                currentWord = ""
                injectKeyTap(KeyEvent.KEYCODE_DPAD_LEFT, mod)
            }
            HidKeyCodes.KEY_RIGHT_ARROW -> {
                currentWord = ""
                injectKeyTap(KeyEvent.KEYCODE_DPAD_RIGHT, mod)
            }
            HidKeyCodes.KEY_UP_ARROW -> {
                currentWord = ""
                injectKeyTap(KeyEvent.KEYCODE_DPAD_UP, mod)
            }
            HidKeyCodes.KEY_DOWN_ARROW -> {
                currentWord = ""
                injectKeyTap(KeyEvent.KEYCODE_DPAD_DOWN, mod)
            }
            HidKeyCodes.KEY_HOME -> {
                currentWord = ""
                injectKeyTap(KeyEvent.KEYCODE_MOVE_HOME, mod)
            }
            HidKeyCodes.KEY_END -> {
                currentWord = ""
                injectKeyTap(KeyEvent.KEYCODE_MOVE_END, mod)
            }
            HidKeyCodes.KEY_PAGE_UP -> {
                currentWord = ""
                injectKeyTap(KeyEvent.KEYCODE_PAGE_UP, mod)
            }
            HidKeyCodes.KEY_PAGE_DOWN -> {
                currentWord = ""
                injectKeyTap(KeyEvent.KEYCODE_PAGE_DOWN, mod)
            }
            HidKeyCodes.KEY_CAPS_LOCK -> {
                injectKeyTap(KeyEvent.KEYCODE_CAPS_LOCK, mod)
            }
            in HidKeyCodes.KEY_F1..HidKeyCodes.KEY_F12 -> {
                currentWord = ""
                val fKey = KeyEvent.KEYCODE_F1 + (code - HidKeyCodes.KEY_F1)
                injectKeyTap(fKey, mod)
                updateStatus("F${code - HidKeyCodes.KEY_F1 + 1}")
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

    private fun injectKeyEvent(keyAction: Int, androidKeyCode: Int, mod: Byte) {
        var meta = 0
        if ((mod.toInt() and (HidKeyCodes.MOD_LEFT_CTRL.toInt() or HidKeyCodes.MOD_RIGHT_CTRL.toInt())) != 0) {
            meta = meta or KeyEvent.META_CTRL_ON or KeyEvent.META_CTRL_LEFT_ON
        }
        if ((mod.toInt() and (HidKeyCodes.MOD_LEFT_ALT.toInt() or HidKeyCodes.MOD_RIGHT_ALT.toInt())) != 0) {
            meta = meta or KeyEvent.META_ALT_ON or KeyEvent.META_ALT_LEFT_ON
        }
        if ((mod.toInt() and (HidKeyCodes.MOD_LEFT_SHIFT.toInt() or HidKeyCodes.MOD_RIGHT_SHIFT.toInt())) != 0) {
            meta = meta or KeyEvent.META_SHIFT_ON or KeyEvent.META_SHIFT_LEFT_ON
        }
        if ((mod.toInt() and (HidKeyCodes.MOD_LEFT_GUI.toInt() or HidKeyCodes.MOD_RIGHT_GUI.toInt())) != 0) {
            meta = meta or KeyEvent.META_META_ON or KeyEvent.META_META_LEFT_ON
        }

        val now = android.os.SystemClock.uptimeMillis()
        val event = KeyEvent(now, now, keyAction, androidKeyCode, 0, meta)
        val ic = currentInputConnection
        if (ic != null) {
            ic.sendKeyEvent(event)
        } else {
            sendDownUpKeyEvents(androidKeyCode)
        }
    }

    private fun injectKeyTap(androidKeyCode: Int, mod: Byte) {
        injectKeyEvent(KeyEvent.ACTION_DOWN, androidKeyCode, mod)
        injectKeyEvent(KeyEvent.ACTION_UP, androidKeyCode, mod)
    }

    private fun releaseAllModifiers() {
        injectKeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_CTRL_LEFT, 0)
        injectKeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ALT_LEFT, 0)
        injectKeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT, 0)
        injectKeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_META_LEFT, 0)
    }

    private fun getAndroidKeyCode(code: Byte): Int {
        return when (code) {
            in HidKeyCodes.KEY_A..HidKeyCodes.KEY_Z -> KeyEvent.KEYCODE_A + (code - HidKeyCodes.KEY_A)
            HidKeyCodes.KEY_1 -> KeyEvent.KEYCODE_1
            HidKeyCodes.KEY_2 -> KeyEvent.KEYCODE_2
            HidKeyCodes.KEY_3 -> KeyEvent.KEYCODE_3
            HidKeyCodes.KEY_4 -> KeyEvent.KEYCODE_4
            HidKeyCodes.KEY_5 -> KeyEvent.KEYCODE_5
            HidKeyCodes.KEY_6 -> KeyEvent.KEYCODE_6
            HidKeyCodes.KEY_7 -> KeyEvent.KEYCODE_7
            HidKeyCodes.KEY_8 -> KeyEvent.KEYCODE_8
            HidKeyCodes.KEY_9 -> KeyEvent.KEYCODE_9
            HidKeyCodes.KEY_0 -> KeyEvent.KEYCODE_0
            HidKeyCodes.KEY_ENTER -> KeyEvent.KEYCODE_ENTER
            HidKeyCodes.KEY_ESC -> KeyEvent.KEYCODE_ESCAPE
            HidKeyCodes.KEY_BACKSPACE -> KeyEvent.KEYCODE_DEL
            HidKeyCodes.KEY_TAB -> KeyEvent.KEYCODE_TAB
            HidKeyCodes.KEY_SPACE -> KeyEvent.KEYCODE_SPACE
            HidKeyCodes.KEY_MINUS -> KeyEvent.KEYCODE_MINUS
            HidKeyCodes.KEY_EQUAL -> KeyEvent.KEYCODE_EQUALS
            HidKeyCodes.KEY_LEFT_BRACKET -> KeyEvent.KEYCODE_LEFT_BRACKET
            HidKeyCodes.KEY_RIGHT_BRACKET -> KeyEvent.KEYCODE_RIGHT_BRACKET
            HidKeyCodes.KEY_BACKSLASH -> KeyEvent.KEYCODE_BACKSLASH
            HidKeyCodes.KEY_SEMICOLON -> KeyEvent.KEYCODE_SEMICOLON
            HidKeyCodes.KEY_APOSTROPHE -> KeyEvent.KEYCODE_APOSTROPHE
            HidKeyCodes.KEY_GRAVE -> KeyEvent.KEYCODE_GRAVE
            HidKeyCodes.KEY_COMMA -> KeyEvent.KEYCODE_COMMA
            HidKeyCodes.KEY_DOT -> KeyEvent.KEYCODE_PERIOD
            HidKeyCodes.KEY_SLASH -> KeyEvent.KEYCODE_SLASH
            HidKeyCodes.KEY_CAPS_LOCK -> KeyEvent.KEYCODE_CAPS_LOCK
            HidKeyCodes.KEY_DELETE -> KeyEvent.KEYCODE_FORWARD_DEL
            HidKeyCodes.KEY_RIGHT_ARROW -> KeyEvent.KEYCODE_DPAD_RIGHT
            HidKeyCodes.KEY_LEFT_ARROW -> KeyEvent.KEYCODE_DPAD_LEFT
            HidKeyCodes.KEY_DOWN_ARROW -> KeyEvent.KEYCODE_DPAD_DOWN
            HidKeyCodes.KEY_UP_ARROW -> KeyEvent.KEYCODE_DPAD_UP
            in HidKeyCodes.KEY_F1..HidKeyCodes.KEY_F12 -> KeyEvent.KEYCODE_F1 + (code - HidKeyCodes.KEY_F1)
            else -> KeyEvent.KEYCODE_UNKNOWN
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
