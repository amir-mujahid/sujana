package com.sujana.core.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ---- Palette ---------------------------------------------------------------
// Slate/navy neutrals
val Slate900 = Color(0xFF0F172A)
val Slate800 = Color(0xFF1E293B)
val Slate700 = Color(0xFF334155)
val Slate500 = Color(0xFF475569)  // muted text / onSurfaceVariant light
val Slate400 = Color(0xFF94A3B8)  // muted text dark
val Slate200 = Color(0xFFE2E8F0)  // border light / primary dark
val Slate100 = Color(0xFFF1F5F9)  // surfaceVariant light
val Slate050 = Color(0xFFF8FAFC)  // background light

// Near-black / near-white
val BackgroundDark = Color(0xFF020617)

// Emerald accent
val Emerald700 = Color(0xFF15803D)
val Emerald600 = Color(0xFF16A34A)
val Emerald500 = Color(0xFF22C55E)
val Emerald100 = Color(0xFFDCFCE7)
val Emerald900Dark = Color(0xFF14532D)

// Error / destructive
val Red600 = Color(0xFFDC2626)
val Red500 = Color(0xFFEF4444)
val Red100 = Color(0xFFFEE2E2)
val Red900Dark = Color(0xFF7F1D1D)

// Status — amber
val Amber600 = Color(0xFFD97706)
val Amber400 = Color(0xFFF59E0B)
val Amber100 = Color(0xFFFEF3C7)
val Amber900Dark = Color(0xFF78350F)

// ---- Material 3 colour schemes ---------------------------------------------

val LightColorScheme = lightColorScheme(
    primary = Slate900,
    onPrimary = Color.White,
    primaryContainer = Slate100,
    onPrimaryContainer = Slate900,

    secondary = Emerald600,
    onSecondary = Color.White,
    secondaryContainer = Emerald100,
    onSecondaryContainer = Emerald700,

    tertiary = Emerald600,
    onTertiary = Color.White,
    tertiaryContainer = Emerald100,
    onTertiaryContainer = Emerald700,

    background = Slate050,
    onBackground = Slate900,

    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate500,

    outline = Slate200,
    outlineVariant = Slate200,

    error = Red600,
    onError = Color.White,
    errorContainer = Red100,
    onErrorContainer = Red600,
)

val DarkColorScheme = darkColorScheme(
    primary = Slate200,
    onPrimary = Slate900,
    primaryContainer = Slate800,
    onPrimaryContainer = Slate200,

    secondary = Emerald500,
    onSecondary = Slate900,
    secondaryContainer = Emerald900Dark,
    onSecondaryContainer = Emerald500,

    tertiary = Emerald500,
    onTertiary = Slate900,
    tertiaryContainer = Emerald900Dark,
    onTertiaryContainer = Emerald500,

    background = BackgroundDark,
    onBackground = Slate050,

    surface = Slate900,
    onSurface = Slate050,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate400,

    outline = Slate700,
    outlineVariant = Slate700,

    error = Red500,
    onError = Color.White,
    errorContainer = Red900Dark,
    onErrorContainer = Red500,
)
