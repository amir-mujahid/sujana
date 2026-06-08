package com.sujana.backend.feature.health

import com.sujana.shared.dto.HealthDto
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

fun Route.healthRoutes() {
    get("/health") {
        val dbConnected = runCatching {
            transaction { exec("SELECT 1") {} }
        }.isSuccess

        call.respond(
            HealthDto(
                status = if (dbConnected) "ok" else "degraded",
                dbConnected = dbConnected,
                time = Instant.now().toString(),
            ),
        )
    }
}
