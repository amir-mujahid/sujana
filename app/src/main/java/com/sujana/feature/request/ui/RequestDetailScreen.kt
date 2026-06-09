package com.sujana.feature.request.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.sujana.core.theme.Radii
import com.sujana.core.theme.Spacing
import com.sujana.core.theme.statusColors as statusTheme
import com.sujana.domain.model.PickupRequest
import com.sujana.feature.request.RequestDetailUiState
import com.sujana.feature.request.RequestDetailViewModel
import com.sujana.shared.RequestStatus

private val STATUS_STEPS = listOf(
    RequestStatus.PENDING,
    RequestStatus.ASSIGNED,
    RequestStatus.COLLECTED,
    RequestStatus.DELIVERED,
    RequestStatus.COMPLETED,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestDetailScreen(
    onNavigateUp: () -> Unit,
    onNavigateToTracking: (assignmentId: String) -> Unit,
    viewModel: RequestDetailViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Request Detail") },
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
                is RequestDetailUiState.Loading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }

                is RequestDetailUiState.Error -> Column(
                    Modifier.fillMaxSize().padding(Spacing.xl),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(Spacing.lg))
                    Button(onClick = viewModel::load) { Text("Retry") }
                }

                is RequestDetailUiState.Content -> RequestDetailContent(
                    request              = state.request,
                    isCancelling         = state.isCancelling,
                    cancelError          = state.cancelError,
                    onCancel             = viewModel::cancel,
                    onNavigateToTracking = { assignmentId -> onNavigateToTracking(assignmentId) },
                )
            }
        }
    }
}

private val TRACKABLE_STATUSES = setOf(
    RequestStatus.ASSIGNED, RequestStatus.COLLECTED, RequestStatus.DELIVERED,
)

@Composable
private fun RequestDetailContent(
    request: PickupRequest,
    isCancelling: Boolean,
    cancelError: String?,
    onCancel: () -> Unit,
    onNavigateToTracking: (assignmentId: String) -> Unit,
) {
    var showCancelDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // Mini map showing pickup location
        val pickupLatLng = LatLng(request.pickupLat, request.pickupLng)
        val cameraState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(pickupLatLng, 15f)
        }
        GoogleMap(
            modifier            = Modifier.fillMaxWidth().height(200.dp),
            cameraPositionState = cameraState,
            uiSettings          = MapUiSettings(
                scrollGesturesEnabled  = false,
                zoomGesturesEnabled    = false,
                zoomControlsEnabled    = false,
                mapToolbarEnabled      = false,
                myLocationButtonEnabled = false,
            ),
            properties          = MapProperties(),
        ) {
            Marker(
                state = rememberMarkerState(position = pickupLatLng),
                title = "Pickup",
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.lg),
        ) {
            // Status + ID row
            Row(
                modifier            = Modifier.fillMaxWidth(),
                verticalAlignment   = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StatusChip(request.status)
                Text(
                    text  = "…${request.id.takeLast(8)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Track Rider button
            if (request.status in TRACKABLE_STATUSES && request.assignmentId != null) {
                Spacer(Modifier.height(Spacing.md))
                Button(
                    onClick  = { onNavigateToTracking(request.assignmentId) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Filled.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(Spacing.sm))
                    Text("Track Rider")
                }
            }

            Spacer(Modifier.height(Spacing.xl))

            // Status timeline (for non-cancelled)
            if (request.status != RequestStatus.CANCELLED) {
                StatusTimeline(currentStatus = request.status)
                Spacer(Modifier.height(Spacing.xl))
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(Spacing.lg))

            // Location details
            DetailRow(label = "Pickup address", value = request.pickupAddress.ifBlank { "—" })
            if (request.dropoffSchoolName != null) {
                Spacer(Modifier.height(Spacing.md))
                DetailRow(label = "Drop-off school", value = request.dropoffSchoolName)
            }
            if (!request.notes.isNullOrBlank()) {
                Spacer(Modifier.height(Spacing.md))
                DetailRow(label = "Notes", value = request.notes)
            }

            Spacer(Modifier.height(Spacing.md))
            DetailRow(label = "Submitted", value = request.createdAt.take(10))

            // Photo
            if (request.photoUrl != null) {
                Spacer(Modifier.height(Spacing.lg))
                Text(
                    text  = "Photo",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(Spacing.sm))
                AsyncImage(
                    model             = request.photoUrl,
                    contentDescription = "Request photo",
                    contentScale      = ContentScale.Crop,
                    modifier          = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(Radii.card)),
                )
            }

            // Cancel button
            val cancellable = request.status == RequestStatus.PENDING ||
                    request.status == RequestStatus.ASSIGNED
            if (cancellable) {
                Spacer(Modifier.height(Spacing.xl))
                if (cancelError != null) {
                    Text(cancelError, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(Spacing.sm))
                }
                Button(
                    onClick  = { showCancelDialog = true },
                    enabled  = !isCancelling,
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor   = MaterialTheme.colorScheme.onError,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (isCancelling) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.Cancel, null)
                        Spacer(Modifier.width(Spacing.sm))
                        Text("Cancel Request")
                    }
                }
            }

            Spacer(Modifier.height(Spacing.xxxl))
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title  = { Text("Cancel request?") },
            text   = { Text("This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = { showCancelDialog = false; onCancel() },
                    colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) { Text("Cancel Request") }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("Keep") }
            },
        )
    }
}

@Composable
private fun StatusTimeline(currentStatus: RequestStatus) {
    val currentIndex = STATUS_STEPS.indexOf(currentStatus)
    val colors = MaterialTheme.statusTheme

    Row(
        modifier            = Modifier.fillMaxWidth(),
        verticalAlignment   = Alignment.CenterVertically,
    ) {
        STATUS_STEPS.forEachIndexed { index, step ->
            val isDone    = index <= currentIndex
            val isCurrent = index == currentIndex
            val dotColor  = if (isDone) colors.success else MaterialTheme.colorScheme.outlineVariant
            val lineColor = if (index < currentIndex) colors.success else MaterialTheme.colorScheme.outlineVariant

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(if (isCurrent) 12.dp else 8.dp)
                        .background(dotColor, CircleShape),
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text  = step.name.take(4),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isDone) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (index < STATUS_STEPS.lastIndex) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(lineColor),
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text  = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
