package com.sujana.feature.home

import androidx.compose.runtime.Composable

@Composable
fun SchoolStaffHomeScreen(onLogout: () -> Unit, onNavigateToNotifications: () -> Unit, unreadCount: Int = 0) {
    StubHomeScreen(role = "School Staff", onLogout = onLogout, onNavigateToNotifications = onNavigateToNotifications, unreadCount = unreadCount)
}
