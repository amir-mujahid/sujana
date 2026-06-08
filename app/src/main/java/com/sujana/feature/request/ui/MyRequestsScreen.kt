package com.sujana.feature.request.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.sujana.domain.model.PickupRequest
import com.sujana.feature.request.MyRequestsUiState
import com.sujana.feature.request.MyRequestsViewModel
import com.sujana.shared.RequestStatus
import com.valentinilk.shimmer.shimmer
import com.sujana.core.theme.statusColors as statusTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRequestsScreen(
    onNavigateUp: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    viewModel: MyRequestsViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Requests") },
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
                Icon(Icons.Filled.Add, contentDescription = "New request")
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (val state = uiState) {
                is MyRequestsUiState.Loading  -> RequestListSkeleton()
                is MyRequestsUiState.Error    -> RequestListError(
                    message = state.message,
                    onRetry = viewModel::load,
                )
                is MyRequestsUiState.Content  -> {
                    if (state.requests.isEmpty()) {
                        RequestListEmpty(onNavigateToCreate)
                    } else {
                        LazyColumn(
                            modifier            = Modifier.fillMaxSize(),
                            contentPadding      = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = Spacing.lg,
                                vertical   = Spacing.lg,
                            ),
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                        ) {
                            items(state.requests, key = { it.id }) { request ->
                                RequestCard(
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
private fun RequestCard(request: PickupRequest, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape     = RoundedCornerShape(Radii.card),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.cardPadding),
            verticalAlignment   = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector  = Icons.Filled.LocationOn,
                contentDescription = null,
                tint         = MaterialTheme.colorScheme.secondary,
                modifier     = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(Spacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = request.pickupAddress.ifBlank { "—" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text  = "ID: …${request.id.takeLast(8)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(Spacing.sm))
            StatusChip(status = request.status)
        }
    }
}

@Composable
internal fun StatusChip(status: RequestStatus) {
    val colors = MaterialTheme.statusTheme
    val (containerColor, textColor, label) = when (status) {
        RequestStatus.PENDING   -> Triple(colors.warningContainer, colors.onWarningContainer, "Pending")
        RequestStatus.ASSIGNED  -> Triple(colors.warningContainer, colors.onWarningContainer, "Assigned")
        RequestStatus.COLLECTED -> Triple(colors.successContainer, colors.onSuccessContainer, "Collected")
        RequestStatus.DELIVERED -> Triple(colors.successContainer, colors.onSuccessContainer, "Delivered")
        RequestStatus.COMPLETED -> Triple(colors.successContainer, colors.onSuccessContainer, "Completed")
        RequestStatus.CANCELLED -> Triple(colors.errorContainer,   colors.onErrorContainer,   "Cancelled")
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(Radii.chip))
            .background(containerColor)
            .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
        )
    }
}

@Composable
private fun RequestListSkeleton() {
    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = androidx.compose.foundation.layout.PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        items(5) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(Radii.card))
                    .shimmer()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
        }
    }
}

@Composable
private fun RequestListEmpty(onNavigateToCreate: () -> Unit) {
    Column(
        modifier            = Modifier.fillMaxSize().padding(Spacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text  = "No requests yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(Spacing.sm))
        Text(
            text  = "Tap + to create your first pickup request.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(Spacing.xl))
        Button(onClick = onNavigateToCreate) { Text("New Request") }
    }
}

@Composable
private fun RequestListError(message: String, onRetry: () -> Unit) {
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
