package com.sujana.feature.home

import androidx.compose.runtime.Composable

@Composable
fun RiderHomeScreen(onLogout: () -> Unit) {
    StubHomeScreen(role = "Rider", onLogout = onLogout)
}
