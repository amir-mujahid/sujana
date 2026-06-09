package com.sujana.feature.rider

import com.sujana.domain.model.Assignment
import com.sujana.domain.model.PickupRequest

data class NearbyPickup(
    val request: PickupRequest,
    val distanceMetres: Double,
)

sealed class RiderTasksUiState {
    object Loading : RiderTasksUiState()
    data class Content(
        val assignments: List<Assignment>,
        val nearbyPickups: List<NearbyPickup> = emptyList(),
        val locationUnavailable: Boolean = false,
        val acceptingRequestId: String? = null,
        val acceptError: String? = null,
    ) : RiderTasksUiState()
    data class Error(val message: String) : RiderTasksUiState()
}

sealed class TaskDetailUiState {
    object Loading : TaskDetailUiState()
    data class Content(
        val assignment: Assignment,
        val isTransitioning: Boolean = false,
        val transitionError: String? = null,
    ) : TaskDetailUiState()
    data class Error(val message: String) : TaskDetailUiState()
}
