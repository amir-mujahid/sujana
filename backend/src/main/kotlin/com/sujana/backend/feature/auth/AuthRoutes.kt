package com.sujana.backend.feature.auth

import com.sujana.backend.plugins.FIREBASE_AUTH
import com.sujana.backend.plugins.UserPrincipal
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.authRoutes() {
    authenticate(FIREBASE_AUTH) {
        get("auth/me") {
            val principal = call.principal<UserPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val me = AuthService.upsertAndGetMe(principal)
            call.respond(me)
        }
    }
}
