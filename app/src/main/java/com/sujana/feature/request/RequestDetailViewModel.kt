package com.sujana.feature.request

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujana.core.common.AppResult
import com.sujana.domain.usecase.request.CancelRequest
import com.sujana.domain.usecase.request.GetRequestDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RequestDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getRequestDetail: GetRequestDetail,
    private val cancelRequest: CancelRequest,
) : ViewModel() {

    private val requestId: String = checkNotNull(savedStateHandle["requestId"])

    private val _uiState = MutableStateFlow<RequestDetailUiState>(RequestDetailUiState.Loading)
    val uiState: StateFlow<RequestDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = RequestDetailUiState.Loading
            _uiState.value = when (val result = getRequestDetail(requestId)) {
                is AppResult.Success -> RequestDetailUiState.Content(result.data)
                is AppResult.Error   -> RequestDetailUiState.Error(result.error.toString())
            }
        }
    }

    fun cancel() {
        val current = _uiState.value as? RequestDetailUiState.Content ?: return
        viewModelScope.launch {
            _uiState.value = current.copy(isCancelling = true, cancelError = null)
            when (val result = cancelRequest(requestId)) {
                is AppResult.Success -> _uiState.value = RequestDetailUiState.Content(result.data)
                is AppResult.Error   -> _uiState.value = current.copy(
                    isCancelling = false,
                    cancelError  = result.error.toString(),
                )
            }
        }
    }
}
