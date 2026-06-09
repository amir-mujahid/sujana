package com.sujana.feature.notification.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sujana.domain.model.NotificationPref
import com.sujana.feature.notification.NotificationPrefsUiState
import com.sujana.feature.notification.NotificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationPrefsScreen(
    onNavigateUp: () -> Unit,
    viewModel: NotificationViewModel,
) {
    val state by viewModel.prefsState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadPrefs() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (val s = state) {
                is NotificationPrefsUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is NotificationPrefsUiState.Error -> Text(
                    text = s.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                )
                is NotificationPrefsUiState.Content -> PrefsList(
                    prefs = s.prefs,
                    onToggle = { category, muted -> viewModel.togglePref(category, muted) },
                )
            }
        }
    }
}

@Composable
private fun PrefsList(
    prefs: List<NotificationPref>,
    onToggle: (String, Boolean) -> Unit,
) {
    LazyColumn {
        item {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    "Mute categories you don't want notifications for.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
            }
        }
        items(prefs, key = { it.category }) { pref ->
            PrefRow(pref = pref, onToggle = { onToggle(pref.category, it) })
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        }
    }
}

@Composable
private fun PrefRow(
    pref: NotificationPref,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = categoryLabel(pref.category),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = categoryDescription(pref.category),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = !pref.muted,
            onCheckedChange = { enabled -> onToggle(!enabled) },
        )
    }
}

private fun categoryLabel(category: String): String = when (category) {
    "REQUEST_UPDATE"    -> "Pickup requests"
    "ASSIGNMENT_UPDATE" -> "Assignments"
    "SCHOOL_COLLECTION" -> "School collections"
    "DISPATCH_ALERT"    -> "Dispatch alerts"
    "ADMIN_SUMMARY"     -> "Admin summaries"
    "SYSTEM"            -> "System alerts"
    else -> category
}

private fun categoryDescription(category: String): String = when (category) {
    "REQUEST_UPDATE"    -> "Status changes to your pickup requests"
    "ASSIGNMENT_UPDATE" -> "New or updated rider assignments"
    "SCHOOL_COLLECTION" -> "Incoming collection schedules and changes"
    "DISPATCH_ALERT"    -> "New requests, SLA alerts, rider issues"
    "ADMIN_SUMMARY"     -> "Operational summaries and escalations"
    "SYSTEM"            -> "System and tenant-level alerts"
    else -> ""
}
