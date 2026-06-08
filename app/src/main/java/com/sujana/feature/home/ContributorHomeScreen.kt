package com.sujana.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
fun ContributorHomeScreen(
    onLogout: () -> Unit,
    onNavigateToMyRequests: () -> Unit,
    onNavigateToCreateRequest: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("I-Sujana") })
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
                text  = "Contributor",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                text  = "What would you like to do?",
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
                onClick  = onNavigateToMyRequests,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("My Requests")
            }
            Spacer(Modifier.height(Spacing.xxxl))
            OutlinedButton(onClick = onLogout) { Text("Sign out") }
        }
    }
}
