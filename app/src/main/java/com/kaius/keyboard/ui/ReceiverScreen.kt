package com.kaius.keyboard.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaius.keyboard.engine.HidKeyCodes
import com.kaius.keyboard.network.ReceiverState
import com.kaius.keyboard.ui.theme.AccentPrimary
import com.kaius.keyboard.ui.theme.AppBg
import com.kaius.keyboard.ui.theme.KeyActiveAccent
import com.kaius.keyboard.ui.theme.KeyActiveBg
import com.kaius.keyboard.ui.theme.KeyTextMain
import com.kaius.keyboard.ui.theme.KeyTextMuted
import com.kaius.keyboard.ui.theme.KeyTextSubtle
import com.kaius.keyboard.ui.theme.StatusError
import com.kaius.keyboard.ui.theme.StatusSuccess
import com.kaius.keyboard.ui.theme.SurfaceBar
import com.kaius.keyboard.ui.theme.SurfaceBorderSubtle
import com.kaius.keyboard.ui.theme.SurfaceElevated

@Composable
fun ReceiverScreen(viewModel: KeyboardViewModel) {
    val context = LocalContext.current
    val receiverState by viewModel.receiverState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
            .padding(8.dp)
    ) {
        // TOP RECEIVER STATUS CARD
        ReceiverHeaderCard(
            state = receiverState,
            onCopyIp = {
                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("IP", receiverState.localIp))
                Toast.makeText(context, "Đã sao chép IP: ${receiverState.localIp}", Toast.LENGTH_SHORT).show()
            },
            onRestart = { viewModel.restartReceiver() }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // LIVE VISUAL KEY MONITOR
        VisualKeyDisplayCard(state = receiverState)

        Spacer(modifier = Modifier.height(8.dp))

        // ACCUMULATED TEXT BUFFER CARD
        AccumulatedTextCard(
            text = receiverState.accumulatedText,
            onCopyText = {
                if (receiverState.accumulatedText.isNotEmpty()) {
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("TypedText", receiverState.accumulatedText))
                    Toast.makeText(context, "Đã sao chép nội dung!", Toast.LENGTH_SHORT).show()
                }
            },
            onClear = { viewModel.clearAccumulatedText() }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // REALTIME KEY EVENT STREAM
        KeyEventStreamCard(
            events = receiverState.eventHistory,
            totalPackets = receiverState.totalPacketsReceived,
            onClear = { viewModel.clearEventHistory() },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ReceiverHeaderCard(
    state: ReceiverState,
    onCopyIp: () -> Unit,
    onRestart: () -> Unit
) {
    val context = LocalContext.current
    Surface(
        color = SurfaceBar,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (state.isRunning) StatusSuccess else StatusError)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RECEIVER",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = KeyTextMain,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "v${com.kaius.keyboard.BuildConfig.VERSION_NAME}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (state.isRunning) "• Đang lắng nghe cổng ${state.port}" else "• Dừng",
                        fontSize = 11.sp,
                        color = KeyTextSubtle
                    )
                }

                IconButton(onClick = onRestart, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = KeyTextSubtle, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // IP & Device Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onCopyIp() }
                ) {
                    Text(
                        text = "${state.localIp}:${state.port}",
                        color = AccentPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = KeyTextSubtle,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Text(
                    text = state.deviceName,
                    color = KeyTextMuted,
                    fontSize = 12.sp
                )
            }

            if (state.connectedSenders.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Đã nhận từ: ${state.connectedSenders.joinToString(", ")}",
                    fontSize = 10.sp,
                    color = KeyTextSubtle,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // IME SHORTCUTS: Enable typing outside this app OR restore normal keyboard
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = AppBg,
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderSubtle),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            try {
                                context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                            } catch (_: Exception) {}
                        }
                ) {
                    Text(
                        "1. Bật Kboard (Cài đặt)",
                        color = AccentPrimary,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                    )
                }

                Surface(
                    color = AppBg,
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderSubtle),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            try {
                                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                                imm?.showInputMethodPicker()
                            } catch (_: Exception) {}
                        }
                ) {
                    Text(
                        "2. Chọn Kboard Nhận Phím",
                        color = StatusSuccess,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                    )
                }

                Surface(
                    color = AppBg,
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderSubtle),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            try {
                                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                                imm?.showInputMethodPicker()
                            } catch (_: Exception) {}
                        }
                ) {
                    Text(
                        "3. Trả Về Bàn Phím Thường",
                        color = KeyTextMain,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun VisualKeyDisplayCard(state: ReceiverState) {
    Surface(
        color = SurfaceElevated,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Modifiers Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                ModifierTag("CTRL", (state.activeModifiers.toInt() and (HidKeyCodes.MOD_LEFT_CTRL.toInt() or HidKeyCodes.MOD_RIGHT_CTRL.toInt())) != 0)
                Spacer(modifier = Modifier.width(6.dp))
                ModifierTag("SHIFT", (state.activeModifiers.toInt() and (HidKeyCodes.MOD_LEFT_SHIFT.toInt() or HidKeyCodes.MOD_RIGHT_SHIFT.toInt())) != 0)
                Spacer(modifier = Modifier.width(6.dp))
                ModifierTag("ALT", (state.activeModifiers.toInt() and (HidKeyCodes.MOD_LEFT_ALT.toInt() or HidKeyCodes.MOD_RIGHT_ALT.toInt())) != 0)
                Spacer(modifier = Modifier.width(6.dp))
                ModifierTag("WIN", (state.activeModifiers.toInt() and (HidKeyCodes.MOD_LEFT_GUI.toInt() or HidKeyCodes.MOD_RIGHT_GUI.toInt())) != 0)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Key Display
            val lastKeyName = state.lastEvent?.keyName ?: "—"
            val lastAction = state.lastEvent?.action?.uppercase() ?: "CHỜ PHÍM"

            AnimatedContent(
                targetState = lastKeyName to lastAction,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "keyAnimation"
            ) { (name, action) ->
                Box(
                    modifier = Modifier
                        .size(width = 160.dp, height = 54.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(SurfaceBar)
                        .border(1.dp, SurfaceBorderSubtle, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = KeyTextMain
                        )
                        Text(
                            text = action,
                            fontSize = 10.sp,
                            color = KeyTextSubtle,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModifierTag(label: String, isActive: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(if (isActive) KeyActiveBg else SurfaceBar)
            .border(1.dp, if (isActive) KeyActiveAccent else SurfaceBorderSubtle, RoundedCornerShape(3.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) KeyActiveAccent else KeyTextSubtle,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun AccumulatedTextCard(
    text: String,
    onCopyText: () -> Unit,
    onClear: () -> Unit
) {
    Surface(
        color = SurfaceBar,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VĂN BẢN ĐÃ GÕ",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = KeyTextSubtle,
                    fontFamily = FontFamily.Monospace
                )

                Row {
                    IconButton(onClick = onCopyText, modifier = Modifier.size(22.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = KeyTextMuted, modifier = Modifier.size(14.dp))
                    }
                    IconButton(onClick = onClear, modifier = Modifier.size(22.dp)) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = KeyTextSubtle, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(AppBg)
                    .border(1.dp, SurfaceBorderSubtle, RoundedCornerShape(4.dp))
                    .padding(6.dp)
            ) {
                if (text.isEmpty()) {
                    Text(
                        text = "Văn bản nhận được sẽ hiển thị ở đây...",
                        color = KeyTextSubtle,
                        fontSize = 12.sp
                    )
                } else {
                    Text(
                        text = text,
                        color = KeyTextMain,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun KeyEventStreamCard(
    events: List<com.kaius.keyboard.network.ReceivedKeyEvent>,
    totalPackets: Long,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SurfaceBar,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderSubtle),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SỰ KIỆN PHÍM ($totalPackets)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = KeyTextSubtle,
                    fontFamily = FontFamily.Monospace
                )
                IconButton(onClick = onClear, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = KeyTextSubtle, modifier = Modifier.size(14.dp))
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppBg)
                    .clip(RoundedCornerShape(4.dp))
                    .border(1.dp, SurfaceBorderSubtle, RoundedCornerShape(4.dp))
                    .padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(events, key = { it.id }) { ev ->
                    val actionColor = when (ev.action) {
                        "down" -> StatusSuccess
                        "up" -> KeyTextSubtle
                        else -> AccentPrimary
                    }
                    Text(
                        text = "[${ev.timestamp}] ${ev.action.uppercase().padEnd(4)} ${ev.keyName.padEnd(8)} mod=0x${(ev.modifiers.toInt() and 0xFF).toString(16).uppercase()}",
                        fontSize = 10.sp,
                        color = actionColor,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 13.sp
                    )
                }
            }
        }
    }
}
