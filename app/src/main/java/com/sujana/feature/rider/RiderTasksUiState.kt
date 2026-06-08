package com.sujana.feature.rider

import com.sujana.domain.model.Assignment

sealed class RiderTasksUiState {
    object Loading : RiderTasksUiState()
    data class Content(val assignments: List<Assignment>) : RiderTasksUiState()
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
