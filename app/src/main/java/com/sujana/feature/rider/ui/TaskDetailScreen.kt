package com.sujana.feature.rider.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import com.sujana.core.theme.Radii
import com.sujana.core.theme.Spacing
import com.sujana.domain.model.Assignment
import com.sujana.feature.rider.TaskDetailUiState
import com.sujana.feature.rider.TaskDetailViewModel
import com.sujana.shared.AssignmentStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    onNavigateUp: () -> Unit,
    viewModel: TaskDetailViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Detail") },
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
                is TaskDetailUiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }

                is TaskDetailUiState.Error -> TaskDetailError(state.message, viewModel::load)

                is TaskDetailUiState.Content -> TaskDetailContent(
                    state      = state,
                    onTransition = viewModel::transition,
                )
            }
        }
    }
}

@Composable
private fun TaskDetailContent(
    state: TaskDetailUiState.Content,
    onTransition: (AssignmentStatus) -> Unit,
) {
    val assignment = state.assignment
    val request = assignment.request

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg),
    ) {
        // Status chip
        val (containerColor, textColor) = assignmentStatusColors(assignment.status)
        val statusLabel = assignment.status.name.lowercase().replaceFirstChar { it.uppercase() }
        Row(verticalAlignment = Alignment.CenterVertically) {
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

        Spacer(Modifier.height(Spacing.xl))
        HorizontalDivider()
        Spacer(Modifier.height(Spacing.xl))

        // Pickup location
        SectionLabel("Pickup")
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                imageVector        = Icons.Filled.LocationOn,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.secondary,
                modifier           = Modifier.size(20.dp),
            )
            Spacer(Modifier.size(Spacing.sm))
            Text(
                text  = request.pickupAddress.ifBlank { "—" },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        // Dropoff school
        if (request.dropoffSchoolName != null) {
            Spacer(Modifier.height(Spacing.xl))
            SectionLabel("Drop-off")
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector        = Icons.Filled.School,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.secondary,
                    modifier           = Modifier.size(20.dp),
                )
                Spacer(Modifier.size(Spacing.sm))
                Text(
                    text  = request.dropoffSchoolName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        // Notes
        if (!request.notes.isNullOrBlank()) {
            Spacer(Modifier.height(Spacing.xl))
            SectionLabel("Notes")
            Text(
                text  = request.notes,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Spacer(Modifier.height(Spacing.xl))
        HorizontalDivider()
        Spacer(Modifier.height(Spacing.xl))

        // Error message
        if (state.transitionError != null) {
            Text(
                text  = state.transitionError,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(Modifier.height(Spacing.lg))
        }

        // Action buttons
        StatusActions(
            status        = assignment.status,
            isTransitioning = state.isTransitioning,
            onTransition  = onTransition,
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text  = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(Spacing.xs))
}

@Composable
private fun StatusActions(
    status: AssignmentStatus,
    isTransitioning: Boolean,
    onTransition: (AssignmentStatus) -> Unit,
) {
    Column(
        modifier            = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        when (status) {
            AssignmentStatus.ASSIGNED -> {
                PrimaryActionButton(
                    label           = "Accept Task",
                    enabled         = !isTransitioning,
                    isLoading       = isTransitioning,
                    onClick         = { onTransition(AssignmentStatus.ACCEPTED) },
                )
                CancelActionButton(
                    enabled   = !isTransitioning,
                    onClick   = { onTransition(AssignmentStatus.CANCELLED) },
                )
            }
            AssignmentStatus.ACCEPTED -> {
                PrimaryActionButton(
                    label     = "Mark Collected",
                    enabled   = !isTransitioning,
                    isLoading = isTransitioning,
                    onClick   = { onTransition(AssignmentStatus.COLLECTED) },
                )
                CancelActionButton(
                    enabled = !isTransitioning,
                    onClick = { onTransition(AssignmentStatus.CANCELLED) },
                )
            }
            AssignmentStatus.COLLECTED -> {
                PrimaryActionButton(
                    label     = "Mark Delivered",
                    enabled   = !isTransitioning,
                    isLoading = isTransitioning,
                    onClick   = { onTransition(AssignmentStatus.DELIVERED) },
                )
            }
            AssignmentStatus.DELIVERED -> {
                PrimaryActionButton(
                    label     = "Complete",
                    enabled   = !isTransitioning,
                    isLoading = isTransitioning,
                    onClick   = { onTransition(AssignmentStatus.COMPLETED) },
                )
            }
            AssignmentStatus.COMPLETED, AssignmentStatus.CANCELLED -> {
                Text(
                    text  = if (status == AssignmentStatus.COMPLETED)
                        "This task is complete." else "This task was cancelled.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PrimaryActionButton(
    label: String,
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick  = onClick,
        enabled  = enabled,
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            Text(label)
        }
    }
}

@Composable
private fun CancelActionButton(enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick  = onClick,
        enabled  = enabled,
        modifier = Modifier.fillMaxWidth(),
        colors   = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor   = MaterialTheme.colorScheme.onErrorContainer,
        ),
    ) {
        Text("Cancel Task")
    }
}

@Composable
private fun TaskDetailError(message: String, onRetry: () -> Unit) {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text  = "Could not load task",
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
