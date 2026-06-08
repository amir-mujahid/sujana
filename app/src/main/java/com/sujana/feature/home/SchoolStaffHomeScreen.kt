package com.sujana.feature.home

import androidx.compose.runtime.Composable

@Composable
fun SchoolStaffHomeScreen(onLogout: () -> Unit) {
    StubHomeScreen(role = "School Staff", onLogout = onLogout)
}
