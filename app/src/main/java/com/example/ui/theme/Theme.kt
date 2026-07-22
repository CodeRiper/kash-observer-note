package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = KawaiiDarkPrimary,
    onPrimary = KawaiiDarkBackground,
    primaryContainer = KawaiiLavender,
    onPrimaryContainer = KawaiiDarkTextOnBg,
    secondary = KawaiiDarkSecondary,
    onSecondary = KawaiiDarkBackground,
    secondaryContainer = KawaiiPink,
    onSecondaryContainer = KawaiiDarkTextOnBg,
    tertiary = KawaiiDarkMint,
    onTertiary = KawaiiDarkBackground,
    tertiaryContainer = KawaiiDarkMintLight,
    onTertiaryContainer = KawaiiDarkTextOnBg,
    background = KawaiiDarkBackground,
    onBackground = KawaiiDarkTextOnBg,
    surface = KawaiiDarkSurface,
    onSurface = KawaiiDarkTextOnBg,
    outline = Color(0xFF433758)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = KawaiiLavender,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF5EFFF),
    onPrimaryContainer = Color(0xFF1E293B),
    secondary = KawaiiPink,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFD6E0),
    onSecondaryContainer = Color(0xFF1E293B),
    tertiary = KawaiiMint,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE0F2F1),
    onTertiaryContainer = Color(0xFF1E293B),
    background = Color(0xFFFAF5FF),
    onBackground = Color(0xFF1E293B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1E293B),
    outline = Color(0xFFF1E5FF)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Custom colors preferred over system dynamic colors for kawaii aesthetic consistency
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
