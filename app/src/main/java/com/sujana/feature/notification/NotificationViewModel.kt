package com.sujana.feature.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujana.core.common.AppResult
import com.sujana.domain.model.Notification
import com.sujana.domain.model.NotificationPref
import com.sujana.domain.repository.INotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface NotificationUiState {
    data object Loading : NotificationUiState
    data class Content(
        val notifications: List<Notification>,
        val unreadCount: Int,
        val hasMore: Boolean,
        val currentPage: Int = 1,
        val isLoadingMore: Boolean = false,
    ) : NotificationUiState
    data class Error(val message: String) : NotificationUiState
}

sealed interface NotificationPrefsUiState {
    data object Loading : NotificationPrefsUiState
    data class Content(val prefs: List<NotificationPref>) : NotificationPrefsUiState
    data class Error(val message: String) : NotificationPrefsUiState
}

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repo: INotificationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificationUiState>(NotificationUiState.Loading)
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    private val _prefsState = MutableStateFlow<NotificationPrefsUiState>(NotificationPrefsUiState.Loading)
    val prefsState: StateFlow<NotificationPrefsUiState> = _prefsState.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.value = NotificationUiState.Loading
            when (val result = repo.getNotifications(page = 1)) {
                is AppResult.Success -> {
                    val unread = result.data.notifications.count { !it.isRead }
                    _uiState.value = NotificationUiState.Content(
                        notifications = result.data.notifications,
                        unreadCount = unread,
                        hasMore = result.data.hasMore,
                        currentPage = 1,
                    )
                }
                is AppResult.Error -> _uiState.value = NotificationUiState.Error(result.error.message ?: "Failed to load")
            }
        }
    }

    fun loadMore() {
        val current = _uiState.value as? NotificationUiState.Content ?: return
        if (!current.hasMore || current.isLoadingMore) return
        viewModelScope.launch {
            _uiState.value = current.copy(isLoadingMore = true)
            val nextPage = current.currentPage + 1
            when (val result = repo.getNotifications(page = nextPage)) {
                is AppResult.Success -> {
                    val combined = current.notifications + result.data.notifications
                    _uiState.value = current.copy(
                        notifications = combined,
                        unreadCount = combined.count { !it.isRead },
                        hasMore = result.data.hasMore,
                        currentPage = nextPage,
                        isLoadingMore = false,
                    )
                }
                is AppResult.Error -> _uiState.value = current.copy(isLoadingMore = false)
            }
        }
    }

    fun markRead(notificationId: String) {
        viewModelScope.launch {
            repo.markRead(notificationId)
            val current = _uiState.value as? NotificationUiState.Content ?: return@launch
            val updated = current.notifications.map { n ->
                if (n.id == notificationId && n.readAt == null) n.copy(readAt = "now") else n
            }
            _uiState.value = current.copy(
                notifications = updated,
                unreadCount = updated.count { !it.isRead },
            )
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            repo.markAllRead()
            val current = _uiState.value as? NotificationUiState.Content ?: return@launch
            val updated = current.notifications.map { it.copy(readAt = "now") }
            _uiState.value = current.copy(notifications = updated, unreadCount = 0)
        }
    }

    fun loadPrefs() {
        viewModelScope.launch {
            _prefsState.value = NotificationPrefsUiState.Loading
            when (val result = repo.getPrefs()) {
                is AppResult.Success -> _prefsState.value = NotificationPrefsUiState.Content(result.data)
                is AppResult.Error   -> _prefsState.value = NotificationPrefsUiState.Error(result.error.message ?: "Failed")
            }
        }
    }

    fun togglePref(category: String, muted: Boolean) {
        viewModelScope.launch {
            repo.updatePref(category, muted)
            val current = _prefsState.value as? NotificationPrefsUiState.Content ?: return@launch
            _prefsState.value = current.copy(
                prefs = current.prefs.map { p ->
                    if (p.category == category) p.copy(muted = muted) else p
                }
            )
        }
    }
}
