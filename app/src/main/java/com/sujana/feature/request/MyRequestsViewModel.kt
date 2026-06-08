package com.sujana.feature.request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujana.core.common.AppResult
import com.sujana.domain.usecase.request.GetMyRequests
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyRequestsViewModel @Inject constructor(
    private val getMyRequests: GetMyRequests,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MyRequestsUiState>(MyRequestsUiState.Loading)
    val uiState: StateFlow<MyRequestsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = MyRequestsUiState.Loading
            _uiState.value = when (val result = getMyRequests()) {
                is AppResult.Success -> MyRequestsUiState.Content(result.data)
                is AppResult.Error   -> MyRequestsUiState.Error(result.error.toString())
            }
        }
    }
}
