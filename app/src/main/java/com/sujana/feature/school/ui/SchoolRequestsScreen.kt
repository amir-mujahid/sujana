package com.sujana.feature.school.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sujana.core.theme.Radii
import com.sujana.core.theme.Spacing
import com.sujana.core.theme.statusColors
import com.sujana.domain.model.PickupRequest
import com.sujana.feature.school.SchoolRequestsUiState
import com.sujana.feature.school.SchoolRequestsViewModel
import com.sujana.shared.RequestStatus
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolRequestsScreen(
    onNavigateUp: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    viewModel: SchoolRequestsViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("School Requests") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor   = MaterialTheme.colorScheme.onSecondary,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "New pickup request")
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (val state = uiState) {
                is SchoolRequestsUiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }

                is SchoolRequestsUiState.Error -> SchoolRequestsError(state.message, viewModel::load)

                is SchoolRequestsUiState.Content -> {
                    if (state.requests.isEmpty()) {
                        SchoolRequestsEmpty(onNavigateToCreate)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.lg),
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                        ) {
                            items(state.requests, key = { it.id }) { request ->
                                SchoolRequestCard(
                                    request = request,
                                    onClick = { onNavigateToDetail(request.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SchoolRequestCard(request: PickupRequest, onClick: () -> Unit) {
    val colors = MaterialTheme.statusColors
    val (containerColor, textColor) = when (request.status) {
        RequestStatus.PENDING   -> colors.warningContainer to colors.onWarningContainer
        RequestStatus.ASSIGNED  -> colors.warningContainer to colors.onWarningContainer
        RequestStatus.COLLECTED -> colors.neutralContainer  to colors.onNeutralContainer
        RequestStatus.DELIVERED -> colors.neutralContainer  to colors.onNeutralContainer
        RequestStatus.COMPLETED -> colors.successContainer  to colors.onSuccessContainer
        RequestStatus.CANCELLED -> colors.errorContainer    to colors.onErrorContainer
    }
    val statusLabel = request.status.name.lowercase().replaceFirstChar { it.uppercase() }

    Card(
        modifier  = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape     = RoundedCornerShape(Radii.card),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(Spacing.cardPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector        = Icons.Filled.School,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.secondary,
                modifier           = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(Spacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text     = request.pickupAddress.ifBlank { "—" },
                    style    = MaterialTheme.typography.bodyLarge,
                    color    = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (request.scheduledFor != null) {
                    Spacer(Modifier.height(Spacing.xs))
                    val formatted = runCatching {
                        OffsetDateTime.parse(request.scheduledFor)
                            .format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))
                    }.getOrElse { request.scheduledFor }
                    Text(
                        text  = "Scheduled: $formatted",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.width(Spacing.sm))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(Radii.chip))
                    .background(containerColor)
                    .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
            ) {
                Text(
                    text  = statusLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor,
                )
            }
        }
    }
}

@Composable
private fun SchoolRequestsEmpty(onNavigateToCreate: () -> Unit) {
    Column(
        modifier            = Modifier.fillMaxSize().padding(Spacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text  = "No pickup requests yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(Spacing.sm))
        Text(
            text  = "Tap + to schedule a waste collection from your school.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(Spacing.xl))
        Button(onClick = onNavigateToCreate) { Text("Schedule Pickup") }
    }
}

@Composable
private fun SchoolRequestsError(message: String, onRetry: () -> Unit) {
    Column(
        modifier            = Modifier.fillMaxSize().padding(Spacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text  = "Could not load requests",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error,
        )
        Spacer(Modifier.height(Spacing.sm))
        Text(
            text  = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(Spacing.xl))
        Button(onClick = onRetry) { Text("Retry") }
    }
}
