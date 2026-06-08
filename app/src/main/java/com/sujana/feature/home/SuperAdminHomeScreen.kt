package com.sujana.feature.home

import androidx.compose.runtime.Composable

@Composable
fun SuperAdminHomeScreen(onLogout: () -> Unit) {
    StubHomeScreen(role = "Super Admin", onLogout = onLogout)
}
