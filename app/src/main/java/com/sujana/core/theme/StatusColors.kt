package com.sujana.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class StatusColors(
    val success: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val error: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    val neutral: Color,
    val neutralContainer: Color,
    val onNeutralContainer: Color,
)

fun lightStatusColors() = StatusColors(
    success = Emerald600,
    successContainer = Emerald100,
    onSuccessContainer = Emerald700,
    warning = Amber600,
    warningContainer = Amber100,
    onWarningContainer = Amber600,
    error = Red600,
    errorContainer = Red100,
    onErrorContainer = Red600,
    neutral = Slate500,
    neutralContainer = Slate100,
    onNeutralContainer = Slate500,
)

fun darkStatusColors() = StatusColors(
    success = Emerald500,
    successContainer = Emerald900Dark,
    onSuccessContainer = Emerald500,
    warning = Amber400,
    warningContainer = Amber900Dark,
    onWarningContainer = Amber400,
    error = Red500,
    errorContainer = Red900Dark,
    onErrorContainer = Red500,
    neutral = Slate400,
    neutralContainer = Slate800,
    onNeutralContainer = Slate400,
)

val LocalStatusColors = staticCompositionLocalOf { lightStatusColors() }

val MaterialTheme.statusColors: StatusColors
    @Composable
    @ReadOnlyComposable
    get() = LocalStatusColors.current
