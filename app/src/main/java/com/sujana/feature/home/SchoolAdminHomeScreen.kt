package com.sujana.feature.home

import androidx.compose.runtime.Composable

@Composable
fun SchoolAdminHomeScreen(onLogout: () -> Unit, onNavigateToNotifications: () -> Unit, unreadCount: Int = 0) {
    StubHomeScreen(role = "School Admin", onLogout = onLogout, onNavigateToNotifications = onNavigateToNotifications, unreadCount = unreadCount)
}
