package com.sujana.backend.feature.ws

import io.ktor.server.websocket.DefaultWebSocketServerSession
import java.util.concurrent.ConcurrentHashMap

object ConnectionManager {
    private val sessions = ConcurrentHashMap<String, DefaultWebSocketServerSession>()

    fun add(userId: String, session: DefaultWebSocketServerSession) {
        sessions[userId] = session
    }

    fun remove(userId: String) {
        sessions.remove(userId)
    }

    fun getSession(userId: String): DefaultWebSocketServerSession? = sessions[userId]
}
