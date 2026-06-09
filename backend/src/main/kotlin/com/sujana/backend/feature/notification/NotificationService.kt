package com.sujana.backend.feature.notification

import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import com.sujana.backend.db.AssignmentsTable
import com.sujana.backend.db.DeviceTokensTable
import com.sujana.backend.db.NotificationPrefsTable
import com.sujana.backend.db.NotificationsTable
import com.sujana.backend.db.RequestsTable
import com.sujana.backend.db.UsersTable
import com.sujana.backend.feature.ws.WsEventBroadcaster
import com.sujana.shared.AssignmentStatus
import com.sujana.shared.NotificationCategory
import com.sujana.shared.RequestStatus
import com.sujana.shared.Role
import com.sujana.shared.WsEventType
import com.sujana.shared.dto.NotificationDto
import com.sujana.shared.dto.NotificationPageResponse
import com.sujana.shared.dto.NotificationPrefDto
import com.sujana.shared.dto.RegisterTokenRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.util.UUID

object NotificationService {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ── Domain event triggers ──────────────────────────────────────────────

    fun onRequestCreated(requestId: UUID, requesterId: UUID) {
        val title = "Pickup request submitted"
        val body = "Your waste pickup request has been received and is pending assignment."
        scope.launch {
            notify(
                recipientIds = listOf(requesterId),
                category = NotificationCategory.REQUEST_UPDATE,
                title = title,
                body = body,
                deeplink = "sujana://request/$requestId",
            )
            broadcastRequestEvent(requestId, RequestStatus.PENDING, listOf(requesterId.toString()))
        }
    }

    fun onRequestStatusChanged(
        requestId: UUID,
        newStatus: RequestStatus,
        requesterId: UUID,
        riderId: UUID?,
    ) {
        val (title, body) = requestStatusCopy(newStatus)
        scope.launch {
            val affectedIds = buildList {
                add(requesterId)
                if (riderId != null) add(riderId)
            }
            notify(
                recipientIds = affectedIds,
                category = NotificationCategory.REQUEST_UPDATE,
                title = title,
                body = body,
                deeplink = "sujana://request/$requestId",
            )
            broadcastRequestEvent(requestId, newStatus, affectedIds.map { it.toString() })
        }
    }

    fun onNewAssignment(assignmentId: UUID, riderId: UUID, requestId: UUID) {
        scope.launch {
            notify(
                recipientIds = listOf(riderId),
                category = NotificationCategory.ASSIGNMENT_UPDATE,
                title = "New pickup assignment",
                body = "You have been assigned a pickup. Tap to view details.",
                deeplink = "sujana://assignment/$assignmentId",
            )
            broadcastAssignmentEvent(assignmentId, AssignmentStatus.ASSIGNED, listOf(riderId.toString()))
        }
    }

    fun onAssignmentStatusChanged(
        assignmentId: UUID,
        newStatus: AssignmentStatus,
        riderId: UUID,
        requesterId: UUID,
    ) {
        val (title, body) = assignmentStatusCopy(newStatus)
        scope.launch {
            val affectedIds = listOf(riderId, requesterId)
            notify(
                recipientIds = affectedIds,
                category = NotificationCategory.ASSIGNMENT_UPDATE,
                title = title,
                body = body,
                deeplink = "sujana://assignment/$assignmentId",
            )
            broadcastAssignmentEvent(assignmentId, newStatus, affectedIds.map { it.toString() })
        }
    }

    fun notifyDispatchersNewRequest(requestId: UUID) {
        scope.launch {
            val dispatcherIds = transaction {
                UsersTable.selectAll()
                    .where { UsersTable.role eq Role.MPS_DISPATCHER.name }
                    .map { it[UsersTable.id] }
            }
            notify(
                recipientIds = dispatcherIds,
                category = NotificationCategory.DISPATCH_ALERT,
                title = "New school pickup request",
                body = "A new school waste collection request needs assignment.",
                deeplink = "sujana://dispatch",
            )
        }
    }

    // ── CRUD endpoints ─────────────────────────────────────────────────────

    fun registerToken(userId: UUID, body: RegisterTokenRequest): Unit = transaction {
        DeviceTokensTable.upsert(DeviceTokensTable.token) {
            it[DeviceTokensTable.userId] = userId
            it[DeviceTokensTable.token] = body.token
            it[DeviceTokensTable.platform] = body.platform
            it[DeviceTokensTable.updatedAt] = Instant.now()
        }
    }

    fun listNotifications(userId: UUID, page: Int, pageSize: Int): NotificationPageResponse = transaction {
        val offset = (page - 1) * pageSize
        val rows = NotificationsTable.selectAll()
            .where { NotificationsTable.userId eq userId }
            .orderBy(NotificationsTable.createdAt, SortOrder.DESC)
            .limit(pageSize + 1, offset.toLong())
            .toList()
        val hasMore = rows.size > pageSize
        NotificationPageResponse(
            notifications = rows.take(pageSize).map { it.toDto() },
            page = page,
            pageSize = pageSize,
            hasMore = hasMore,
        )
    }

    fun markRead(userId: UUID, notificationId: UUID): Boolean = transaction {
        val updated = NotificationsTable.update({
            (NotificationsTable.id eq notificationId) and
                (NotificationsTable.userId eq userId) and
                (NotificationsTable.readAt eq null)
        }) {
            it[readAt] = Instant.now()
        }
        updated > 0
    }

    fun markAllRead(userId: UUID): Unit = transaction {
        NotificationsTable.update({
            (NotificationsTable.userId eq userId) and
                (NotificationsTable.readAt eq null)
        }) {
            it[readAt] = Instant.now()
        }
    }

    fun getPrefs(userId: UUID): List<NotificationPrefDto> = transaction {
        val existing = NotificationPrefsTable.selectAll()
            .where { NotificationPrefsTable.userId eq userId }
            .associate { it[NotificationPrefsTable.category] to it[NotificationPrefsTable.muted] }

        NotificationCategory.entries.map { cat ->
            NotificationPrefDto(
                category = cat.name,
                muted = existing[cat.name] ?: false,
            )
        }
    }

    fun updatePref(userId: UUID, category: String, muted: Boolean): Unit = transaction {
        NotificationPrefsTable.upsert(NotificationPrefsTable.userId, NotificationPrefsTable.category) {
            it[NotificationPrefsTable.userId] = userId
            it[NotificationPrefsTable.category] = category
            it[NotificationPrefsTable.muted] = muted
        }
    }

    // ── Internals ──────────────────────────────────────────────────────────

    private suspend fun notify(
        recipientIds: List<UUID>,
        category: NotificationCategory,
        title: String,
        body: String,
        deeplink: String?,
    ) {
        val mutedByUser = transaction {
            NotificationPrefsTable.selectAll()
                .where {
                    (NotificationPrefsTable.category eq category.name) and
                        (NotificationPrefsTable.muted eq true)
                }
                .map { it[NotificationPrefsTable.userId] }
                .toSet()
        }

        val eligible = recipientIds.filter { it !in mutedByUser }
        if (eligible.isEmpty()) return

        transaction {
            for (uid in eligible) {
                NotificationsTable.insert {
                    it[userId] = uid
                    it[NotificationsTable.category] = category.name
                    it[NotificationsTable.title] = title
                    it[NotificationsTable.body] = body
                    it[NotificationsTable.deeplink] = deeplink
                    it[createdAt] = Instant.now()
                }
            }
        }

        if (FirebaseApp.getApps().isEmpty()) return

        val tokens = transaction {
            DeviceTokensTable.selectAll()
                .where { DeviceTokensTable.userId inList eligible }
                .map { it[DeviceTokensTable.token] }
        }

        for (token in tokens) {
            try {
                FirebaseMessaging.getInstance().send(
                    Message.builder()
                        .setToken(token)
                        .setNotification(
                            Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build(),
                        )
                        .apply { if (deeplink != null) putData("deeplink", deeplink) }
                        .putData("category", category.name)
                        .build(),
                )
            } catch (_: Exception) {
                // stale token; clean up asynchronously
            }
        }
    }

    private fun broadcastRequestEvent(requestId: UUID, status: RequestStatus, userIds: List<String>) {
        WsEventBroadcaster.broadcastToUsers(
            userIds = userIds,
            event = WsEventType.REQUEST_STATUS_CHANGED,
            entityId = requestId.toString(),
            status = status.name,
        )
    }

    private fun broadcastAssignmentEvent(
        assignmentId: UUID,
        status: AssignmentStatus,
        userIds: List<String>,
    ) {
        WsEventBroadcaster.broadcastToUsers(
            userIds = userIds,
            event = WsEventType.ASSIGNMENT_STATUS_CHANGED,
            entityId = assignmentId.toString(),
            status = status.name,
        )
    }

    private fun org.jetbrains.exposed.sql.ResultRow.toDto() = NotificationDto(
        id = this[NotificationsTable.id].toString(),
        category = this[NotificationsTable.category],
        title = this[NotificationsTable.title],
        body = this[NotificationsTable.body],
        deeplink = this[NotificationsTable.deeplink],
        dataJson = this[NotificationsTable.dataJson],
        readAt = this[NotificationsTable.readAt]?.toString(),
        createdAt = this[NotificationsTable.createdAt].toString(),
    )

    private fun requestStatusCopy(status: RequestStatus): Pair<String, String> = when (status) {
        RequestStatus.ASSIGNED  -> "Rider assigned" to "A rider has accepted your pickup request."
        RequestStatus.COLLECTED -> "Waste collected" to "Your waste has been collected by the rider."
        RequestStatus.DELIVERED -> "Delivered to school" to "Your waste has been delivered to the collection point."
        RequestStatus.COMPLETED -> "Pickup complete" to "Your waste pickup has been completed. Thank you!"
        RequestStatus.CANCELLED -> "Request cancelled" to "Your pickup request has been cancelled."
        RequestStatus.PENDING   -> "Request received" to "Your waste pickup request has been submitted."
    }

    private fun assignmentStatusCopy(status: AssignmentStatus): Pair<String, String> = when (status) {
        AssignmentStatus.ASSIGNED  -> "New assignment" to "You have a new pickup assignment."
        AssignmentStatus.ACCEPTED  -> "Assignment accepted" to "The rider has accepted the pickup."
        AssignmentStatus.COLLECTED -> "Waste collected" to "The waste has been collected."
        AssignmentStatus.DELIVERED -> "Delivered" to "Waste delivered to collection point."
        AssignmentStatus.COMPLETED -> "Assignment complete" to "The pickup assignment has been completed."
        AssignmentStatus.CANCELLED -> "Assignment cancelled" to "The pickup assignment has been cancelled."
    }
}
