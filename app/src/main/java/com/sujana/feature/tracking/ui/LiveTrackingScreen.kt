package com.sujana.feature.tracking.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.sujana.core.theme.Radii
import com.sujana.core.theme.Spacing
import com.sujana.feature.tracking.LiveTrackingUiState
import com.sujana.feature.tracking.LiveTrackingViewModel

@Composable
fun LiveTrackingScreen(
    onNavigateUp: () -> Unit,
    viewModel: LiveTrackingViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val cameraPositionState = rememberCameraPositionState()

    // Bug fix: hold a single MarkerState for the rider and mutate its position imperatively.
    // rememberMarkerState(position = ...) only sets the initial value — it ignores position
    // changes on recomposition, which is why the pin was stuck.
    val riderMarkerState = remember { MarkerState() }

    val tracking = uiState.trackingUpdate

    LaunchedEffect(tracking?.lat, tracking?.lng) {
        if (tracking != null) {
            riderMarkerState.position = LatLng(tracking.lat, tracking.lng)
            cameraPositionState.animate(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.fromLatLngZoom(LatLng(tracking.lat, tracking.lng), 15f)
                ),
                durationMs = 800,
            )
        }
    }

    Box(Modifier.fillMaxSize()) {
        GoogleMap(
            modifier            = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings          = MapUiSettings(
                myLocationButtonEnabled = false,
                mapToolbarEnabled       = false,
                zoomControlsEnabled     = false,
            ),
            properties = MapProperties(),
        ) {
            val request = uiState.assignment?.request

            // Pickup marker — red pin
            if (request != null) {
                val pickupLatLng = LatLng(request.pickupLat, request.pickupLng)
                Marker(
                    state = rememberMarkerState(position = pickupLatLng),
                    title = "Pickup",
                    icon  = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                )
            }

            // Dropoff / school marker — green pin
            if (request?.dropoffSchoolLat != null && request.dropoffSchoolLng != null) {
                Marker(
                    state = rememberMarkerState(
                        position = LatLng(request.dropoffSchoolLat, request.dropoffSchoolLng)
                    ),
                    title = request.dropoffSchoolName ?: "Drop-off",
                    icon  = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                )
            }

            // Rider marker — blue pin, position updated via riderMarkerState above
            if (tracking != null) {
                Marker(
                    state = riderMarkerState,
                    title = "Rider",
                    icon  = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                )
            }

            // Route polyline
            if (uiState.routePoints.size >= 2) {
                Polyline(
                    points = uiState.routePoints,
                    color  = MaterialTheme.colorScheme.primary,
                    width  = 8f,
                )
            }
        }

        // Back button overlay
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .padding(Spacing.md),
        ) {
            Surface(
                shape           = CircleShape,
                shadowElevation = 4.dp,
                color           = MaterialTheme.colorScheme.surface,
            ) {
                IconButton(onClick = onNavigateUp) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        }

        // Bottom info sheet
        TrackingInfoSheet(
            uiState  = uiState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        // "Waiting for rider" banner — shown until first RTDB update arrives
        if (tracking == null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 64.dp)
                    .clip(RoundedCornerShape(Radii.chip))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
            ) {
                Text(
                    text  = "Waiting for rider location…",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TrackingInfoSheet(
    uiState: LiveTrackingUiState,
    modifier: Modifier = Modifier,
) {
    val assignment = uiState.assignment
    Card(
        modifier  = modifier
            .fillMaxWidth()
            .padding(Spacing.md)
            .navigationBarsPadding(),
        shape     = RoundedCornerShape(Radii.card),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (assignment != null) {
                    Text(
                        text  = assignment.status.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                if (uiState.distanceText.isNotBlank()) {
                    Text(
                        text  = uiState.distanceText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (uiState.isLoadingRoute) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                }
            }

            if (assignment != null) {
                Spacer(Modifier.height(Spacing.md))

                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector        = Icons.Filled.LocationOn,
                        contentDescription = null,
                        modifier           = Modifier.size(18.dp),
                        tint               = MaterialTheme.colorScheme.error,
                    )
                    Spacer(Modifier.width(Spacing.sm))
                    Column {
                        Text(
                            text  = "Pickup",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text  = assignment.request.pickupAddress.ifBlank { "—" },
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                if (assignment.request.dropoffSchoolName != null) {
                    Spacer(Modifier.height(Spacing.sm))
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector        = Icons.Filled.School,
                            contentDescription = null,
                            modifier           = Modifier.size(18.dp),
                            tint               = MaterialTheme.colorScheme.secondary,
                        )
                        Spacer(Modifier.width(Spacing.sm))
                        Column {
                            Text(
                                text  = "Drop-off",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text  = assignment.request.dropoffSchoolName,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
        }
    }
}
