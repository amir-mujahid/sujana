package com.sujana.feature.home

import androidx.compose.runtime.Composable

@Composable
fun SuperAdminHomeScreen(onLogout: () -> Unit, onNavigateToNotifications: () -> Unit, unreadCount: Int = 0) {
    StubHomeScreen(role = "Super Admin", onLogout = onLogout, onNavigateToNotifications = onNavigateToNotifications, unreadCount = unreadCount)
}
