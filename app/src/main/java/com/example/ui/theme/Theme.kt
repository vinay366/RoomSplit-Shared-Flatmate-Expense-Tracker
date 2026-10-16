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

private val DarkColorScheme = darkColorScheme(
  primary = EmeraldLight,
  onPrimary = Color(0xFF003822),
  primaryContainer = Color(0xFF064E3B),
  onPrimaryContainer = EmeraldContainer,
  secondary = Color(0xFF94A3B8),
  onSecondary = SlateBackgroundDark,
  secondaryContainer = SlateSurfaceVariantDark,
  onSecondaryContainer = Color(0xFFF1F5F9),
  tertiary = Color(0xFF38BDF8),
  background = SlateBackgroundDark,
  onBackground = Color(0xFFF8FAFC),
  surface = SlateCardDark,
  onSurface = Color(0xFFF8FAFC),
  surfaceVariant = SlateSurfaceVariantDark,
  onSurfaceVariant = Color(0xFFCBD5E1),
  outline = SlateBorderDark,
  outlineVariant = Color(0xFF1E293B)
)

private val LightColorScheme = lightColorScheme(
  primary = EmeraldPrimary,
  onPrimary = Color.White,
  primaryContainer = EmeraldContainer,
  onPrimaryContainer = OnEmeraldContainer,
  secondary = SlateMuted,
  onSecondary = Color.White,
  secondaryContainer = SlateSurfaceVariantLight,
  onSecondaryContainer = SlateDark,
  tertiary = Color(0xFF0284C7),
  background = SlateLightBg,
  onBackground = SlateDark,
  surface = SlateCardLight,
  onSurface = SlateDark,
  surfaceVariant = SlateSurfaceVariantLight,
  onSurfaceVariant = Color(0xFF475569),
  outline = SlateBorderLight,
  outlineVariant = Color(0xFFE2E8F0)
)

@Composable
fun RoomSplitTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
