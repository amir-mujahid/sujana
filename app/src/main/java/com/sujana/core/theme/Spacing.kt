package com.sujana.core.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object Spacing {
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 32.dp
    val xxxl: Dp = 48.dp

    val screenPadding: Dp = lg    // 16dp — standard screen edge padding
    val sectionGap: Dp = xl       // 24dp — gap between sections
    val cardPadding: Dp = lg      // 16dp — inner padding for cards
}

// Corner radii — named per use, not by size
object Radii {
    val chip = 8.dp       // chips, text inputs
    val card = 12.dp      // cards, list items
    val sheet = 16.dp     // bottom sheets, dialogs
}
