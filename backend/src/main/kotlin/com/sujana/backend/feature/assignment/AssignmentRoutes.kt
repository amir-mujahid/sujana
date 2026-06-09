package com.sujana.backend.feature.assignment

import com.sujana.backend.feature.notification.NotificationService
import com.sujana.backend.plugins.FIREBASE_AUTH
import com.sujana.backend.plugins.UserPrincipal
import com.sujana.shared.dto.CreateAssignmentRequest
import com.sujana.shared.dto.TransitionRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import java.util.UUID

fun Route.assignmentRoutes() {
    authenticate(FIREBASE_AUTH) {
        get("riders") {
            call.principal<UserPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            call.respond(AssignmentService.listRiders())
        }

        post("assignments") {
            val principal = call.principal<UserPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val body = call.receive<CreateAssignmentRequest>()
            val dto = AssignmentService.createAssignment(principal, body)
            NotificationService.onNewAssignment(
                assignmentId = UUID.fromString(dto.id),
                riderId = UUID.fromString(dto.riderId),
            )
            call.respond(HttpStatusCode.Created, dto)
        }

        get("assignments") {
            val principal = call.principal<UserPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            call.respond(AssignmentService.listAssignments(principal))
        }

        get("assignments/{id}") {
            val principal = call.principal<UserPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"]
                ?: return@get call.respond(HttpStatusCode.BadRequest)
            call.respond(AssignmentService.getAssignment(principal, id))
        }

        post("assignments/{id}/transition") {
            val principal = call.principal<UserPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"]
                ?: return@post call.respond(HttpStatusCode.BadRequest)
            val body = call.receive<TransitionRequest>()
            val dto = AssignmentService.transitionAssignment(principal, id, body)
            val requesterId = AssignmentService.requesterIdForAssignment(UUID.fromString(dto.id))
            if (requesterId != null) {
                NotificationService.onAssignmentStatusChanged(
                    assignmentId = UUID.fromString(dto.id),
                    requestId = UUID.fromString(dto.requestId),
                    newStatus = dto.status,
                    riderId = UUID.fromString(dto.riderId),
                    requesterId = requesterId,
                )
            }
            call.respond(dto)
        }
    }
}
