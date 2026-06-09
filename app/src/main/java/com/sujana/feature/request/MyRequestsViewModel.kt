package com.sujana.feature.request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujana.core.common.AppResult
import com.sujana.core.websocket.WebSocketManager
import com.sujana.domain.usecase.request.GetMyRequests
import com.sujana.shared.WsEventType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyRequestsViewModel @Inject constructor(
    private val getMyRequests: GetMyRequests,
    private val webSocketManager: WebSocketManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MyRequestsUiState>(MyRequestsUiState.Loading)
    val uiState: StateFlow<MyRequestsUiState> = _uiState.asStateFlow()

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
                if (event.event == WsEventType.REQUEST_STATUS_CHANGED) {
                    silentRefresh()
                }
            }
        }
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

    private fun silentRefresh() {
        if (_uiState.value is MyRequestsUiState.Loading) return
        viewModelScope.launch {
            when (val result = getMyRequests()) {
                is AppResult.Success -> _uiState.value = MyRequestsUiState.Content(result.data)
                is AppResult.Error   -> { /* keep current state on transient poll failure */ }
            }
        }
    }

    companion object {
        private const val POLL_MS = 10_000L
    }
}
