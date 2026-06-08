package com.sujana.core.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

private val SujanaShapes = Shapes(
    extraSmall = RoundedCornerShape(Radii.chip),
    small = RoundedCornerShape(Radii.chip),
    medium = RoundedCornerShape(Radii.card),
    large = RoundedCornerShape(Radii.sheet),
    extraLarge = RoundedCornerShape(Radii.sheet),
)

@Composable
fun SujanaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color off by default — enterprise brand must stay consistent
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

    val statusColors = if (darkTheme) darkStatusColors() else lightStatusColors()

    CompositionLocalProvider(LocalStatusColors provides statusColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = SujanaTypography,
            shapes = SujanaShapes,
            content = content,
        )
    }
}
