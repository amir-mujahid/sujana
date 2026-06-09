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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sujana.core.theme.Radii
import com.sujana.core.theme.Spacing
import com.sujana.core.theme.statusColors
import com.sujana.domain.model.Assignment
import com.sujana.feature.rider.NearbyPickup
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
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick  = { selectedTab = 0 },
                    text     = { Text("Assigned") },
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick  = { selectedTab = 1 },
                    text     = { Text("Available") },
                )
            }

            when (val state = uiState) {
                is RiderTasksUiState.Loading -> Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }

                is RiderTasksUiState.Error -> TaskListError(state.message, viewModel::load)

                is RiderTasksUiState.Content -> {
                    if (state.acceptError != null) {
                        Text(
                            text     = state.acceptError,
                            style    = MaterialTheme.typography.bodyMedium,
                            color    = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                        )
                    }
                    when (selectedTab) {
                        0 -> AssignedTab(
                            assignments      = state.assignments,
                            onNavigateToTask = onNavigateToTask,
                        )
                        1 -> AvailableTab(
                            nearbyPickups       = state.nearbyPickups,
                            locationUnavailable = state.locationUnavailable,
                            acceptingRequestId  = state.acceptingRequestId,
                            onAccept            = viewModel::acceptPickup,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AssignedTab(
    assignments: List<Assignment>,
    onNavigateToTask: (String) -> Unit,
) {
    if (assignments.isEmpty()) {
        TaskListEmpty("No assigned tasks", "You have no active assignments right now.")
    } else {
        LazyColumn(
            modifier            = Modifier.fillMaxSize(),
            contentPadding      = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            items(assignments, key = { it.id }) { assignment ->
                TaskCard(
                    assignment = assignment,
                    onClick    = { onNavigateToTask(assignment.id) },
                )
            }
        }
    }
}

@Composable
private fun AvailableTab(
    nearbyPickups: List<NearbyPickup>,
    locationUnavailable: Boolean,
    acceptingRequestId: String?,
    onAccept: (String) -> Unit,
) {
    when {
        locationUnavailable -> TaskListEmpty(
            title    = "Location required",
            subtitle = "Enable location permission so we can show pickups near you.",
        )
        nearbyPickups.isEmpty() -> TaskListEmpty(
            title    = "No pickups nearby",
            subtitle = "There are no pending requests within 10 km of your location.",
        )
        else -> LazyColumn(
            modifier            = Modifier.fillMaxSize(),
            contentPadding      = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            items(nearbyPickups, key = { it.request.id }) { nearby ->
                AvailablePickupCard(
                    nearby      = nearby,
                    isAccepting = acceptingRequestId == nearby.request.id,
                    onAccept    = { onAccept(nearby.request.id) },
                )
            }
        }
    }
}

@Composable
private fun AvailablePickupCard(
    nearby: NearbyPickup,
    isAccepting: Boolean,
    onAccept: () -> Unit,
) {
    val request = nearby.request
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
                Spacer(Modifier.height(Spacing.xs))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text  = formatDistance(nearby.distanceMetres),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    if (request.dropoffSchoolName != null) {
                        Text(
                            text  = " · ",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text     = "→ ${request.dropoffSchoolName}",
                            style    = MaterialTheme.typography.labelMedium,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            Spacer(Modifier.width(Spacing.sm))
            if (isAccepting) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                OutlinedButton(onClick = onAccept) { Text("Accept") }
            }
        }
    }
}

private fun formatDistance(metres: Double): String = when {
    metres < 1_000 -> "${metres.toInt()} m"
    else           -> "${"%.1f".format(metres / 1_000)} km"
}

@Composable
internal fun TaskCard(assignment: Assignment, onClick: () -> Unit) {
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
private fun TaskListEmpty(title: String, subtitle: String) {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(Spacing.sm))
        Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        Text(text = "Could not load tasks", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(Spacing.sm))
        Text(text = message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
