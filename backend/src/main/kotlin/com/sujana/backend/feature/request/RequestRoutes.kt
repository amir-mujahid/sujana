package com.sujana.backend.feature.request

import com.sujana.backend.plugins.FIREBASE_AUTH
import com.sujana.backend.plugins.UserPrincipal
import com.sujana.shared.dto.CreateRequestRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

fun Route.requestRoutes() {
    authenticate(FIREBASE_AUTH) {
        get("schools") {
            val principal = call.principal<UserPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            call.respond(RequestService.listSchools())
        }

        post("requests") {
            val principal = call.principal<UserPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val body = call.receive<CreateRequestRequest>()
            val dto = RequestService.createRequest(principal, body)
            call.respond(HttpStatusCode.Created, dto)
        }

        get("requests") {
            val principal = call.principal<UserPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            call.respond(RequestService.listRequests(principal))
        }

        get("requests/{id}") {
            val principal = call.principal<UserPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            call.respond(RequestService.getRequest(principal, id))
        }

        post("requests/{id}/cancel") {
            val principal = call.principal<UserPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            call.respond(RequestService.cancelRequest(principal, id))
        }

        get("requests/available") {
            val principal = call.principal<UserPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            call.respond(RequestService.listAvailableRequests(principal))
        }

        get("requests/nearby") {
            val principal = call.principal<UserPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val lat = call.request.queryParameters["lat"]?.toDoubleOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "lat required"))
            val lng = call.request.queryParameters["lng"]?.toDoubleOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "lng required"))
            val radius = call.request.queryParameters["radius"]?.toDoubleOrNull() ?: 5000.0
            call.respond(RequestService.listNearbyRequests(principal, lat, lng, radius))
        }

        post("requests/{id}/accept") {
            val principal = call.principal<UserPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            call.respond(RequestService.acceptRequest(principal, id))
        }
    }
}
