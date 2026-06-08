package com.sujana.feature.dispatch.ui

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.sujana.domain.model.Assignment
import com.sujana.domain.model.PickupRequest
import com.sujana.domain.model.UserProfile
import com.sujana.feature.dispatch.DispatchUiState
import com.sujana.feature.dispatch.DispatchViewModel
import com.sujana.shared.AssignmentStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DispatchQueueScreen(
    onNavigateUp: () -> Unit,
    viewModel: DispatchViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dispatch Queue") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (val state = uiState) {
                is DispatchUiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }

                is DispatchUiState.Error -> DispatchError(state.message, viewModel::load)

                is DispatchUiState.Content -> {
                    DispatchContent(state = state, viewModel = viewModel)
                    if (state.riderPickerRequest != null) {
                        RiderPickerDialog(
                            request   = state.riderPickerRequest,
                            riders    = state.riders,
                            isAssigning = state.isAssigning,
                            error     = state.assignError,
                            onAssign  = { rider -> viewModel.assign(state.riderPickerRequest, rider) },
                            onDismiss = viewModel::dismissRiderPicker,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DispatchContent(state: DispatchUiState.Content, viewModel: DispatchViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        item {
            Text(
                text  = "Pending (${state.pendingRequests.size})",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(Spacing.sm))
        }

        if (state.pendingRequests.isEmpty()) {
            item {
                Text(
                    text  = "No pending requests.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = Spacing.sm),
                )
            }
        } else {
            items(state.pendingRequests, key = { it.id }) { request ->
                PendingRequestCard(
                    request  = request,
                    onAssign = { viewModel.openRiderPicker(request) },
                )
            }
        }

        if (state.activeAssignments.isNotEmpty()) {
            item {
                Spacer(Modifier.height(Spacing.xl))
                HorizontalDivider()
                Spacer(Modifier.height(Spacing.xl))
                Text(
                    text  = "Active (${state.activeAssignments.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(Spacing.sm))
            }
            items(state.activeAssignments, key = { it.id }) { assignment ->
                ActiveAssignmentCard(assignment = assignment)
            }
        }
    }
}

@Composable
private fun PendingRequestCard(request: PickupRequest, onAssign: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(Radii.card),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(Spacing.cardPadding),
            verticalAlignment   = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector        = Icons.Filled.LocationOn,
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
                if (request.dropoffSchoolName != null) {
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        text  = "→ ${request.dropoffSchoolName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(Modifier.width(Spacing.sm))
            Button(onClick = onAssign) { Text("Assign") }
        }
    }
}

@Composable
private fun ActiveAssignmentCard(assignment: Assignment) {
    val colors = MaterialTheme.statusColors
    val (containerColor, textColor) = when (assignment.status) {
        AssignmentStatus.ASSIGNED,
        AssignmentStatus.ACCEPTED  -> Pair(colors.warningContainer, colors.onWarningContainer)
        AssignmentStatus.COLLECTED,
        AssignmentStatus.DELIVERED -> Pair(colors.neutralContainer,  colors.onNeutralContainer)
        AssignmentStatus.COMPLETED -> Pair(colors.successContainer, colors.onSuccessContainer)
        AssignmentStatus.CANCELLED -> Pair(colors.errorContainer,   colors.onErrorContainer)
    }
    val statusLabel = assignment.status.name.lowercase().replaceFirstChar { it.uppercase() }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(Radii.card),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(Spacing.cardPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector        = Icons.Filled.Person,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(Spacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text     = assignment.request.pickupAddress.ifBlank { "—" },
                    style    = MaterialTheme.typography.bodyLarge,
                    color    = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text  = "Rider: ${assignment.riderName ?: assignment.riderId.takeLast(8)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
private fun RiderPickerDialog(
    request: PickupRequest,
    riders: List<UserProfile>,
    isAssigning: Boolean,
    error: String?,
    onAssign: (UserProfile) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isAssigning) onDismiss() },
        shape = RoundedCornerShape(Radii.sheet),
        title = { Text("Assign Rider") },
        text = {
            Column {
                Text(
                    text  = request.pickupAddress.ifBlank { request.id.takeLast(8) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(Spacing.lg))
                if (riders.isEmpty()) {
                    Text(
                        text  = "No riders available.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    riders.forEach { rider ->
                        TextButton(
                            onClick  = { if (!isAssigning) onAssign(rider) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector        = Icons.Filled.Person,
                                    contentDescription = null,
                                    modifier           = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(Spacing.sm))
                                Text(rider.name, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
                if (error != null) {
                    Spacer(Modifier.height(Spacing.sm))
                    Text(
                        text  = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                if (isAssigning) {
                    Spacer(Modifier.height(Spacing.sm))
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(onClick = onDismiss, enabled = !isAssigning) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun DispatchError(message: String, onRetry: () -> Unit) {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text  = "Could not load queue",
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
