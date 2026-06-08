package com.sujana.feature.dispatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujana.core.common.AppResult
import com.sujana.domain.model.Assignment
import com.sujana.domain.model.PickupRequest
import com.sujana.domain.model.UserProfile
import com.sujana.domain.usecase.assignment.AssignRider
import com.sujana.domain.usecase.assignment.GetAvailableRiders
import com.sujana.domain.usecase.assignment.GetDispatchQueue
import com.sujana.domain.usecase.request.GetMyRequests
import com.sujana.shared.AssignmentStatus
import com.sujana.shared.RequestStatus
import com.sujana.shared.RequestType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DispatchViewModel @Inject constructor(
    private val getMyRequests: GetMyRequests,
    private val getDispatchQueue: GetDispatchQueue,
    private val getAvailableRiders: GetAvailableRiders,
    private val assignRider: AssignRider,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DispatchUiState>(DispatchUiState.Loading)
    val uiState: StateFlow<DispatchUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = DispatchUiState.Loading

            val requestsDeferred  = async { getMyRequests() }
            val assignmentsDeferred = async { getDispatchQueue() }
            val ridersDeferred    = async { getAvailableRiders() }

            val requestsResult    = requestsDeferred.await()
            val assignmentsResult = assignmentsDeferred.await()
            val ridersResult      = ridersDeferred.await()

            if (requestsResult is AppResult.Error) {
                _uiState.value = DispatchUiState.Error(requestsResult.error.toString())
                return@launch
            }
            if (assignmentsResult is AppResult.Error) {
                _uiState.value = DispatchUiState.Error(assignmentsResult.error.toString())
                return@launch
            }
            if (ridersResult is AppResult.Error) {
                _uiState.value = DispatchUiState.Error(ridersResult.error.toString())
                return@launch
            }

            val allRequests   = (requestsResult as AppResult.Success).data
            val allAssignments = (assignmentsResult as AppResult.Success).data
            val riders        = (ridersResult as AppResult.Success).data

            val activeStatuses = setOf(
                AssignmentStatus.ASSIGNED,
                AssignmentStatus.ACCEPTED,
                AssignmentStatus.COLLECTED,
                AssignmentStatus.DELIVERED,
            )
            val assignedRequestIds = allAssignments
                .filter { it.status in activeStatuses }
                .map { it.requestId }
                .toSet()

            val pendingRequests = allRequests.filter {
                it.type == RequestType.SCHOOL &&
                it.status == RequestStatus.PENDING &&
                it.id !in assignedRequestIds
            }
            val activeAssignments = allAssignments.filter { it.status in activeStatuses }

            _uiState.value = DispatchUiState.Content(
                pendingRequests  = pendingRequests,
                activeAssignments = activeAssignments,
                riders           = riders,
            )
        }
    }

    fun openRiderPicker(request: PickupRequest) {
        val current = _uiState.value as? DispatchUiState.Content ?: return
        _uiState.value = current.copy(riderPickerRequest = request, assignError = null)
    }

    fun dismissRiderPicker() {
        val current = _uiState.value as? DispatchUiState.Content ?: return
        _uiState.value = current.copy(riderPickerRequest = null, assignError = null)
    }

    fun assign(request: PickupRequest, rider: UserProfile) {
        val current = _uiState.value as? DispatchUiState.Content ?: return
        viewModelScope.launch {
            _uiState.value = current.copy(isAssigning = true, assignError = null)
            when (val result = assignRider(request.id, rider.id)) {
                is AppResult.Success -> {
                    _uiState.value = current.copy(
                        riderPickerRequest = null,
                        isAssigning = false,
                    )
                    load()
                }
                is AppResult.Error -> {
                    _uiState.value = (_uiState.value as? DispatchUiState.Content
                        ?: current).copy(
                        isAssigning = false,
                        assignError = result.error.toString(),
                    )
                }
            }
        }
    }
}
