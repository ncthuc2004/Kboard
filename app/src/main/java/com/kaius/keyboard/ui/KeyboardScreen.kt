package com.kaius.keyboard.ui

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaius.keyboard.engine.HidKeyCodes
import com.kaius.keyboard.engine.KeyItem
import com.kaius.keyboard.network.DiscoveredReceiver
import com.kaius.keyboard.transport.ConnectionStatus
import com.kaius.keyboard.transport.TransportMode
import com.kaius.keyboard.ui.theme.AccentCyan
import com.kaius.keyboard.ui.theme.AccentGreen
import com.kaius.keyboard.ui.theme.AccentOrange
import com.kaius.keyboard.ui.theme.AccentRed
import com.kaius.keyboard.ui.theme.DarkBg
import com.kaius.keyboard.ui.theme.KeyBgModifier
import com.kaius.keyboard.ui.theme.KeyBgModifierActive
import com.kaius.keyboard.ui.theme.KeyBgNormal
import com.kaius.keyboard.ui.theme.KeyBgSpecial
import com.kaius.keyboard.ui.theme.KeyTextActive
import com.kaius.keyboard.ui.theme.KeyTextNormal
import com.kaius.keyboard.ui.theme.KeyTextSub
import com.kaius.keyboard.ui.theme.SurfaceBorder
import com.kaius.keyboard.ui.theme.SurfaceDark
import com.kaius.keyboard.ui.theme.SurfaceElevated

@Composable
fun KeyboardScreen(viewModel: KeyboardViewModel) {
    val appRole by viewModel.appRole.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // TOP APP ROLE SWITCHER: [ ⌨️ Làm bàn phím ] vs [ 🖥️ Nhận bàn phím ]
        AppRoleBar(
            currentRole = appRole,
            onSelectRole = { viewModel.setAppRole(it) }
        )

        if (appRole == AppRole.RECEIVER) {
            // RECEIVER MODE SCREEN
            ReceiverScreen(viewModel = viewModel)
        } else {
            // KEYBOARD MODE SCREEN
            KeyboardModeContent(viewModel = viewModel)
        }
    }
}

@Composable
fun AppRoleBar(
    currentRole: AppRole,
    onSelectRole: (AppRole) -> Unit
) {
    Surface(
        color = SurfaceDark,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            val isKbd = currentRole == AppRole.KEYBOARD
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isKbd) AccentCyan.copy(alpha = 0.25f) else SurfaceElevated)
                    .border(1.dp, if (isKbd) AccentCyan else SurfaceBorder, RoundedCornerShape(6.dp))
                    .clickable { onSelectRole(AppRole.KEYBOARD) }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "⌨️ Làm bàn phím",
                    fontSize = 12.sp,
                    fontWeight = if (isKbd) FontWeight.Bold else FontWeight.Normal,
                    color = if (isKbd) AccentCyan else KeyTextSub
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            val isRcv = currentRole == AppRole.RECEIVER
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isRcv) AccentGreen.copy(alpha = 0.25f) else SurfaceElevated)
                    .border(1.dp, if (isRcv) AccentGreen else SurfaceBorder, RoundedCornerShape(6.dp))
                    .clickable { onSelectRole(AppRole.RECEIVER) }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "🖥️ Nhận bàn phím",
                    fontSize = 12.sp,
                    fontWeight = if (isRcv) FontWeight.Bold else FontWeight.Normal,
                    color = if (isRcv) AccentGreen else KeyTextSub
                )
            }
        }
    }
}

@Composable
fun KeyboardModeContent(viewModel: KeyboardViewModel) {
    val context = LocalContext.current
    val currentMode by viewModel.currentMode.collectAsState()
    val state by viewModel.transportState.collectAsState()
    val modifiers by viewModel.modifiers.collectAsState()
    val isDiagVisible by viewModel.isDiagnosticsVisible.collectAsState()
    val showWifiDialog by viewModel.showWifiDialog.collectAsState()
    val showPairedDialog by viewModel.showPairedDialog.collectAsState()
    val pairedDevices by viewModel.pairedDevices.collectAsState()
    val discoveredReceivers by viewModel.discoveredReceivers.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        // TOP CONTROL BAR: Mode Selector & Status
        TopControlBar(
            currentMode = currentMode,
            state = state,
            onSelectMode = { viewModel.switchMode(it) },
            onMakeDiscoverable = {
                val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                    putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
                }
                context.startActivity(discoverableIntent)
            },
            onOpenPairedDevices = { viewModel.setShowPairedDialog(true) },
            onOpenWifiConfig = { viewModel.setShowWifiDialog(true) },
            onToggleDiagnostics = { viewModel.toggleDiagnostics() },
            onReRegister = { viewModel.reRegisterHid() }
        )

        // AUTO-DISCOVERED RECEIVERS IN LAN BANNER
        if (currentMode == TransportMode.WIFI_LAN && discoveredReceivers.isNotEmpty()) {
            Spacer(modifier = Modifier.height(3.dp))
            DiscoveredReceiversBanner(
                receivers = discoveredReceivers,
                currentTargetIp = state.wifiTargetIp,
                onConnect = { viewModel.connectToDiscoveredReceiver(it) }
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        // QUICK SHORTCUTS & MACRO BAR
        QuickMacroBar(
            onSendMacro = { mod, key -> viewModel.sendMacro(mod, key) }
        )

        Spacer(modifier = Modifier.height(3.dp))

        // MAIN PC KEYBOARD LAYOUT
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            PcKeyboardLayout(
                activeModifiers = modifiers,
                onKeyDown = { viewModel.onKeyDown(it) },
                onKeyUp = { viewModel.onKeyUp(it) },
                onKeyTap = { viewModel.onKeyTap(it) },
                onToggleModifier = { viewModel.toggleModifier(it) }
            )
        }

        // COLLAPSIBLE DIAGNOSTICS CONSOLE
        AnimatedVisibility(
            visible = isDiagVisible,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            DiagnosticsConsole(
                logs = state.logs,
                onClose = { viewModel.toggleDiagnostics() }
            )
        }
    }

    // Wi-Fi Config Dialog
    if (showWifiDialog) {
        WifiConfigDialog(
            currentIp = state.wifiTargetIp,
            currentPort = state.wifiTargetPort,
            onSave = { ip, port -> viewModel.updateWifiTarget(ip, port) },
            onPing = { viewModel.sendWifiPing() },
            onDismiss = { viewModel.setShowWifiDialog(false) }
        )
    }

    // Paired Bluetooth Devices Dialog
    if (showPairedDialog) {
        PairedDevicesDialog(
            devices = pairedDevices,
            onSelect = { viewModel.connectDevice(it) },
            onRefresh = { viewModel.refreshPairedDevices() },
            onDisconnect = { viewModel.disconnectBluetooth() },
            onDismiss = { viewModel.setShowPairedDialog(false) }
        )
    }
}

@Composable
fun DiscoveredReceiversBanner(
    receivers: List<DiscoveredReceiver>,
    currentTargetIp: String,
    onConnect: (DiscoveredReceiver) -> Unit
) {
    Surface(
        color = SurfaceElevated,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AccentGreen.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Tìm thấy:", fontSize = 10.sp, color = AccentGreen, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(receivers) { rcv ->
                    val isCurrent = rcv.ip == currentTargetIp
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isCurrent) AccentGreen.copy(alpha = 0.2f) else SurfaceDark)
                            .border(1.dp, if (isCurrent) AccentGreen else SurfaceBorder, RoundedCornerShape(6.dp))
                            .clickable { onConnect(rcv) }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "🟢 ${rcv.name} (${rcv.ip})",
                            fontSize = 10.sp,
                            color = if (isCurrent) AccentGreen else KeyTextNormal,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopControlBar(
    currentMode: TransportMode,
    state: com.kaius.keyboard.transport.TransportState,
    onSelectMode: (TransportMode) -> Unit,
    onMakeDiscoverable: () -> Unit,
    onOpenPairedDevices: () -> Unit,
    onOpenWifiConfig: () -> Unit,
    onToggleDiagnostics: () -> Unit,
    onReRegister: () -> Unit
) {
    Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Title
                Text(
                    text = "KAIUS KEYBOARD",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentCyan,
                    fontFamily = FontFamily.Monospace
                )

                // Mode Selector Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val isBt = currentMode == TransportMode.BLUETOOTH_HID
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isBt) AccentCyan.copy(alpha = 0.2f) else SurfaceElevated)
                            .border(1.dp, if (isBt) AccentCyan else SurfaceBorder, RoundedCornerShape(6.dp))
                            .clickable { onSelectMode(TransportMode.BLUETOOTH_HID) }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bluetooth,
                                contentDescription = "BT",
                                tint = if (isBt) AccentCyan else KeyTextSub,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("BT HID", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (isBt) AccentCyan else KeyTextSub)
                        }
                    }

                    val isWifi = currentMode == TransportMode.WIFI_LAN
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isWifi) AccentGreen.copy(alpha = 0.2f) else SurfaceElevated)
                            .border(1.dp, if (isWifi) AccentGreen else SurfaceBorder, RoundedCornerShape(6.dp))
                            .clickable { onSelectMode(TransportMode.WIFI_LAN) }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = "WiFi",
                                tint = if (isWifi) AccentGreen else KeyTextSub,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Wi-Fi LAN", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (isWifi) AccentGreen else KeyTextSub)
                        }
                    }
                }

                // Quick Action Icons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (currentMode == TransportMode.BLUETOOTH_HID) {
                        IconButton(onClick = onMakeDiscoverable, modifier = Modifier.size(26.dp)) {
                            Icon(Icons.Default.Search, contentDescription = "Pairing", tint = AccentCyan, modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = onOpenPairedDevices, modifier = Modifier.size(26.dp)) {
                            Icon(Icons.Default.Link, contentDescription = "Paired", tint = KeyTextNormal, modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = onReRegister, modifier = Modifier.size(26.dp)) {
                            Icon(Icons.Default.Refresh, contentDescription = "ReRegister", tint = KeyTextSub, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        IconButton(onClick = onOpenWifiConfig, modifier = Modifier.size(26.dp)) {
                            Icon(Icons.Default.Settings, contentDescription = "Config", tint = AccentGreen, modifier = Modifier.size(16.dp))
                        }
                    }

                    IconButton(onClick = onToggleDiagnostics, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Terminal, contentDescription = "Log", tint = AccentOrange, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Status Badge Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                val statusColor = when (state.status) {
                    ConnectionStatus.CONNECTED -> AccentGreen
                    ConnectionStatus.REGISTERED -> AccentCyan
                    ConnectionStatus.CONNECTING, ConnectionStatus.REGISTERING -> AccentOrange
                    ConnectionStatus.FAILED -> AccentRed
                    ConnectionStatus.DISCONNECTED -> KeyTextSub
                }

                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = state.statusMessage,
                    fontSize = 10.sp,
                    color = statusColor,
                    maxLines = 1,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun QuickMacroBar(onSendMacro: (Byte, Byte) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val macros = listOf(
            Triple("Ctrl+C", HidKeyCodes.MOD_LEFT_CTRL, HidKeyCodes.KEY_C),
            Triple("Ctrl+V", HidKeyCodes.MOD_LEFT_CTRL, HidKeyCodes.KEY_V),
            Triple("Ctrl+A", HidKeyCodes.MOD_LEFT_CTRL, HidKeyCodes.KEY_A),
            Triple("Ctrl+Z", HidKeyCodes.MOD_LEFT_CTRL, HidKeyCodes.KEY_Z),
            Triple("Alt+Tab", HidKeyCodes.MOD_LEFT_ALT, HidKeyCodes.KEY_TAB),
            Triple("Win", HidKeyCodes.MOD_LEFT_GUI, HidKeyCodes.KEY_NONE)
        )

        for ((label, mod, key) in macros) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(26.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(KeyBgSpecial)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(5.dp))
                    .clickable { onSendMacro(mod, key) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentCyan
                )
            }
        }
    }
}

@Composable
fun PcKeyboardLayout(
    activeModifiers: Byte,
    onKeyDown: (Byte) -> Unit,
    onKeyUp: (Byte) -> Unit,
    onKeyTap: (Byte) -> Unit,
    onToggleModifier: (Byte) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Row 1: Function Keys
        val row1 = listOf(
            KeyItem("Esc", null, HidKeyCodes.KEY_ESC, widthWeight = 1.1f),
            KeyItem("F1", null, HidKeyCodes.KEY_F1),
            KeyItem("F2", null, HidKeyCodes.KEY_F2),
            KeyItem("F3", null, HidKeyCodes.KEY_F3),
            KeyItem("F4", null, HidKeyCodes.KEY_F4),
            KeyItem("F5", null, HidKeyCodes.KEY_F5),
            KeyItem("F6", null, HidKeyCodes.KEY_F6),
            KeyItem("F7", null, HidKeyCodes.KEY_F7),
            KeyItem("F8", null, HidKeyCodes.KEY_F8),
            KeyItem("F9", null, HidKeyCodes.KEY_F9),
            KeyItem("F10", null, HidKeyCodes.KEY_F10),
            KeyItem("F11", null, HidKeyCodes.KEY_F11),
            KeyItem("F12", null, HidKeyCodes.KEY_F12),
            KeyItem("Del", null, HidKeyCodes.KEY_DELETE, widthWeight = 1.1f)
        )
        KeyboardRow(row1, activeModifiers, onKeyDown, onKeyUp, onKeyTap, onToggleModifier, Modifier.weight(0.85f))

        // Row 2: Numbers
        val row2 = listOf(
            KeyItem("`", "~", HidKeyCodes.KEY_GRAVE, widthWeight = 0.9f),
            KeyItem("1", "!", HidKeyCodes.KEY_1),
            KeyItem("2", "@", HidKeyCodes.KEY_2),
            KeyItem("3", "#", HidKeyCodes.KEY_3),
            KeyItem("4", "$", HidKeyCodes.KEY_4),
            KeyItem("5", "%", HidKeyCodes.KEY_5),
            KeyItem("6", "^", HidKeyCodes.KEY_6),
            KeyItem("7", "&", HidKeyCodes.KEY_7),
            KeyItem("8", "*", HidKeyCodes.KEY_8),
            KeyItem("9", "(", HidKeyCodes.KEY_9),
            KeyItem("0", ")", HidKeyCodes.KEY_0),
            KeyItem("-", "_", HidKeyCodes.KEY_MINUS),
            KeyItem("=", "+", HidKeyCodes.KEY_EQUAL),
            KeyItem("⌫", null, HidKeyCodes.KEY_BACKSPACE, widthWeight = 1.4f)
        )
        KeyboardRow(row2, activeModifiers, onKeyDown, onKeyUp, onKeyTap, onToggleModifier, Modifier.weight(1f))

        // Row 3: Tab + QWERTY
        val row3 = listOf(
            KeyItem("Tab", null, HidKeyCodes.KEY_TAB, widthWeight = 1.3f),
            KeyItem("Q", null, HidKeyCodes.KEY_Q),
            KeyItem("W", null, HidKeyCodes.KEY_W),
            KeyItem("E", null, HidKeyCodes.KEY_E),
            KeyItem("R", null, HidKeyCodes.KEY_R),
            KeyItem("T", null, HidKeyCodes.KEY_T),
            KeyItem("Y", null, HidKeyCodes.KEY_Y),
            KeyItem("U", null, HidKeyCodes.KEY_U),
            KeyItem("I", null, HidKeyCodes.KEY_I),
            KeyItem("O", null, HidKeyCodes.KEY_O),
            KeyItem("P", null, HidKeyCodes.KEY_P),
            KeyItem("[", "{", HidKeyCodes.KEY_LEFT_BRACKET),
            KeyItem("]", "}", HidKeyCodes.KEY_RIGHT_BRACKET),
            KeyItem("\\", "|", HidKeyCodes.KEY_BACKSLASH, widthWeight = 1f)
        )
        KeyboardRow(row3, activeModifiers, onKeyDown, onKeyUp, onKeyTap, onToggleModifier, Modifier.weight(1f))

        // Row 4: Caps + ASDF + Enter
        val row4 = listOf(
            KeyItem("Caps", null, HidKeyCodes.KEY_CAPS_LOCK, widthWeight = 1.4f),
            KeyItem("A", null, HidKeyCodes.KEY_A),
            KeyItem("S", null, HidKeyCodes.KEY_S),
            KeyItem("D", null, HidKeyCodes.KEY_D),
            KeyItem("F", null, HidKeyCodes.KEY_F),
            KeyItem("G", null, HidKeyCodes.KEY_G),
            KeyItem("H", null, HidKeyCodes.KEY_H),
            KeyItem("J", null, HidKeyCodes.KEY_J),
            KeyItem("K", null, HidKeyCodes.KEY_K),
            KeyItem("L", null, HidKeyCodes.KEY_L),
            KeyItem(";", ":", HidKeyCodes.KEY_SEMICOLON),
            KeyItem("'", "\"", HidKeyCodes.KEY_APOSTROPHE),
            KeyItem("Enter", null, HidKeyCodes.KEY_ENTER, widthWeight = 1.6f)
        )
        KeyboardRow(row4, activeModifiers, onKeyDown, onKeyUp, onKeyTap, onToggleModifier, Modifier.weight(1f))

        // Row 5: Shift + ZXCV + Shift
        val row5 = listOf(
            KeyItem("Shift", null, HidKeyCodes.KEY_NONE, isModifier = true, modifierMask = HidKeyCodes.MOD_LEFT_SHIFT, widthWeight = 1.7f),
            KeyItem("Z", null, HidKeyCodes.KEY_Z),
            KeyItem("X", null, HidKeyCodes.KEY_X),
            KeyItem("C", null, HidKeyCodes.KEY_C),
            KeyItem("V", null, HidKeyCodes.KEY_V),
            KeyItem("B", null, HidKeyCodes.KEY_B),
            KeyItem("N", null, HidKeyCodes.KEY_N),
            KeyItem("M", null, HidKeyCodes.KEY_M),
            KeyItem(",", "<", HidKeyCodes.KEY_COMMA),
            KeyItem(".", ">", HidKeyCodes.KEY_DOT),
            KeyItem("/", "?", HidKeyCodes.KEY_SLASH),
            KeyItem("▲", null, HidKeyCodes.KEY_UP_ARROW, widthWeight = 1f),
            KeyItem("Shift", null, HidKeyCodes.KEY_NONE, isModifier = true, modifierMask = HidKeyCodes.MOD_RIGHT_SHIFT, widthWeight = 1.3f)
        )
        KeyboardRow(row5, activeModifiers, onKeyDown, onKeyUp, onKeyTap, onToggleModifier, Modifier.weight(1f))

        // Row 6: Bottom Controls + Space + Arrows
        val row6 = listOf(
            KeyItem("Ctrl", null, HidKeyCodes.KEY_NONE, isModifier = true, modifierMask = HidKeyCodes.MOD_LEFT_CTRL, widthWeight = 1.2f),
            KeyItem("Win", null, HidKeyCodes.KEY_NONE, isModifier = true, modifierMask = HidKeyCodes.MOD_LEFT_GUI, widthWeight = 1.0f),
            KeyItem("Alt", null, HidKeyCodes.KEY_NONE, isModifier = true, modifierMask = HidKeyCodes.MOD_LEFT_ALT, widthWeight = 1.1f),
            KeyItem("SPACE", null, HidKeyCodes.KEY_SPACE, widthWeight = 4.2f),
            KeyItem("Alt", null, HidKeyCodes.KEY_NONE, isModifier = true, modifierMask = HidKeyCodes.MOD_RIGHT_ALT, widthWeight = 1.1f),
            KeyItem("◄", null, HidKeyCodes.KEY_LEFT_ARROW, widthWeight = 1f),
            KeyItem("▼", null, HidKeyCodes.KEY_DOWN_ARROW, widthWeight = 1f),
            KeyItem("►", null, HidKeyCodes.KEY_RIGHT_ARROW, widthWeight = 1f),
            KeyItem("Ctrl", null, HidKeyCodes.KEY_NONE, isModifier = true, modifierMask = HidKeyCodes.MOD_RIGHT_CTRL, widthWeight = 1.1f)
        )
        KeyboardRow(row6, activeModifiers, onKeyDown, onKeyUp, onKeyTap, onToggleModifier, Modifier.weight(1f))
    }
}

@Composable
fun KeyboardRow(
    keys: List<KeyItem>,
    activeModifiers: Byte,
    onKeyDown: (Byte) -> Unit,
    onKeyUp: (Byte) -> Unit,
    onKeyTap: (Byte) -> Unit,
    onToggleModifier: (Byte) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        for (key in keys) {
            KeyButton(
                item = key,
                activeModifiers = activeModifiers,
                onKeyDown = onKeyDown,
                onKeyUp = onKeyUp,
                onKeyTap = onKeyTap,
                onToggleModifier = onToggleModifier,
                modifier = Modifier.weight(key.widthWeight)
            )
        }
    }
}

@Composable
fun KeyButton(
    item: KeyItem,
    activeModifiers: Byte,
    onKeyDown: (Byte) -> Unit,
    onKeyUp: (Byte) -> Unit,
    onKeyTap: (Byte) -> Unit,
    onToggleModifier: (Byte) -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    val isModActive = item.isModifier && (activeModifiers.toInt() and item.modifierMask.toInt()) != 0

    val bgColor = when {
        isModActive -> KeyBgModifierActive
        isPressed -> AccentCyan.copy(alpha = 0.6f)
        item.isModifier -> KeyBgModifier
        item.keyCode == HidKeyCodes.KEY_ENTER -> AccentCyan.copy(alpha = 0.25f)
        item.keyCode == HidKeyCodes.KEY_SPACE -> KeyBgNormal
        item.keyCode in listOf(HidKeyCodes.KEY_ESC, HidKeyCodes.KEY_BACKSPACE, HidKeyCodes.KEY_TAB) -> KeyBgSpecial
        else -> KeyBgNormal
    }

    val textColor = when {
        isModActive -> KeyTextActive
        item.keyCode == HidKeyCodes.KEY_ENTER -> AccentCyan
        else -> KeyTextNormal
    }

    val borderColor = when {
        isModActive -> AccentCyan
        item.keyCode == HidKeyCodes.KEY_ENTER -> AccentCyan.copy(alpha = 0.5f)
        else -> SurfaceBorder
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(5.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(5.dp))
            .pointerInput(item) {
                detectTapGestures(
                    onPress = {
                        if (item.isModifier) {
                            onToggleModifier(item.modifierMask)
                        } else {
                            isPressed = true
                            onKeyDown(item.keyCode)
                            tryAwaitRelease()
                            isPressed = false
                            onKeyUp(item.keyCode)
                        }
                    },
                    onTap = {
                        if (!item.isModifier) {
                            onKeyTap(item.keyCode)
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (item.subLabel != null) {
                Text(
                    text = item.subLabel,
                    fontSize = 8.sp,
                    color = KeyTextSub,
                    lineHeight = 8.sp
                )
            }
            Text(
                text = item.label,
                fontSize = if (item.label.length > 3) 10.sp else 12.sp,
                fontWeight = if (item.isModifier || isModActive) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
fun DiagnosticsConsole(
    logs: List<com.kaius.keyboard.transport.LogEntry>,
    onClose: () -> Unit
) {
    Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(top = 4.dp)
    ) {
        Column(modifier = Modifier.padding(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "DIAGNOSTICS CONSOLE (${logs.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentOrange,
                    fontFamily = FontFamily.Monospace
                )
                IconButton(onClick = onClose, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = KeyTextSub, modifier = Modifier.size(16.dp))
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBg)
                    .padding(4.dp)
            ) {
                items(logs, key = { it.id }) { entry ->
                    Text(
                        text = "[${entry.timestamp}] ${entry.message}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (entry.isError) AccentRed else KeyTextNormal,
                        lineHeight = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun WifiConfigDialog(
    currentIp: String,
    currentPort: Int,
    onSave: (String, Int) -> Unit,
    onPing: () -> Unit,
    onDismiss: () -> Unit
) {
    var ip by remember { mutableStateOf(currentIp) }
    var port by remember { mutableStateOf(currentPort.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Text("Cấu hình Wi-Fi LAN / Hotspot", color = AccentGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Nhập IP của thiết bị nhận (Redmi Turbo 4 hoặc Laptop):", color = KeyTextNormal, fontSize = 12.sp)

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = { ip = "192.168.43.1" },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentGreen)
                    ) {
                        Text("Hotspot Gateway", fontSize = 10.sp)
                    }
                }

                OutlinedTextField(
                    value = ip,
                    onValueChange = { ip = it },
                    label = { Text("Target IP") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = KeyTextNormal,
                        unfocusedTextColor = KeyTextNormal,
                        focusedBorderColor = AccentGreen,
                        unfocusedBorderColor = SurfaceBorder
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = port,
                    onValueChange = { port = it },
                    label = { Text("Target Port (mặc định 8964)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = KeyTextNormal,
                        unfocusedTextColor = KeyTextNormal,
                        focusedBorderColor = AccentGreen,
                        unfocusedBorderColor = SurfaceBorder
                    ),
                    singleLine = true
                )

                Button(
                    onClick = onPing,
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated)
                ) {
                    Text("Gửi gói thử nghiệm (Test Ping)", color = AccentGreen, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = port.toIntOrNull() ?: 8964
                    onSave(ip.trim(), p)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
            ) {
                Text("Lưu & Kết nối", color = DarkBg, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = KeyTextSub)
            }
        }
    )
}

@SuppressLint("MissingPermission")
@Composable
fun PairedDevicesDialog(
    devices: List<BluetoothDevice>,
    onSelect: (BluetoothDevice) -> Unit,
    onRefresh: () -> Unit,
    onDisconnect: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Thiết bị Bluetooth đã Pair", color = AccentCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onRefresh, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = AccentCyan)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (devices.isEmpty()) {
                    Text(
                        "Chưa có thiết bị nào được pair.\nVui lòng vào Cài đặt Bluetooth trên máy nhận để pair với 'Kaius Keyboard'.",
                        color = KeyTextSub,
                        fontSize = 12.sp
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(devices) { dev ->
                            val devName = try { dev.name ?: "Unknown" } catch (e: SecurityException) { "Unknown" }
                            Surface(
                                color = SurfaceElevated,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(dev) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Bluetooth, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(devName, color = KeyTextNormal, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Text(dev.address, color = KeyTextSub, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDisconnect,
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed.copy(alpha = 0.8f))
            ) {
                Text("Ngắt kết nối hiện tại", color = Color.White, fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng", color = KeyTextSub)
            }
        }
    )
}
