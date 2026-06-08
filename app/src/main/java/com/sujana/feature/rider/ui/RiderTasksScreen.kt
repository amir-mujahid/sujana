package com.sujana.feature.rider.ui

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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.sujana.domain.model.Assignment
import com.sujana.feature.rider.RiderTasksUiState
import com.sujana.feature.rider.RiderTasksViewModel
import com.sujana.shared.AssignmentStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiderTasksScreen(
    onNavigateUp: () -> Unit,
    onNavigateToTask: (String) -> Unit,
    viewModel: RiderTasksViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Tasks") },
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
                is RiderTasksUiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }

                is RiderTasksUiState.Error -> TaskListError(state.message, viewModel::load)

                is RiderTasksUiState.Content -> {
                    if (state.assignments.isEmpty()) {
                        TaskListEmpty()
                    } else {
                        LazyColumn(
                            modifier            = Modifier.fillMaxSize(),
                            contentPadding      = PaddingValues(
                                horizontal = Spacing.lg,
                                vertical   = Spacing.lg,
                            ),
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                        ) {
                            items(state.assignments, key = { it.id }) { assignment ->
                                TaskCard(
                                    assignment = assignment,
                                    onClick    = { onNavigateToTask(assignment.id) },
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
internal fun TaskCard(assignment: Assignment, onClick: () -> Unit) {
    val colors = MaterialTheme.statusColors
    val (containerColor, textColor) = assignmentStatusColors(assignment.status)
    val statusLabel = assignment.status.name.lowercase().replaceFirstChar { it.uppercase() }

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                imageVector        = Icons.Filled.LocationOn,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.secondary,
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
                if (assignment.request.dropoffSchoolName != null) {
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        text     = "→ ${assignment.request.dropoffSchoolName}",
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
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
private fun TaskListEmpty() {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text  = "No active tasks",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(Spacing.sm))
        Text(
            text  = "You have no assignments right now.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TaskListError(message: String, onRetry: () -> Unit) {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text  = "Could not load tasks",
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

@Composable
internal fun assignmentStatusColors(status: AssignmentStatus): Pair<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color> {
    val colors = MaterialTheme.statusColors
    return when (status) {
        AssignmentStatus.ASSIGNED,
        AssignmentStatus.ACCEPTED  -> Pair(colors.warningContainer, colors.onWarningContainer)
        AssignmentStatus.COLLECTED,
        AssignmentStatus.DELIVERED -> Pair(colors.neutralContainer,  colors.onNeutralContainer)
        AssignmentStatus.COMPLETED -> Pair(colors.successContainer, colors.onSuccessContainer)
        AssignmentStatus.CANCELLED -> Pair(colors.errorContainer,   colors.onErrorContainer)
    }
}
