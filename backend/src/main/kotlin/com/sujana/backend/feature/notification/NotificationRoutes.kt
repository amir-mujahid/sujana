package com.sujana.backend.feature.notification

import com.sujana.backend.db.UsersTable
import com.sujana.backend.plugins.FIREBASE_AUTH
import com.sujana.backend.plugins.UserPrincipal
import com.sujana.shared.dto.RegisterTokenRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

@Serializable
private data class PrefUpdateRequest(val category: String, val muted: Boolean)

fun Route.notificationRoutes() {
    authenticate(FIREBASE_AUTH) {

        post("devices/token") {
            val principal = call.principal<UserPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val body = call.receive<RegisterTokenRequest>()
            val userId = resolveUserId(principal) ?: return@post call.respond(HttpStatusCode.NotFound)
            NotificationService.registerToken(userId, body)
            call.respond(HttpStatusCode.NoContent)
        }

        get("notifications") {
            val principal = call.principal<UserPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val userId = resolveUserId(principal) ?: return@get call.respond(HttpStatusCode.NotFound)
            val page = call.request.queryParameters["page"]?.toIntOrNull()?.coerceAtLeast(1) ?: 1
            val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull()
                ?.coerceIn(1, 50) ?: 20
            call.respond(NotificationService.listNotifications(userId, page, pageSize))
        }

        post("notifications/{id}/read") {
            val principal = call.principal<UserPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"]?.let(UUID::fromString)
                ?: return@post call.respond(HttpStatusCode.BadRequest)
            val userId = resolveUserId(principal) ?: return@post call.respond(HttpStatusCode.NotFound)
            val updated = NotificationService.markRead(userId, id)
            if (updated) call.respond(HttpStatusCode.NoContent)
            else call.respond(HttpStatusCode.NotFound)
        }

        post("notifications/read-all") {
            val principal = call.principal<UserPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val userId = resolveUserId(principal) ?: return@post call.respond(HttpStatusCode.NotFound)
            NotificationService.markAllRead(userId)
            call.respond(HttpStatusCode.NoContent)
        }

        get("notification-prefs") {
            val principal = call.principal<UserPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val userId = resolveUserId(principal) ?: return@get call.respond(HttpStatusCode.NotFound)
            call.respond(NotificationService.getPrefs(userId))
        }

        put("notification-prefs") {
            val principal = call.principal<UserPrincipal>()
                ?: return@put call.respond(HttpStatusCode.Unauthorized)
            val userId = resolveUserId(principal) ?: return@put call.respond(HttpStatusCode.NotFound)
            val body = call.receive<PrefUpdateRequest>()
            NotificationService.updatePref(userId, body.category, body.muted)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}

private fun resolveUserId(principal: UserPrincipal): UUID? = transaction {
    UsersTable.selectAll()
        .where { UsersTable.firebaseUid eq principal.uid }
        .singleOrNull()
        ?.get(UsersTable.id)
}
