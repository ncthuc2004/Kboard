package com.kaius.keyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kaius.keyboard.BuildConfig
import com.kaius.keyboard.ui.theme.AccentPrimary
import com.kaius.keyboard.ui.theme.AppBg
import com.kaius.keyboard.ui.theme.KeyActiveAccent
import com.kaius.keyboard.ui.theme.KeyActiveBg
import com.kaius.keyboard.ui.theme.KeyNormalBg
import com.kaius.keyboard.ui.theme.KeyTextMain
import com.kaius.keyboard.ui.theme.KeyTextMuted
import com.kaius.keyboard.ui.theme.KeyTextSubtle
import com.kaius.keyboard.ui.theme.KeyboardTheme
import com.kaius.keyboard.ui.theme.StatusSuccess
import com.kaius.keyboard.ui.theme.SurfaceBar
import com.kaius.keyboard.ui.theme.SurfaceBorder
import com.kaius.keyboard.ui.theme.SurfaceBorderSubtle
import com.kaius.keyboard.ui.theme.SurfaceElevated

private data class SettingsTabItem(
    val title: String,
    val icon: ImageVector
)

@Composable
fun KeyboardSettingsDialog(
    initialTab: Int = 0,
    isTelexEnabled: Boolean,
    onToggleTelex: () -> Unit,
    onSetTelex: (Boolean) -> Unit,
    rgbTheme: String,
    onSelectRgbTheme: (String) -> Unit,
    isReactiveGlow: Boolean,
    onToggleReactiveGlow: (Boolean) -> Unit,
    isHapticEnabled: Boolean,
    onToggleHaptic: (Boolean) -> Unit,
    hapticStrength: String,
    onSelectHapticStrength: (String) -> Unit,
    isSoundEnabled: Boolean,
    onToggleSound: (Boolean) -> Unit,
    repeatDelayMs: Long,
    onSelectRepeatDelay: (Long) -> Unit,
    repeatIntervalMs: Long,
    onSelectRepeatInterval: (Long) -> Unit,
    currentIp: String,
    currentPort: Int,
    onSaveWifi: (String, Int) -> Unit,
    onPing: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    var targetIp by remember { mutableStateOf(currentIp) }
    var targetPort by remember { mutableStateOf(currentPort.toString()) }

    val tabs = listOf(
        SettingsTabItem("Bo go & Ngon ngu", Icons.Default.Language),
        SettingsTabItem("Den nen RGB", Icons.Default.ColorLens),
        SettingsTabItem("Rung & Am thanh", Icons.Default.GraphicEq),
        SettingsTabItem("Toc do lap phim", Icons.Default.Speed),
        SettingsTabItem("Mang LAN & IP", Icons.Default.Wifi)
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = AppBg,
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.92f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // TOP BAR: Dialog Title + Version Badge + Close Button
                Surface(
                    color = SurfaceBar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "CAI DAT BAN PHIM",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = KeyTextMain,
                                letterSpacing = 1.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(SurfaceElevated)
                                    .border(1.dp, SurfaceBorderSubtle, RoundedCornerShape(3.dp))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AccentPrimary,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        IconButton(onClick = onDismiss, modifier = Modifier.size(26.dp)) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = KeyTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // MAIN BODY: Sidebar Navigation + Right Content Area
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    // LEFT SIDEBAR TABS
                    Surface(
                        color = SurfaceBar,
                        modifier = Modifier
                            .width(165.dp)
                            .fillMaxHeight(),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, SurfaceBorderSubtle)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            tabs.forEachIndexed { index, tab ->
                                val isSelected = selectedTab == index
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isSelected) KeyActiveBg else Color.Transparent)
                                        .clickable { selectedTab = index }
                                        .padding(horizontal = 8.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title,
                                        tint = if (isSelected) AccentPrimary else KeyTextMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(7.dp))
                                    Text(
                                        text = tab.title,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) KeyTextMain else KeyTextSubtle
                                    )
                                }
                            }
                        }
                    }

                    // RIGHT CONTENT PANE (Scrollable)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(AppBg)
                            .padding(10.dp)
                    ) {
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            when (selectedTab) {
                                0 -> TabLanguageAndTyping(
                                    isTelexEnabled = isTelexEnabled,
                                    onSetTelex = onSetTelex
                                )
                                1 -> TabRgbThemes(
                                    currentThemeId = rgbTheme,
                                    onSelectTheme = onSelectRgbTheme,
                                    isReactiveGlow = isReactiveGlow,
                                    onToggleReactiveGlow = onToggleReactiveGlow
                                )
                                2 -> TabHapticsAndSound(
                                    isHapticEnabled = isHapticEnabled,
                                    onToggleHaptic = onToggleHaptic,
                                    hapticStrength = hapticStrength,
                                    onSelectStrength = onSelectHapticStrength,
                                    isSoundEnabled = isSoundEnabled,
                                    onToggleSound = onToggleSound
                                )
                                3 -> TabKeyRepeat(
                                    repeatDelayMs = repeatDelayMs,
                                    onSelectRepeatDelay = onSelectRepeatDelay,
                                    repeatIntervalMs = repeatIntervalMs,
                                    onSelectRepeatInterval = onSelectRepeatInterval
                                )
                                4 -> TabNetworkLan(
                                    targetIp = targetIp,
                                    onTargetIpChange = { targetIp = it },
                                    targetPort = targetPort,
                                    onTargetPortChange = { targetPort = it },
                                    onSave = {
                                        val portInt = targetPort.toIntOrNull() ?: 8964
                                        onSaveWifi(targetIp.trim(), portInt)
                                    },
                                    onPing = onPing
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// TAB 0: LANGUAGE & TELEX ENGINE
// ---------------------------------------------------------
@Composable
private fun TabLanguageAndTyping(
    isTelexEnabled: Boolean,
    onSetTelex: (Boolean) -> Unit
) {
    Text(
        text = "BO GO VA NGON NGU NHAP LIEU",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = KeyTextMuted,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 0.5.sp
    )

    // LANGUAGE PICKER CARDS
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // VIETNAMESE CARD
        Surface(
            color = if (isTelexEnabled) KeyActiveBg else SurfaceElevated,
            shape = RoundedCornerShape(6.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isTelexEnabled) AccentPrimary else SurfaceBorderSubtle),
            modifier = Modifier
                .weight(1f)
                .clickable { onSetTelex(true) }
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tieng Viet (VI)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isTelexEnabled) AccentPrimary else KeyTextMain
                    )
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(if (isTelexEnabled) AccentPrimary else Color.Transparent)
                            .border(1.dp, if (isTelexEnabled) AccentPrimary else SurfaceBorder, CircleShape)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Bo go Telex tich hop. Tu dong bo dau (s, f, r, x, j, w, aa, ee, oo, dd). Ho tro go truc tiep khong can bo go ngoai.",
                    fontSize = 9.sp,
                    color = KeyTextSubtle,
                    lineHeight = 13.sp
                )
            }
        }

        // ENGLISH CARD
        Surface(
            color = if (!isTelexEnabled) KeyActiveBg else SurfaceElevated,
            shape = RoundedCornerShape(6.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (!isTelexEnabled) AccentPrimary else SurfaceBorderSubtle),
            modifier = Modifier
                .weight(1f)
                .clickable { onSetTelex(false) }
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "English (EN)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!isTelexEnabled) AccentPrimary else KeyTextMain
                    )
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(if (!isTelexEnabled) AccentPrimary else Color.Transparent)
                            .border(1.dp, if (!isTelexEnabled) AccentPrimary else SurfaceBorder, CircleShape)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Che do truyen ky tu goc tieu chuan quoc te. Khong qua engine Telex, phu hop go code, game va go van ban tieng Anh.",
                    fontSize = 9.sp,
                    color = KeyTextSubtle,
                    lineHeight = 13.sp
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // TELEX REALTIME SYNC NOTE
    Surface(
        color = SurfaceElevated,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Cong tac Telex nhanh tren ban phim",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = KeyTextMain
                )
                Text(
                    text = "Nut [ VI ] / [ EN ] tren thanh Macro Bar cho phep ban chuyen nhanh bang 1 cham va tu dong dong bo toi may nhan.",
                    fontSize = 9.sp,
                    color = KeyTextSubtle
                )
            }
            Switch(
                checked = isTelexEnabled,
                onCheckedChange = { onSetTelex(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AccentPrimary,
                    checkedTrackColor = KeyActiveBg,
                    uncheckedThumbColor = KeyTextSubtle,
                    uncheckedTrackColor = SurfaceBar
                )
            )
        }
    }
}

// ---------------------------------------------------------
// TAB 1: RGB BACKLIGHT & THEMES
// ---------------------------------------------------------
@Composable
private fun TabRgbThemes(
    currentThemeId: String,
    onSelectTheme: (String) -> Unit,
    isReactiveGlow: Boolean,
    onToggleReactiveGlow: (Boolean) -> Unit
) {
    Text(
        text = "CHU DE DEN NEN RGB CHO BAN PHIM",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = KeyTextMuted,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 0.5.sp
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        KeyboardTheme.values().forEach { theme ->
            val isSelected = currentThemeId == theme.id
            Surface(
                color = if (isSelected) KeyActiveBg else SurfaceElevated,
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) theme.primaryColor else SurfaceBorderSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectTheme(theme.id) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(theme.primaryColor)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = theme.title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) KeyTextMain else KeyTextMain
                        )
                        Text(
                            text = theme.subtitle,
                            fontSize = 9.sp,
                            color = KeyTextSubtle
                        )
                    }
                    if (isSelected) {
                        Text(
                            text = "DANG CHON",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.primaryColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // REACTIVE KEYPRESS GLOW
    Surface(
        color = SurfaceElevated,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Phat sang vien phim khi cham (Reactive Key Glow)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = KeyTextMain
                )
                Text(
                    text = "Khi nhan phim, vien phim bung sang ruc ro theo mau cua chu de LED tao cam giac co hoc chan thuc.",
                    fontSize = 9.sp,
                    color = KeyTextSubtle
                )
            }
            Switch(
                checked = isReactiveGlow,
                onCheckedChange = { onToggleReactiveGlow(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AccentPrimary,
                    checkedTrackColor = KeyActiveBg,
                    uncheckedThumbColor = KeyTextSubtle,
                    uncheckedTrackColor = SurfaceBar
                )
            )
        }
    }
}

// ---------------------------------------------------------
// TAB 2: HAPTICS & SOUND
// ---------------------------------------------------------
@Composable
private fun TabHapticsAndSound(
    isHapticEnabled: Boolean,
    onToggleHaptic: (Boolean) -> Unit,
    hapticStrength: String,
    onSelectStrength: (String) -> Unit,
    isSoundEnabled: Boolean,
    onToggleSound: (Boolean) -> Unit
) {
    Text(
        text = "PHAN HOI RUNG XUC GIAC & AM THANH",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = KeyTextMuted,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 0.5.sp
    )

    // HAPTIC TOGGLE
    Surface(
        color = SurfaceElevated,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Rung xuc giac khi go phim (Haptic Feedback)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = KeyTextMain)
                    Text("Mo phong cam giac switch co go nhan bang bo rung smartphone.", fontSize = 9.sp, color = KeyTextSubtle)
                }
                Switch(
                    checked = isHapticEnabled,
                    onCheckedChange = { onToggleHaptic(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AccentPrimary,
                        checkedTrackColor = KeyActiveBg,
                        uncheckedThumbColor = KeyTextSubtle,
                        uncheckedTrackColor = SurfaceBar
                    )
                )
            }

            if (isHapticEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Muc do rung:", fontSize = 10.sp, color = KeyTextMuted, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("LIGHT" to "Nhe (12ms)", "MEDIUM" to "Vua (22ms)", "STRONG" to "Manh (35ms)").forEach { (key, label) ->
                        val isSelected = hapticStrength == key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) KeyActiveBg else SurfaceBar)
                                .border(1.dp, if (isSelected) AccentPrimary else SurfaceBorderSubtle, RoundedCornerShape(4.dp))
                                .clickable { onSelectStrength(key) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) AccentPrimary else KeyTextMain
                            )
                        }
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // SOUND TOGGLE
    Surface(
        color = SurfaceElevated,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Am thanh go phim (Key Click Sound)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = KeyTextMain)
                Text("Phat am thanh go nhe nhang khi nhan phim.", fontSize = 9.sp, color = KeyTextSubtle)
            }
            Switch(
                checked = isSoundEnabled,
                onCheckedChange = { onToggleSound(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AccentPrimary,
                    checkedTrackColor = KeyActiveBg,
                    uncheckedThumbColor = KeyTextSubtle,
                    uncheckedTrackColor = SurfaceBar
                )
            )
        }
    }
}

// ---------------------------------------------------------
// TAB 3: KEY REPEAT SPEED
// ---------------------------------------------------------
@Composable
private fun TabKeyRepeat(
    repeatDelayMs: Long,
    onSelectRepeatDelay: (Long) -> Unit,
    repeatIntervalMs: Long,
    onSelectRepeatInterval: (Long) -> Unit
) {
    Text(
        text = "TOC DO LAP PHIM CO HOC (HARDWARE KEY REPEAT)",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = KeyTextMuted,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 0.5.sp
    )

    Surface(
        color = SurfaceElevated,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Do tre truoc khi bat dau lap phim (Initial Delay):", fontSize = 10.sp, color = KeyTextMuted)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(250L to "Nhanh (250ms)", 380L to "Chuan (380ms)", 500L to "Cham (500ms)").forEach { (ms, label) ->
                    val isSelected = repeatDelayMs == ms
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) KeyActiveBg else SurfaceBar)
                            .border(1.dp, if (isSelected) AccentPrimary else SurfaceBorderSubtle, RoundedCornerShape(4.dp))
                            .clickable { onSelectRepeatDelay(ms) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) AccentPrimary else KeyTextMain
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text("Toc do lap ky tu (Repeat Interval):", fontSize = 10.sp, color = KeyTextMuted)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(25L to "Sieu toc (25ms)", 45L to "Chuan (45ms)", 70L to "Em diu (70ms)").forEach { (ms, label) ->
                    val isSelected = repeatIntervalMs == ms
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) KeyActiveBg else SurfaceBar)
                            .border(1.dp, if (isSelected) AccentPrimary else SurfaceBorderSubtle, RoundedCornerShape(4.dp))
                            .clickable { onSelectRepeatInterval(ms) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) AccentPrimary else KeyTextMain
                        )
                    }
                }
            }

            Text(
                text = "Che do lap phim hoat dong giong het ban phim co tren PC khi ban nhan giu phim.",
                fontSize = 8.5.sp,
                color = KeyTextSubtle
            )
        }
    }
}

// ---------------------------------------------------------
// TAB 4: NETWORK & WI-FI LAN
// ---------------------------------------------------------
@Composable
private fun TabNetworkLan(
    targetIp: String,
    onTargetIpChange: (String) -> Unit,
    targetPort: String,
    onTargetPortChange: (String) -> Unit,
    onSave: () -> Unit,
    onPing: () -> Unit
) {
    Text(
        text = "KET NOI MANG WI-FI LAN & MAY NHAN",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = KeyTextMuted,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 0.5.sp
    )

    Surface(
        color = SurfaceElevated,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Dia chi IP may nhan (PC / Laptop / May Android khac):", fontSize = 10.sp, color = KeyTextMuted)

            Text(
                text = "Goi y: Dung Hotspot Gateway (192.168.43.1)",
                color = AccentPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clip(RoundedCornerShape(2.dp))
                    .clickable { onTargetIpChange("192.168.43.1") }
                    .padding(vertical = 2.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = targetIp,
                    onValueChange = onTargetIpChange,
                    label = { Text("IP Dich", fontSize = 10.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = KeyTextMain,
                        unfocusedTextColor = KeyTextMain,
                        focusedBorderColor = AccentPrimary,
                        unfocusedBorderColor = SurfaceBorderSubtle
                    ),
                    singleLine = true,
                    modifier = Modifier.weight(0.7f)
                )

                OutlinedTextField(
                    value = targetPort,
                    onValueChange = onTargetPortChange,
                    label = { Text("Port", fontSize = 10.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = KeyTextMain,
                        unfocusedTextColor = KeyTextMain,
                        focusedBorderColor = AccentPrimary,
                        unfocusedBorderColor = SurfaceBorderSubtle
                    ),
                    singleLine = true,
                    modifier = Modifier.weight(0.3f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onSave,
                    colors = ButtonDefaults.buttonColors(containerColor = KeyActiveBg, contentColor = KeyActiveAccent),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.weight(1f).height(32.dp)
                ) {
                    Text("Luu IP & Port", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onPing,
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceBar, contentColor = StatusSuccess),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.weight(1f).height(32.dp)
                ) {
                    Text("Gui goi Ping kiem tra", fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
