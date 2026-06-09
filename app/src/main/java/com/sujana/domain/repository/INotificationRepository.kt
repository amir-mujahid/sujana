package com.sujana.domain.repository

import com.sujana.core.common.AppResult
import com.sujana.domain.model.Notification
import com.sujana.domain.model.NotificationPage
import com.sujana.domain.model.NotificationPref

interface INotificationRepository {
    suspend fun registerDeviceToken(token: String): AppResult<Unit>
    suspend fun getNotifications(page: Int = 1, pageSize: Int = 20): AppResult<NotificationPage>
    suspend fun markRead(notificationId: String): AppResult<Unit>
    suspend fun markAllRead(): AppResult<Unit>
    suspend fun getPrefs(): AppResult<List<NotificationPref>>
    suspend fun updatePref(category: String, muted: Boolean): AppResult<Unit>
    suspend fun getUnreadCount(): AppResult<Int>
}
