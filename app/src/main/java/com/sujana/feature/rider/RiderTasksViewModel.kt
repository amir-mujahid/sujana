package com.sujana.feature.rider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujana.core.common.AppResult
import com.sujana.domain.usecase.assignment.GetRiderTasks
import com.sujana.domain.usecase.assignment.TransitionAssignment
import com.sujana.shared.AssignmentStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RiderTasksViewModel @Inject constructor(
    private val getRiderTasks: GetRiderTasks,
    private val transitionAssignment: TransitionAssignment,
) : ViewModel() {

    private val _uiState = MutableStateFlow<RiderTasksUiState>(RiderTasksUiState.Loading)
    val uiState: StateFlow<RiderTasksUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = RiderTasksUiState.Loading
            _uiState.value = when (val result = getRiderTasks()) {
                is AppResult.Success -> RiderTasksUiState.Content(
                    result.data.filter { it.status != AssignmentStatus.CANCELLED }
                )
                is AppResult.Error   -> RiderTasksUiState.Error(result.error.toString())
            }
        }
    }
}
