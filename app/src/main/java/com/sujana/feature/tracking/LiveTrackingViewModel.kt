package com.sujana.feature.tracking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujana.core.common.AppResult
import com.sujana.data.repository.DirectionsRepository
import com.sujana.domain.model.Assignment
import com.sujana.domain.repository.IAssignmentRepository
import com.sujana.domain.repository.ITrackingRepository
import com.sujana.shared.AssignmentStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LiveTrackingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val trackingRepository: ITrackingRepository,
    private val assignmentRepository: IAssignmentRepository,
    private val directionsRepository: DirectionsRepository,
) : ViewModel() {

    val assignmentId: String = checkNotNull(savedStateHandle["assignmentId"])

    private val _uiState = MutableStateFlow(LiveTrackingUiState())
    val uiState: StateFlow<LiveTrackingUiState> = _uiState.asStateFlow()

    init {
        loadAssignment()
        observeTracking()
    }

    private fun loadAssignment() {
        viewModelScope.launch {
            when (val result = assignmentRepository.getAssignment(assignmentId)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(assignment = result.data) }
                    fetchRoute(result.data)
                }
                is AppResult.Error -> _uiState.update { it.copy(error = result.error.toString()) }
            }
        }
    }

    private fun observeTracking() {
        trackingRepository.observeTracking(assignmentId)
            .onEach { update ->
                _uiState.update { it.copy(trackingUpdate = update) }
                if (update == null) return@onEach
                val assignment = _uiState.value.assignment
                when {
                    // Assignment not yet loaded — init's loadAssignment() will handle route.
                    assignment == null -> return@onEach
                    // Tracking arrived but assignment is still ASSIGNED (rider just accepted).
                    // Re-fetch to get the ACCEPTED status so fetchRoute can draw the polyline.
                    assignment.status == AssignmentStatus.ASSIGNED -> loadAssignment()
                    else -> fetchRoute(assignment, riderLat = update.lat, riderLng = update.lng)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun fetchRoute(
        assignment: Assignment,
        riderLat: Double? = null,
        riderLng: Double? = null,
    ) {
        val originLat = riderLat ?: _uiState.value.trackingUpdate?.lat ?: return
        val originLng = riderLng ?: _uiState.value.trackingUpdate?.lng ?: return

        val (destLat, destLng) = when (assignment.status) {
            AssignmentStatus.ACCEPTED -> {
                assignment.request.pickupLat to assignment.request.pickupLng
            }
            AssignmentStatus.COLLECTED -> {
                val lat = assignment.request.dropoffSchoolLat ?: return
                val lng = assignment.request.dropoffSchoolLng ?: return
                lat to lng
            }
            else -> return
        }

        if (_uiState.value.isLoadingRoute) return
        _uiState.update { it.copy(isLoadingRoute = true) }
        viewModelScope.launch {
            when (val result = directionsRepository.getRoute(originLat, originLng, destLat, destLng)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(
                        routePoints     = result.data.points,
                        distanceText    = result.data.distanceText,
                        isLoadingRoute  = false,
                    )
                }
                is AppResult.Error -> _uiState.update { it.copy(isLoadingRoute = false) }
            }
        }
    }
}
