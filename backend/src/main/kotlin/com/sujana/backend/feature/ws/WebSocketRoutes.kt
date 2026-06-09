package com.sujana.backend.feature.ws

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.sujana.backend.db.UsersTable
import io.ktor.server.routing.Routing
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun Routing.webSocketRoutes() {
    webSocket("/ws") {
        val token = call.request.headers["Authorization"]
            ?.removePrefix("Bearer ")
            ?.trim()
            ?: run { close(); return@webSocket }

        val userId = resolveUserDbId(token) ?: run { close(); return@webSocket }

        ConnectionManager.add(userId, this)
        try {
            for (frame in incoming) {
                if (frame is Frame.Text && frame.readText() == "ping") {
                    send(Frame.Text("pong"))
                }
            }
        } catch (_: ClosedReceiveChannelException) {
            // normal client disconnect
        } finally {
            ConnectionManager.remove(userId)
        }
    }
}

private val isLocalDev: Boolean =
    System.getenv("SUJANA_ENV") == "local"

private fun resolveUserDbId(token: String): String? {
    val firebaseUid = if (FirebaseApp.getApps().isEmpty() && isLocalDev) {
        "dev-uid"
    } else {
        try {
            FirebaseAuth.getInstance().verifyIdToken(token).uid
        } catch (_: Exception) {
            return null
        }
    }
    return transaction {
        UsersTable.selectAll()
            .where { UsersTable.firebaseUid eq firebaseUid }
            .singleOrNull()
            ?.get(UsersTable.id)
            ?.toString()
    }
}
