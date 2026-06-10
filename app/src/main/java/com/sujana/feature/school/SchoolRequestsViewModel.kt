package com.sujana.feature.school

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujana.core.common.AppResult
import com.sujana.domain.model.PickupRequest
import com.sujana.domain.usecase.request.GetMyRequests
import com.sujana.shared.RequestType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SchoolRequestsUiState {
    data object Loading : SchoolRequestsUiState()
    data class Error(val message: String) : SchoolRequestsUiState()
    data class Content(val requests: List<PickupRequest>) : SchoolRequestsUiState()
}

@HiltViewModel
class SchoolRequestsViewModel @Inject constructor(
    private val getMyRequests: GetMyRequests,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SchoolRequestsUiState>(SchoolRequestsUiState.Loading)
    val uiState: StateFlow<SchoolRequestsUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = SchoolRequestsUiState.Loading
            when (val result = getMyRequests()) {
                is AppResult.Success -> {
                    val schoolRequests = result.data.filter { it.type == RequestType.SCHOOL }
                    _uiState.value = SchoolRequestsUiState.Content(schoolRequests)
                }
                is AppResult.Error -> _uiState.value = SchoolRequestsUiState.Error(result.error.toString())
            }
        }
    }
}
