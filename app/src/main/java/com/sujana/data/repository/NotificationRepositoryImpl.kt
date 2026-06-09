package com.sujana.data.repository

import com.sujana.core.common.AppResult
import com.sujana.core.network.SujanaApi
import com.sujana.domain.model.Notification
import com.sujana.domain.model.NotificationPage
import com.sujana.domain.model.NotificationPref
import com.sujana.domain.repository.INotificationRepository
import com.sujana.shared.dto.NotificationPrefDto
import com.sujana.shared.dto.RegisterTokenRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val api: SujanaApi,
) : INotificationRepository {

    override suspend fun registerDeviceToken(token: String): AppResult<Unit> =
        runCatching { api.registerToken(RegisterTokenRequest(token)) }
            .fold({ AppResult.Success(Unit) }, { AppResult.Error(it) })

    override suspend fun getNotifications(page: Int, pageSize: Int): AppResult<NotificationPage> =
        runCatching { api.getNotifications(page, pageSize) }
            .fold(
                onSuccess = { resp ->
                    AppResult.Success(
                        NotificationPage(
                            notifications = resp.notifications.map { dto ->
                                Notification(
                                    id = dto.id,
                                    category = dto.category,
                                    title = dto.title,
                                    body = dto.body,
                                    deeplink = dto.deeplink,
                                    readAt = dto.readAt,
                                    createdAt = dto.createdAt,
                                )
                            },
                            page = resp.page,
                            hasMore = resp.hasMore,
                        )
                    )
                },
                onFailure = { AppResult.Error(it) },
            )

    override suspend fun markRead(notificationId: String): AppResult<Unit> =
        runCatching { api.markNotificationRead(notificationId) }
            .fold({ AppResult.Success(Unit) }, { AppResult.Error(it) })

    override suspend fun markAllRead(): AppResult<Unit> =
        runCatching { api.markAllNotificationsRead() }
            .fold({ AppResult.Success(Unit) }, { AppResult.Error(it) })

    override suspend fun getPrefs(): AppResult<List<NotificationPref>> =
        runCatching { api.getNotificationPrefs() }
            .fold(
                onSuccess = { dtos ->
                    AppResult.Success(dtos.map { NotificationPref(category = it.category, muted = it.muted) })
                },
                onFailure = { AppResult.Error(it) },
            )

    override suspend fun updatePref(category: String, muted: Boolean): AppResult<Unit> =
        runCatching { api.updateNotificationPref(NotificationPrefDto(category = category, muted = muted)) }
            .fold({ AppResult.Success(Unit) }, { AppResult.Error(it) })

    override suspend fun getUnreadCount(): AppResult<Int> =
        runCatching { api.getNotifications(page = 1, pageSize = 50) }
            .fold(
                onSuccess = { resp ->
                    AppResult.Success(resp.notifications.count { it.readAt == null })
                },
                onFailure = { AppResult.Error(it) },
            )
}
