package com.sujana.feature.request

import android.net.Uri
import com.sujana.domain.model.PickupRequest
import com.sujana.domain.model.School

sealed class MyRequestsUiState {
    object Loading : MyRequestsUiState()
    data class Content(val requests: List<PickupRequest>) : MyRequestsUiState()
    data class Error(val message: String) : MyRequestsUiState()
}

data class CreateRequestFormState(
    val pickupLat: Double? = null,
    val pickupLng: Double? = null,
    val pickupAddress: String = "",
    val dropoffSchool: School? = null,
    val notes: String = "",
    val photoUri: Uri? = null,
    val photoUrl: String? = null,
    val schools: List<School> = emptyList(),
    val schoolsLoading: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    val locationError: String? = null,
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
)

sealed class RequestDetailUiState {
    object Loading : RequestDetailUiState()
    data class Content(
        val request: PickupRequest,
        val isCancelling: Boolean = false,
        val cancelError: String? = null,
    ) : RequestDetailUiState()
    data class Error(val message: String) : RequestDetailUiState()
}
