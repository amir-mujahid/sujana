package com.sujana.feature.rider

import androidx.lifecycle.SavedStateHandle
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
class TaskDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getRiderTasks: GetRiderTasks,
    private val transitionAssignment: TransitionAssignment,
) : ViewModel() {

    private val assignmentId: String = checkNotNull(savedStateHandle["assignmentId"])

    private val _uiState = MutableStateFlow<TaskDetailUiState>(TaskDetailUiState.Loading)
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = TaskDetailUiState.Loading
            when (val result = getRiderTasks()) {
                is AppResult.Success -> {
                    val assignment = result.data.find { it.id == assignmentId }
                    _uiState.value = if (assignment != null) {
                        TaskDetailUiState.Content(assignment)
                    } else {
                        TaskDetailUiState.Error("Task not found")
                    }
                }
                is AppResult.Error -> _uiState.value = TaskDetailUiState.Error(result.error.toString())
            }
        }
    }

    fun transition(newStatus: AssignmentStatus) {
        val current = _uiState.value as? TaskDetailUiState.Content ?: return
        viewModelScope.launch {
            _uiState.value = current.copy(isTransitioning = true, transitionError = null)
            when (val result = transitionAssignment(assignmentId, newStatus)) {
                is AppResult.Success -> _uiState.value = TaskDetailUiState.Content(result.data)
                is AppResult.Error   -> _uiState.value = current.copy(
                    isTransitioning = false,
                    transitionError = result.error.toString(),
                )
            }
        }
    }
}
