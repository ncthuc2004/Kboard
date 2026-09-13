package com.kaius.keyboard.ui.theme

import androidx.compose.ui.graphics.Color

enum class KeyboardTheme(
    val id: String,
    val title: String,
    val subtitle: String,
    val primaryColor: Color
) {
    DARK_INDUSTRIAL(
        id = "DARK_INDUSTRIAL",
        title = "Dark Industrial",
        subtitle = "Co khi toi gian, tram tinh khong moi mat",
        primaryColor = Color(0xFF38BDF8)
    ),
    CYBERPUNK_NEON(
        id = "CYBERPUNK_NEON",
        title = "Cyberpunk Neon",
        subtitle = "Vien phat sang Cyan & Magenta sac net",
        primaryColor = Color(0xFF00F0FF)
    ),
    MATRIX_GREEN(
        id = "MATRIX_GREEN",
        title = "Matrix Green",
        subtitle = "Xanh phosphor ma tran cong nghe dien tu",
        primaryColor = Color(0xFF00FF66)
    ),
    RGB_CHROMA(
        id = "RGB_CHROMA",
        title = "RGB Chroma Wave",
        subtitle = "Song cau vong da sac bien thien uyen chuyen",
        primaryColor = Color(0xFFA855F7)
    ),
    RETRO_AMBER(
        id = "RETRO_AMBER",
        title = "Retro Amber",
        subtitle = "Den cam co dien phím may tinh thap nien 80",
        primaryColor = Color(0xFFFFB000)
    )
}

data class KeyVisualTheme(
    val normalBg: Color,
    val pressedBg: Color,
    val normalBorder: Color,
    val pressedBorder: Color,
    val activeAccent: Color,
    val activeBg: Color,
    val specialBg: Color,
    val textColor: Color
)

fun resolveKeyVisualTheme(themeId: String, chromaColor: Color = Color(0xFF38BDF8)): KeyVisualTheme {
    return when (themeId) {
        KeyboardTheme.CYBERPUNK_NEON.id -> KeyVisualTheme(
            normalBg = Color(0xFF141828),
            pressedBg = Color(0xFF281E3C),
            normalBorder = Color(0x4400F0FF),
            pressedBorder = Color(0xFF00F0FF),
            activeAccent = Color(0xFFFF007F),
            activeBg = Color(0xFF4A0E35),
            specialBg = Color(0xFF0E121E),
            textColor = Color(0xFFE2E8F0)
        )
        KeyboardTheme.MATRIX_GREEN.id -> KeyVisualTheme(
            normalBg = Color(0xFF0E1912),
            pressedBg = Color(0xFF14331E),
            normalBorder = Color(0x4400FF66),
            pressedBorder = Color(0xFF00FF66),
            activeAccent = Color(0xFF00FF66),
            activeBg = Color(0xFF0B3819),
            specialBg = Color(0xFF08120B),
            textColor = Color(0xFFDCFCE7)
        )
        KeyboardTheme.RGB_CHROMA.id -> KeyVisualTheme(
            normalBg = Color(0xFF151824),
            pressedBg = Color(0xFF262038),
            normalBorder = chromaColor.copy(alpha = 0.35f),
            pressedBorder = chromaColor,
            activeAccent = chromaColor,
            activeBg = chromaColor.copy(alpha = 0.25f),
            specialBg = Color(0xFF0F121C),
            textColor = Color(0xFFF8FAFC)
        )
        KeyboardTheme.RETRO_AMBER.id -> KeyVisualTheme(
            normalBg = Color(0xFF1B1610),
            pressedBg = Color(0xFF322413),
            normalBorder = Color(0x44FFB000),
            pressedBorder = Color(0xFFFFB000),
            activeAccent = Color(0xFFFFC043),
            activeBg = Color(0xFF422806),
            specialBg = Color(0xFF130E09),
            textColor = Color(0xFFFEF3C7)
        )
        else -> KeyVisualTheme(
            normalBg = KeyNormalBg,
            pressedBg = KeyNormalPressed,
            normalBorder = SurfaceBorderSubtle,
            pressedBorder = AccentPrimary,
            activeAccent = AccentPrimary,
            activeBg = KeyActiveBg,
            specialBg = KeySpecialBg,
            textColor = KeyTextMain
        )
    }
}
