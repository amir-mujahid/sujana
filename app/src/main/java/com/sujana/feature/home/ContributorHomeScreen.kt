package com.sujana.feature.home

import androidx.compose.runtime.Composable

@Composable
fun ContributorHomeScreen(onLogout: () -> Unit) {
    StubHomeScreen(role = "Contributor", onLogout = onLogout)
}
