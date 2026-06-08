package com.sujana.feature.rider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujana.core.common.AppResult
import com.sujana.domain.usecase.assignment.GetRiderTasks
import com.sujana.domain.usecase.assignment.SelfAssignRequest
import com.sujana.domain.usecase.assignment.TransitionAssignment
import com.sujana.domain.usecase.request.GetAvailablePickups
import com.sujana.shared.AssignmentStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RiderTasksViewModel @Inject constructor(
    private val getRiderTasks: GetRiderTasks,
    private val getAvailablePickups: GetAvailablePickups,
    private val selfAssignRequest: SelfAssignRequest,
    private val transitionAssignment: TransitionAssignment,
) : ViewModel() {

    private val _uiState = MutableStateFlow<RiderTasksUiState>(RiderTasksUiState.Loading)
    val uiState: StateFlow<RiderTasksUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = RiderTasksUiState.Loading

            val tasksDeferred     = async { getRiderTasks() }
            val availableDeferred = async { getAvailablePickups() }

            val tasksResult     = tasksDeferred.await()
            val availableResult = availableDeferred.await()

            if (tasksResult is AppResult.Error) {
                _uiState.value = RiderTasksUiState.Error(tasksResult.error.toString())
                return@launch
            }

            val assignments = (tasksResult as AppResult.Success).data
                .filter { it.status != AssignmentStatus.CANCELLED }
            val available = if (availableResult is AppResult.Success) availableResult.data else emptyList()

            _uiState.value = RiderTasksUiState.Content(
                assignments      = assignments,
                availablePickups = available,
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
}
