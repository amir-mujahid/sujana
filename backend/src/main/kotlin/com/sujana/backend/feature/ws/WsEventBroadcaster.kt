package com.sujana.backend.feature.ws

import com.sujana.shared.WsEventType
import com.sujana.shared.dto.WsEventDto
import io.ktor.websocket.Frame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object WsEventBroadcaster {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { encodeDefaults = true }

    fun broadcastToUsers(
        userIds: List<String>,
        event: WsEventType,
        entityId: String,
        status: String,
    ) {
        if (userIds.isEmpty()) return
        val text = json.encodeToString(WsEventDto(event = event, entityId = entityId, status = status))
        scope.launch {
            for (userId in userIds) {
                val session = ConnectionManager.getSession(userId) ?: continue
                try {
                    session.send(Frame.Text(text))
                } catch (_: Exception) {
                    ConnectionManager.remove(userId)
                }
            }
        }
    }
}
