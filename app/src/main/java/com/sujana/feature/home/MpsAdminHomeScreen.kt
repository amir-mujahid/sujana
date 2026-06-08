package com.sujana.feature.home

import androidx.compose.runtime.Composable

@Composable
fun MpsAdminHomeScreen(onLogout: () -> Unit) {
    StubHomeScreen(role = "MPS Admin", onLogout = onLogout)
}
