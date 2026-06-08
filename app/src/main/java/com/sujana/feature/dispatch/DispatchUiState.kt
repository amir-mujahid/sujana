package com.sujana.feature.dispatch

import com.sujana.domain.model.Assignment
import com.sujana.domain.model.PickupRequest
import com.sujana.domain.model.UserProfile

sealed class DispatchUiState {
    object Loading : DispatchUiState()
    data class Error(val message: String) : DispatchUiState()
    data class Content(
        val pendingRequests: List<PickupRequest>,
        val activeAssignments: List<Assignment>,
        val riders: List<UserProfile>,
        val riderPickerRequest: PickupRequest? = null,
        val isAssigning: Boolean = false,
        val assignError: String? = null,
    ) : DispatchUiState()
}
