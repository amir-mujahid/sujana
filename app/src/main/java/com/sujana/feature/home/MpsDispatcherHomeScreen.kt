package com.sujana.feature.home

import androidx.compose.runtime.Composable

@Composable
fun MpsDispatcherHomeScreen(onLogout: () -> Unit) {
    StubHomeScreen(role = "MPS Dispatcher", onLogout = onLogout)
}
