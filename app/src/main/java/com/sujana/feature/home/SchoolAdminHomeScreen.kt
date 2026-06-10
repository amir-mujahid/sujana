package com.sujana.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.sujana.core.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolAdminHomeScreen(
    onLogout: () -> Unit,
    onNavigateToRequests: () -> Unit,
    onNavigateToCreateRequest: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    unreadCount: Int = 0,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("I-Sujana") },
                actions = {
                    IconButton(onClick = onNavigateToNotifications) {
                        BadgedBox(badge = {
                            if (unreadCount > 0) Badge { Text(if (unreadCount > 99) "99+" else "$unreadCount") }
                        }) {
                            Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text  = "School Admin",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                text  = "Manage waste collection requests for your school.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Spacing.xxxl))
            Button(
                onClick  = onNavigateToCreateRequest,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("New Pickup Request")
            }
            Spacer(Modifier.height(Spacing.sm))
            OutlinedButton(
                onClick  = onNavigateToRequests,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("View All Requests")
            }
            Spacer(Modifier.height(Spacing.xxxl))
            OutlinedButton(onClick = onLogout) { Text("Sign out") }
        }
    }
}
