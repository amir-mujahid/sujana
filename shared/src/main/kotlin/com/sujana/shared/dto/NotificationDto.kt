package com.sujana.shared.dto

import kotlinx.serialization.Serializable

@Serializable
data class NotificationDto(
    val id: String,
    val category: String,
    val title: String,
    val body: String,
    val deeplink: String?,
    val dataJson: String?,
    val readAt: String?,
    val createdAt: String,
)

@Serializable
data class NotificationPrefDto(
    val category: String,
    val muted: Boolean,
)

@Serializable
data class RegisterTokenRequest(
    val token: String,
    val platform: String = "android",
)

@Serializable
data class NotificationPageResponse(
    val notifications: List<NotificationDto>,
    val page: Int,
    val pageSize: Int,
    val hasMore: Boolean,
)
