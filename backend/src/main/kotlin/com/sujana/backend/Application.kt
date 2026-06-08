package com.sujana.backend

import com.sujana.backend.feature.health.healthRoutes
import com.sujana.backend.plugins.configureDatabase
import com.sujana.backend.plugins.configureFirebaseAuth
import com.sujana.backend.plugins.configureSerialization
import com.sujana.backend.plugins.configureStatusPages
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.routing.routing
import org.slf4j.event.Level

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureStatusPages()
    configureDatabase()
    configureFirebaseAuth()
    install(CallLogging) {
        level = Level.INFO
    }
    routing {
        healthRoutes()
    }
}
