package com.sujana.feature.home

import androidx.compose.runtime.Composable

@Composable
fun MpsAdminHomeScreen(onLogout: () -> Unit, onNavigateToNotifications: () -> Unit, unreadCount: Int = 0) {
    StubHomeScreen(role = "MPS Admin", onLogout = onLogout, onNavigateToNotifications = onNavigateToNotifications, unreadCount = unreadCount)
}
