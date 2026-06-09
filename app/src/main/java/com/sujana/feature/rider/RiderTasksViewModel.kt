package com.sujana.feature.rider

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.sujana.core.common.AppResult
import com.sujana.core.websocket.WebSocketManager
import com.sujana.domain.usecase.assignment.GetRiderTasks
import com.sujana.domain.usecase.assignment.SelfAssignRequest
import com.sujana.domain.usecase.assignment.TransitionAssignment
import com.sujana.domain.usecase.request.GetNearbyPickups
import com.sujana.shared.AssignmentStatus
import com.sujana.shared.WsEventType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

@HiltViewModel
class RiderTasksViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getRiderTasks: GetRiderTasks,
    private val getNearbyPickups: GetNearbyPickups,
    private val selfAssignRequest: SelfAssignRequest,
    private val transitionAssignment: TransitionAssignment,
    private val webSocketManager: WebSocketManager,
) : ViewModel() {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    private val _uiState = MutableStateFlow<RiderTasksUiState>(RiderTasksUiState.Loading)
    val uiState: StateFlow<RiderTasksUiState> = _uiState.asStateFlow()

    init {
        load()
        viewModelScope.launch {
            while (true) {
                delay(POLL_MS)
                silentRefresh()
            }
        }
        viewModelScope.launch {
            webSocketManager.events.collect { event ->
                if (event.event == WsEventType.ASSIGNMENT_STATUS_CHANGED ||
                    event.event == WsEventType.REQUEST_STATUS_CHANGED
                ) {
                    silentRefresh()
                }
            }
        }
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = RiderTasksUiState.Loading
            val location = getRiderLocation()

            if (location == null) {
                when (val tasksResult = getRiderTasks()) {
                    is AppResult.Success -> _uiState.value = RiderTasksUiState.Content(
                        assignments         = tasksResult.data.filter { it.status != AssignmentStatus.CANCELLED },
                        nearbyPickups       = emptyList(),
                        locationUnavailable = true,
                    )
                    is AppResult.Error   -> _uiState.value = RiderTasksUiState.Error(tasksResult.error.toString())
                }
                return@launch
            }

            val riderLat = location.latitude
            val riderLng = location.longitude

            val tasksDeferred  = async { getRiderTasks() }
            val nearbyDeferred = async { getNearbyPickups(riderLat, riderLng) }

            val tasksResult  = tasksDeferred.await()
            val nearbyResult = nearbyDeferred.await()

            if (tasksResult is AppResult.Error) {
                _uiState.value = RiderTasksUiState.Error(tasksResult.error.toString())
                return@launch
            }

            val assignments = (tasksResult as AppResult.Success).data
                .filter { it.status != AssignmentStatus.CANCELLED }

            val nearby = if (nearbyResult is AppResult.Success) {
                nearbyResult.data
                    .map { req -> NearbyPickup(req, haversineMetres(riderLat, riderLng, req.pickupLat, req.pickupLng)) }
                    .sortedBy { it.distanceMetres }
            } else emptyList()

            _uiState.value = RiderTasksUiState.Content(
                assignments   = assignments,
                nearbyPickups = nearby,
            )
        }
    }

    private fun silentRefresh() {
        val current = _uiState.value as? RiderTasksUiState.Content ?: return
        if (current.acceptingRequestId != null) return
        viewModelScope.launch {
            val location = getRiderLocation()

            val newAssignments = when (val r = getRiderTasks()) {
                is AppResult.Success -> r.data.filter { it.status != AssignmentStatus.CANCELLED }
                is AppResult.Error   -> return@launch
            }

            if (location == null) {
                _uiState.value = current.copy(
                    assignments         = newAssignments,
                    locationUnavailable = true,
                )
                return@launch
            }

            val newNearby = when (val r = getNearbyPickups(location.latitude, location.longitude)) {
                is AppResult.Success -> r.data
                    .map { req -> NearbyPickup(req, haversineMetres(location.latitude, location.longitude, req.pickupLat, req.pickupLng)) }
                    .sortedBy { it.distanceMetres }
                is AppResult.Error -> current.nearbyPickups
            }

            _uiState.value = current.copy(
                assignments         = newAssignments,
                nearbyPickups       = newNearby,
                locationUnavailable = false,
            )
        }
    }

    fun acceptPickup(requestId: String) {
        val current = _uiState.value as? RiderTasksUiState.Content ?: return
        viewModelScope.launch {
            _uiState.value = current.copy(acceptingRequestId = requestId, acceptError = null)
            when (val result = selfAssignRequest(requestId)) {
                is AppResult.Success -> load()
                is AppResult.Error   -> _uiState.value = current.copy(
                    acceptingRequestId = null,
                    acceptError        = result.error.toString(),
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getRiderLocation(): android.location.Location? =
        suspendCancellableCoroutine { cont ->
            try {
                fusedClient.lastLocation
                    .addOnSuccessListener { loc -> cont.resume(loc) }
                    .addOnFailureListener { cont.resume(null) }
            } catch (_: SecurityException) {
                cont.resume(null)
            }
        }

    private fun haversineMetres(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val R = 6_371_000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2) * sin(dLng / 2)
        return R * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    companion object {
        private const val POLL_MS = 10_000L
    }
}
