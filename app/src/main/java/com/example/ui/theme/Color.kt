package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme

@Composable
fun isAppInDarkTheme(): Boolean {
    return MaterialTheme.colorScheme.background == KawaiiDarkBackground
}

// Clean Minimalism / Kawaii Saturated Pastel Palette
val KawaiiLavender = Color(0xFF9370DB) // Soft vibrant pastel purple / lavender
val KawaiiLavenderLight: Color
    @Composable
    get() = if (isAppInDarkTheme()) Color(0xFF302340) else Color(0xFFF3EAFF)

val KawaiiPink = Color(0xFFFF728A) // Soft vibrant sakura pink
val KawaiiPinkLight: Color
    @Composable
    get() = if (isAppInDarkTheme()) Color(0xFF3F2131) else Color(0xFFFFDFE8)

val KawaiiCreamBg: Color
    @Composable
    get() = if (isAppInDarkTheme()) Color(0xFF150F1E) else Color(0xFFFAF5FF)

val KawaiiCreamCardBg: Color
    @Composable
    get() = if (isAppInDarkTheme()) Color(0xFF231A2E) else Color(0xFFFFFFFF)

val KawaiiCreamSurface: Color
    @Composable
    get() = KawaiiCreamCardBg

val KawaiiMint = Color(0xFF10B981) // Soft vibrant mint
val KawaiiMintLight: Color
    @Composable
    get() = if (isAppInDarkTheme()) Color(0xFF1E3A36) else Color(0xFFE2F6F3)

val KawaiiDarkText: Color
    @Composable
    get() = if (isAppInDarkTheme()) Color(0xFFF6F0FF) else Color(0xFF2E243A)

val KawaiiSoftBorder: Color
    @Composable
    get() = if (isAppInDarkTheme()) Color(0xFF3E3150) else Color(0xFFE8D7FF)

// Custom Flat Shadow Highlight Colors
val ShadowPink: Color
    @Composable
    get() = if (isAppInDarkTheme()) Color(0xFF291A25) else Color(0xFFFDE2E4)

val ShadowYellow: Color
    @Composable
    get() = if (isAppInDarkTheme()) Color(0xFF2A2318) else Color(0xFFF6E05E)

val StickyYellowBg: Color
    @Composable
    get() = if (isAppInDarkTheme()) Color(0xFF32281C) else Color(0xFFFFF9E6)

// Dark Theme equivalents (softer, dreamy night pastel style)
val KawaiiDarkPrimary = Color(0xFFC7B8E3)
val KawaiiDarkSecondary = Color(0xFFF3C5DC)
val KawaiiDarkBackground = Color(0xFF150F1E)
val KawaiiDarkSurface = Color(0xFF231A2E)
val KawaiiDarkTextOnBg = Color(0xFFF6F0FF)
val KawaiiDarkMint = Color(0xFF76C4BB)
val KawaiiDarkMintLight = Color(0xFF1E3A36)



