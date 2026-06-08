package com.sujana.backend.feature.assignment

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
            call.respond(HttpStatusCode.Created, dto)
        }

        get("assignments") {
            val principal = call.principal<UserPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            call.respond(AssignmentService.listAssignments(principal))
        }

        post("assignments/{id}/transition") {
            val principal = call.principal<UserPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"]
                ?: return@post call.respond(HttpStatusCode.BadRequest)
            val body = call.receive<TransitionRequest>()
            call.respond(AssignmentService.transitionAssignment(principal, id, body))
        }
    }
}
