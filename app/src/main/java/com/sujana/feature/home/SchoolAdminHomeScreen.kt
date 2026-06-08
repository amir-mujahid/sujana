package com.sujana.feature.home

import androidx.compose.runtime.Composable

@Composable
fun SchoolAdminHomeScreen(onLogout: () -> Unit) {
    StubHomeScreen(role = "School Admin", onLogout = onLogout)
}
