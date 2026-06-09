package com.sujana.feature.tracking

import com.google.android.gms.maps.model.LatLng
import com.sujana.domain.model.Assignment
import com.sujana.domain.model.TrackingUpdate

data class LiveTrackingUiState(
    val assignment: Assignment? = null,
    val trackingUpdate: TrackingUpdate? = null,
    val routePoints: List<LatLng> = emptyList(),
    val distanceText: String = "",
    val isLoadingRoute: Boolean = false,
    val error: String? = null,
)
