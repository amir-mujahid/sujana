package com.sujana.feature.request

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujana.core.common.AppResult
import com.sujana.core.websocket.WebSocketManager
import com.sujana.domain.usecase.request.CancelRequest
import com.sujana.domain.usecase.request.GetRequestDetail
import com.sujana.shared.AssignmentStatus
import com.sujana.shared.RequestStatus
import com.sujana.shared.WsEventType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
    private val webSocketManager: WebSocketManager,
) : ViewModel() {

    private val requestId: String = checkNotNull(savedStateHandle["requestId"])

    private val _uiState = MutableStateFlow<RequestDetailUiState>(RequestDetailUiState.Loading)
    val uiState: StateFlow<RequestDetailUiState> = _uiState.asStateFlow()

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
                val current = _uiState.value as? RequestDetailUiState.Content ?: return@collect
                when (event.event) {
                    WsEventType.REQUEST_STATUS_CHANGED -> {
                        if (event.entityId != requestId) return@collect
                        val newStatus = runCatching { RequestStatus.valueOf(event.status) }.getOrNull()
                        if (newStatus != null) {
                            _uiState.value = current.copy(request = current.request.copy(status = newStatus))
                        }
                        silentRefresh()
                    }
                    WsEventType.ASSIGNMENT_STATUS_CHANGED -> {
                        // Mirror assignment transitions → request status (matches backend mirrorStatus logic)
                        if (event.entityId != current.request.assignmentId) return@collect
                        val mirrored = runCatching { AssignmentStatus.valueOf(event.status) }
                            .getOrNull()?.toRequestStatus() ?: return@collect
                        _uiState.value = current.copy(request = current.request.copy(status = mirrored))
                        silentRefresh()
                    }
                }
            }
        }
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

    private fun silentRefresh() {
        val current = _uiState.value as? RequestDetailUiState.Content ?: return
        if (current.isCancelling) return
        viewModelScope.launch {
            when (val result = getRequestDetail(requestId)) {
                is AppResult.Success -> _uiState.value = RequestDetailUiState.Content(result.data)
                is AppResult.Error   -> { /* keep current state on transient poll failure */ }
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

    companion object {
        private const val POLL_MS = 10_000L
    }
}

private fun AssignmentStatus.toRequestStatus(): RequestStatus? = when (this) {
    AssignmentStatus.ASSIGNED,
    AssignmentStatus.ACCEPTED  -> RequestStatus.ASSIGNED
    AssignmentStatus.COLLECTED -> RequestStatus.COLLECTED
    AssignmentStatus.DELIVERED -> RequestStatus.DELIVERED
    AssignmentStatus.COMPLETED -> RequestStatus.COMPLETED
    AssignmentStatus.CANCELLED -> RequestStatus.PENDING
}
