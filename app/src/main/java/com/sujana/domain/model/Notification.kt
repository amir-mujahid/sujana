package com.sujana.domain.model

data class Notification(
    val id: String,
    val category: String,
    val title: String,
    val body: String,
    val deeplink: String?,
    val readAt: String?,
    val createdAt: String,
) {
    val isRead: Boolean get() = readAt != null
}

data class NotificationPage(
    val notifications: List<Notification>,
    val page: Int,
    val hasMore: Boolean,
)

data class NotificationPref(
    val category: String,
    val muted: Boolean,
)
