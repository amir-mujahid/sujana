package com.sujana

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujana.core.websocket.WebSocketManager
import com.sujana.domain.model.User
import com.sujana.domain.usecase.auth.GetCurrentSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SessionState(
    val loaded: Boolean = false,
    val user: User? = null,
)

@HiltViewModel
class MainViewModel @Inject constructor(
    getCurrentSession: GetCurrentSession,
    private val webSocketManager: WebSocketManager,
) : ViewModel() {

    val session: StateFlow<SessionState> = getCurrentSession()
        .map { SessionState(loaded = true, user = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SessionState(loaded = false),
        )

    init {
        viewModelScope.launch {
            session.collect { s ->
                if (!s.loaded) return@collect
                if (s.user != null) webSocketManager.connect() else webSocketManager.disconnect()
            }
        }
    }
}
