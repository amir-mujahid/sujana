package com.sujana.feature.request.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.sujana.core.theme.Spacing

private val DEFAULT_CENTER = LatLng(3.1390, 101.6869) // Kuala Lumpur
private const val DEFAULT_ZOOM = 12f
private const val SELECTED_ZOOM = 15f

@Composable
fun LocationPickerMap(
    selectedLat: Double?,
    selectedLng: Double?,
    onLocationSelected: (lat: Double, lng: Double) -> Unit,
    onUseCurrentLocation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(DEFAULT_CENTER, DEFAULT_ZOOM)
    }

    val markerState = rememberMarkerState()
    val hasSelection = selectedLat != null && selectedLng != null

    LaunchedEffect(selectedLat, selectedLng) {
        if (selectedLat != null && selectedLng != null) {
            val pos = LatLng(selectedLat, selectedLng)
            markerState.position = pos
            cameraState.animate(CameraUpdateFactory.newLatLngZoom(pos, SELECTED_ZOOM))
        }
    }

    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled    = false,
            myLocationButtonEnabled = false,
            mapToolbarEnabled       = false,
        )
    }
    val mapProperties = remember { MapProperties(isMyLocationEnabled = false) }

    Box(modifier = modifier) {
        GoogleMap(
            modifier        = Modifier.fillMaxSize(),
            cameraPositionState = cameraState,
            uiSettings       = uiSettings,
            properties       = mapProperties,
            onMapClick       = { latLng -> onLocationSelected(latLng.latitude, latLng.longitude) },
        ) {
            if (hasSelection) {
                Marker(
                    state = markerState,
                    title = "Pickup location",
                )
            }
        }

        FilledIconButton(
            onClick = onUseCurrentLocation,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Spacing.lg)
                .size(48.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor   = MaterialTheme.colorScheme.secondary,
            ),
        ) {
            Icon(
                imageVector   = Icons.Filled.MyLocation,
                contentDescription = "Use my location",
            )
        }
    }
}
