package com.kaius.keyboard.ui

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.provider.Settings
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
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
import com.kaius.keyboard.ui.theme.AccentPrimary
import com.kaius.keyboard.ui.theme.AppBg
import com.kaius.keyboard.ui.theme.KeyActiveAccent
import com.kaius.keyboard.ui.theme.KeyActiveBg
import com.kaius.keyboard.ui.theme.KeyModifierBg
import com.kaius.keyboard.ui.theme.KeyNormalBg
import com.kaius.keyboard.ui.theme.KeyNormalPressed
import com.kaius.keyboard.ui.theme.KeySpecialBg
import com.kaius.keyboard.ui.theme.KeyTextMain
import com.kaius.keyboard.ui.theme.KeyTextMuted
import com.kaius.keyboard.ui.theme.KeyTextSubtle
import com.kaius.keyboard.ui.theme.StatusError
import com.kaius.keyboard.ui.theme.StatusPending
import com.kaius.keyboard.ui.theme.StatusSuccess
import com.kaius.keyboard.ui.theme.SurfaceBar
import com.kaius.keyboard.ui.theme.SurfaceBorder
import com.kaius.keyboard.ui.theme.SurfaceBorderSubtle
import com.kaius.keyboard.ui.theme.SurfaceElevated

@Composable
fun KeyboardScreen(viewModel: KeyboardViewModel) {
    val context = LocalContext.current
    val appRole by viewModel.appRole.collectAsState()
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
            .background(AppBg)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        // UNIFIED ULTRA-COMPACT LANDSCAPE HEADER (Single line: Role + Transport + Status + Actions)
        LandscapeUnifiedHeader(
            currentRole = appRole,
            currentMode = currentMode,
            state = state,
            onSelectRole = { viewModel.setAppRole(it) },
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

        if (appRole == AppRole.RECEIVER) {
            ReceiverScreen(viewModel = viewModel)
        } else {
            // PERMANENT LAN CONNECTION BAR: Target IP, Discovered Devices & Scan
            if (currentMode == TransportMode.WIFI_LAN) {
                Spacer(modifier = Modifier.height(2.dp))
                LanConnectionBar(
                    targetIp = state.wifiTargetIp,
                    targetPort = state.wifiTargetPort,
                    receivers = discoveredReceivers,
                    onOpenConfig = { viewModel.setShowWifiDialog(true) },
                    onScan = { viewModel.lanDiscovery.startDiscovery() },
                    onSelectReceiver = { viewModel.connectToDiscoveredReceiver(it) }
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // QUICK SHORTCUTS & MACRO BAR (Sleek minimalist keys)
            QuickMacroBar(
                onSendMacro = { mod, key -> viewModel.sendMacro(mod, key) }
            )

            Spacer(modifier = Modifier.height(2.dp))

            // MAIN PC KEYBOARD LAYOUT (Fills 90% of landscape height)
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
fun LandscapeUnifiedHeader(
    currentRole: AppRole,
    currentMode: TransportMode,
    state: com.kaius.keyboard.transport.TransportState,
    onSelectRole: (AppRole) -> Unit,
    onSelectMode: (TransportMode) -> Unit,
    onMakeDiscoverable: () -> Unit,
    onOpenPairedDevices: () -> Unit,
    onOpenWifiConfig: () -> Unit,
    onToggleDiagnostics: () -> Unit,
    onReRegister: () -> Unit
) {
    Surface(
        color = SurfaceBar,
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // LEFT: Brand + Role Switcher (Bàn phím | Nhận phím)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "KAIUS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = KeyTextMain,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Role Segment
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(AppBg)
                        .border(1.dp, SurfaceBorderSubtle, RoundedCornerShape(3.dp))
                        .padding(1.dp)
                ) {
                    Row {
                        val isKbd = currentRole == AppRole.KEYBOARD
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isKbd) SurfaceElevated else Color.Transparent)
                                .clickable { onSelectRole(AppRole.KEYBOARD) }
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "Bàn phím",
                                fontSize = 10.sp,
                                fontWeight = if (isKbd) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isKbd) KeyTextMain else KeyTextSubtle
                            )
                        }

                        val isRcv = currentRole == AppRole.RECEIVER
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isRcv) SurfaceElevated else Color.Transparent)
                                .clickable { onSelectRole(AppRole.RECEIVER) }
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "Nhận phím",
                                fontSize = 10.sp,
                                fontWeight = if (isRcv) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isRcv) KeyTextMain else KeyTextSubtle
                            )
                        }
                    }
                }
            }

            // CENTER: Transport Segment (Bluetooth | Wi-Fi LAN) + Status Dot
            if (currentRole == AppRole.KEYBOARD) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(AppBg)
                            .border(1.dp, SurfaceBorderSubtle, RoundedCornerShape(3.dp))
                            .padding(1.dp)
                    ) {
                        Row {
                            val isBt = currentMode == TransportMode.BLUETOOTH_HID
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (isBt) SurfaceElevated else Color.Transparent)
                                    .clickable { onSelectMode(TransportMode.BLUETOOTH_HID) }
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "Bluetooth",
                                    fontSize = 10.sp,
                                    fontWeight = if (isBt) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isBt) KeyTextMain else KeyTextSubtle
                                )
                            }

                            val isWifi = currentMode == TransportMode.WIFI_LAN
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (isWifi) SurfaceElevated else Color.Transparent)
                                    .clickable { onSelectMode(TransportMode.WIFI_LAN) }
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "Wi-Fi LAN",
                                    fontSize = 10.sp,
                                    fontWeight = if (isWifi) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isWifi) KeyTextMain else KeyTextSubtle
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Status Dot + Host text
                    val dotColor = when (state.status) {
                        ConnectionStatus.CONNECTED -> StatusSuccess
                        ConnectionStatus.REGISTERED -> AccentPrimary
                        ConnectionStatus.CONNECTING, ConnectionStatus.REGISTERING -> StatusPending
                        ConnectionStatus.FAILED -> StatusError
                        ConnectionStatus.DISCONNECTED -> KeyTextSubtle
                    }

                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = state.statusMessage,
                        fontSize = 10.sp,
                        color = KeyTextMuted,
                        maxLines = 1,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // RIGHT: Action Icons
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (currentRole == AppRole.KEYBOARD) {
                    if (currentMode == TransportMode.BLUETOOTH_HID) {
                        IconButton(onClick = onMakeDiscoverable, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Search, contentDescription = "Pairing", tint = KeyTextMuted, modifier = Modifier.size(15.dp))
                        }
                        IconButton(onClick = onOpenPairedDevices, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Devices, contentDescription = "Devices", tint = KeyTextMuted, modifier = Modifier.size(15.dp))
                        }
                        IconButton(onClick = onReRegister, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = KeyTextSubtle, modifier = Modifier.size(15.dp))
                        }
                    } else {
                        IconButton(onClick = onOpenWifiConfig, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = KeyTextMuted, modifier = Modifier.size(15.dp))
                        }
                    }

                    IconButton(onClick = onToggleDiagnostics, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Terminal, contentDescription = "Console", tint = KeyTextSubtle, modifier = Modifier.size(15.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun LanConnectionBar(
    targetIp: String,
    targetPort: Int,
    receivers: List<DiscoveredReceiver>,
    onOpenConfig: () -> Unit,
    onScan: () -> Unit,
    onSelectReceiver: (DiscoveredReceiver) -> Unit
) {
    Surface(
        color = SurfaceBar,
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .height(26.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // LEFT: Current target IP & Clickable connect/change button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(SurfaceElevated)
                    .clickable { onOpenConfig() }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "🔗 Gửi tới: $targetIp:$targetPort",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentPrimary,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "[Đổi IP]",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = StatusSuccess
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // CENTER: Discovered devices or notice
            if (receivers.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(receivers) { rcv ->
                        val isCurrent = rcv.ip == targetIp
                        Text(
                            text = "⚡ ${rcv.name} (${rcv.ip})",
                            fontSize = 9.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrent) StatusSuccess else KeyTextMain,
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isCurrent) KeyActiveBg else SurfaceElevated)
                                .clickable { onSelectReceiver(rcv) }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            } else {
                Text(
                    text = "Bấm [Đổi IP] hoặc [Quét LAN] để kết nối",
                    fontSize = 9.sp,
                    color = KeyTextSubtle,
                    modifier = Modifier.weight(1f)
                )
            }

            // RIGHT: Scan button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(SurfaceElevated)
                    .clickable { onScan() }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Scan", tint = AccentPrimary, modifier = Modifier.size(11.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("Quét LAN", fontSize = 9.sp, color = AccentPrimary, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun QuickMacroBar(onSendMacro: (Byte, Byte) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
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
                    .fillMaxSize()
                    .clip(RoundedCornerShape(3.dp))
                    .background(KeySpecialBg)
                    .border(1.dp, SurfaceBorderSubtle, RoundedCornerShape(3.dp))
                    .clickable { onSendMacro(mod, key) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = KeyTextMuted,
                    fontFamily = FontFamily.Monospace
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
    val coroutineScope = rememberCoroutineScope()

    val isModActive = item.isModifier && (activeModifiers.toInt() and item.modifierMask.toInt()) != 0

    val bgColor = when {
        isModActive -> KeyActiveBg
        isPressed -> KeyNormalPressed
        item.isModifier -> KeyModifierBg
        item.keyCode == HidKeyCodes.KEY_ENTER -> KeySpecialBg
        item.keyCode in listOf(HidKeyCodes.KEY_ESC, HidKeyCodes.KEY_BACKSPACE, HidKeyCodes.KEY_TAB) -> KeySpecialBg
        else -> KeyNormalBg
    }

    val textColor = when {
        isModActive -> KeyActiveAccent
        isPressed -> KeyTextMain
        item.keyCode == HidKeyCodes.KEY_ENTER -> KeyTextMain
        else -> KeyTextMain
    }

    val borderColor = when {
        isModActive -> KeyActiveAccent
        isPressed -> SurfaceBorder
        else -> SurfaceBorderSubtle
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .pointerInput(item) {
                detectTapGestures(
                    onPress = {
                        if (item.isModifier) {
                            onToggleModifier(item.modifierMask)
                        } else {
                            isPressed = true
                            onKeyDown(item.keyCode)

                            // Key Repeat (Đè phím lặp lại mượt mà như phím thật)
                            val repeatJob = coroutineScope.launch {
                                delay(380)
                                while (isActive) {
                                    onKeyDown(item.keyCode)
                                    delay(45)
                                }
                            }

                            tryAwaitRelease()
                            repeatJob.cancel()
                            isPressed = false
                            onKeyUp(item.keyCode)
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
                    color = KeyTextSubtle,
                    lineHeight = 8.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = item.label,
                fontSize = if (item.label.length > 3) 10.sp else 12.sp,
                fontWeight = if (item.isModifier || isModActive) FontWeight.Bold else FontWeight.Normal,
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
        color = SurfaceBar,
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderSubtle),
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
                    "LOG CONSOLE (${logs.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = KeyTextMuted,
                    fontFamily = FontFamily.Monospace
                )
                IconButton(onClick = onClose, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = KeyTextSubtle, modifier = Modifier.size(16.dp))
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppBg)
                    .padding(4.dp)
            ) {
                items(logs, key = { it.id }) { entry ->
                    Text(
                        text = "[${entry.timestamp}] ${entry.message}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (entry.isError) StatusError else KeyTextMuted,
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
        containerColor = SurfaceBar,
        title = {
            Text("Cấu hình Wi-Fi LAN", color = KeyTextMain, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("IP thiết bị nhận (Redmi Turbo 4 hoặc Laptop):", color = KeyTextMuted, fontSize = 12.sp)

                Text(
                    text = "Dùng Hotspot Gateway (192.168.43.1)",
                    color = AccentPrimary,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .clickable { ip = "192.168.43.1" }
                        .padding(vertical = 2.dp)
                )

                OutlinedTextField(
                    value = ip,
                    onValueChange = { ip = it },
                    label = { Text("IP Đích", fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = KeyTextMain,
                        unfocusedTextColor = KeyTextMain,
                        focusedBorderColor = AccentPrimary,
                        unfocusedBorderColor = SurfaceBorderSubtle
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = port,
                    onValueChange = { port = it },
                    label = { Text("Cổng (8964)", fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = KeyTextMain,
                        unfocusedTextColor = KeyTextMain,
                        focusedBorderColor = AccentPrimary,
                        unfocusedBorderColor = SurfaceBorderSubtle
                    ),
                    singleLine = true
                )

                TextButton(onClick = onPing) {
                    Text("Gửi gói Ping thử nghiệm", color = KeyTextMuted, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = port.toIntOrNull() ?: 8964
                    onSave(ip.trim(), p)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated)
            ) {
                Text("Lưu kết nối", color = KeyTextMain, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = KeyTextSubtle)
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
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceBar,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Thiết bị Bluetooth", color = KeyTextMain, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = onRefresh, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = KeyTextMuted)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Quick actions: Cho máy khác thấy + Mở Cài đặt Bluetooth của máy
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = {
                            val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
                            }
                            context.startActivity(discoverableIntent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1f).height(32.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text("📡 Cho máy khác thấy (300s)", color = KeyTextMain, fontSize = 10.sp)
                    }

                    Button(
                        onClick = {
                            try {
                                context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                            } catch (e: Exception) {
                                // Ignore
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1f).height(32.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text("⚙️ Cài đặt Bluetooth máy", color = KeyTextMain, fontSize = 10.sp)
                    }
                }

                if (devices.isEmpty()) {
                    Text(
                        "Chưa có thiết bị nào được ghép đôi.\n\n💡 Mẹo: Bấm 'Cài đặt Bluetooth máy' ở trên để ghép đôi với Laptop trước. Sau khi ghép đôi, quay lại đây bấm vào tên Laptop để kết nối ngay!",
                        color = KeyTextSubtle,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(devices) { dev ->
                            val devName = try { dev.name ?: "Unknown" } catch (e: SecurityException) { "Unknown" }
                            Surface(
                                color = SurfaceElevated,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(dev) }
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(devName, color = KeyTextMain, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text(dev.address, color = KeyTextSubtle, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
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
                colors = ButtonDefaults.buttonColors(containerColor = StatusError.copy(alpha = 0.2f))
            ) {
                Text("Ngắt kết nối", color = StatusError, fontSize = 11.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng", color = KeyTextSubtle)
            }
        }
    )
}
