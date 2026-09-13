package com.kaius.keyboard.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaius.keyboard.engine.HidKeyCodes
import com.kaius.keyboard.network.ReceiverState
import com.kaius.keyboard.ui.theme.AccentCyan
import com.kaius.keyboard.ui.theme.AccentGreen
import com.kaius.keyboard.ui.theme.AccentOrange
import com.kaius.keyboard.ui.theme.AccentRed
import com.kaius.keyboard.ui.theme.DarkBg
import com.kaius.keyboard.ui.theme.KeyBgModifierActive
import com.kaius.keyboard.ui.theme.KeyTextActive
import com.kaius.keyboard.ui.theme.KeyTextNormal
import com.kaius.keyboard.ui.theme.KeyTextSub
import com.kaius.keyboard.ui.theme.SurfaceBorder
import com.kaius.keyboard.ui.theme.SurfaceDark
import com.kaius.keyboard.ui.theme.SurfaceElevated

@Composable
fun ReceiverScreen(viewModel: KeyboardViewModel) {
    val context = LocalContext.current
    val receiverState by viewModel.receiverState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(12.dp)
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

        Spacer(modifier = Modifier.height(10.dp))

        // LIVE VISUAL KEY MONITOR
        VisualKeyDisplayCard(state = receiverState)

        Spacer(modifier = Modifier.height(10.dp))

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

        Spacer(modifier = Modifier.height(10.dp))

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
    Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (state.isRunning) AccentGreen else AccentRed)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "KAIUS RECEIVER",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGreen,
                        fontFamily = FontFamily.Monospace
                    )
                }

                IconButton(onClick = onRestart, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = KeyTextSub)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // IP & Port Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("IP thiết bị nhận (để nhập trên bàn phím):", color = KeyTextSub, fontSize = 11.sp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onCopyIp() }
                    ) {
                        Text(
                            text = "${state.localIp}:${state.port}",
                            color = AccentGreen,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = AccentGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Thiết bị:", color = KeyTextSub, fontSize = 11.sp)
                    Text(state.deviceName, color = KeyTextNormal, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            if (state.connectedSenders.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Devices, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Đã nhận từ: ${state.connectedSenders.joinToString(", ")}",
                        fontSize = 11.sp,
                        color = AccentCyan
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
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Modifiers Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                ModifierIndicator("CTRL", (state.activeModifiers.toInt() and (HidKeyCodes.MOD_LEFT_CTRL.toInt() or HidKeyCodes.MOD_RIGHT_CTRL.toInt())) != 0)
                Spacer(modifier = Modifier.width(6.dp))
                ModifierIndicator("SHIFT", (state.activeModifiers.toInt() and (HidKeyCodes.MOD_LEFT_SHIFT.toInt() or HidKeyCodes.MOD_RIGHT_SHIFT.toInt())) != 0)
                Spacer(modifier = Modifier.width(6.dp))
                ModifierIndicator("ALT", (state.activeModifiers.toInt() and (HidKeyCodes.MOD_LEFT_ALT.toInt() or HidKeyCodes.MOD_RIGHT_ALT.toInt())) != 0)
                Spacer(modifier = Modifier.width(6.dp))
                ModifierIndicator("WIN", (state.activeModifiers.toInt() and (HidKeyCodes.MOD_LEFT_GUI.toInt() or HidKeyCodes.MOD_RIGHT_GUI.toInt())) != 0)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Key Display Big Card
            val lastKeyName = state.lastEvent?.keyName ?: "CHƯA CÓ PHÍM"
            val lastAction = state.lastEvent?.action?.uppercase() ?: "IDLE"

            AnimatedContent(
                targetState = lastKeyName to lastAction,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "keyAnimation"
            ) { (name, action) ->
                Box(
                    modifier = Modifier
                        .size(width = 180.dp, height = 70.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceDark)
                        .border(1.dp, if (action == "DOWN" || action == "TAP") AccentGreen else SurfaceBorder, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = name,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (action == "DOWN" || action == "TAP") AccentGreen else KeyTextNormal
                        )
                        Text(
                            text = action,
                            fontSize = 11.sp,
                            color = if (action == "DOWN" || action == "TAP") AccentGreen else KeyTextSub,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModifierIndicator(label: String, isActive: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isActive) KeyBgModifierActive else SurfaceDark)
            .border(1.dp, if (isActive) AccentCyan else SurfaceBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) KeyTextActive else KeyTextSub
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
        color = SurfaceDark,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VĂN BẢN ĐÃ GÕ TRỰC TIẾP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentCyan,
                    fontFamily = FontFamily.Monospace
                )

                Row {
                    IconButton(onClick = onCopyText, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AccentCyan, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onClear, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = KeyTextSub, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(DarkBg)
                    .padding(8.dp)
            ) {
                if (text.isEmpty()) {
                    Text(
                        text = "Gõ phím từ máy bàn phím (Redmi 10) để xem nội dung hiển thị ở đây...",
                        color = KeyTextSub,
                        fontSize = 12.sp
                    )
                } else {
                    Text(
                        text = text,
                        color = KeyTextNormal,
                        fontSize = 14.sp,
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
        color = SurfaceDark,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NHẬT KÝ SỰ KIỆN PHÍM ($totalPackets packets)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentOrange,
                    fontFamily = FontFamily.Monospace
                )
                IconButton(onClick = onClear, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = KeyTextSub, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBg)
                    .padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(events, key = { it.id }) { ev ->
                    val color = when (ev.action) {
                        "down" -> AccentGreen
                        "up" -> KeyTextSub
                        else -> AccentCyan
                    }
                    Text(
                        text = "[${ev.timestamp}] ${ev.action.uppercase().padEnd(4)} key=${ev.keyName} (0x${(ev.keyCode.toInt() and 0xFF).toString(16).uppercase()}) mod=0x${(ev.modifiers.toInt() and 0xFF).toString(16).uppercase()} from ${ev.senderIp}",
                        fontSize = 10.sp,
                        color = color,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 13.sp
                    )
                }
            }
        }
    }
}
