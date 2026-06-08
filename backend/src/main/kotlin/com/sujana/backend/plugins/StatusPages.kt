package com.sujana.backend.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val error: ErrorBody)

@Serializable
data class ErrorBody(val code: String, val message: String)

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(ErrorBody("BAD_REQUEST", cause.message ?: "Invalid request")),
            )
        }
        exception<SecurityException> { call, cause ->
            call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(ErrorBody("UNAUTHORIZED", cause.message ?: "Unauthorized")),
            )
        }
        exception<NoSuchElementException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse(ErrorBody("NOT_FOUND", cause.message ?: "Resource not found")),
            )
        }
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(ErrorBody("INTERNAL_ERROR", "An unexpected error occurred")),
            )
        }
    }
}
