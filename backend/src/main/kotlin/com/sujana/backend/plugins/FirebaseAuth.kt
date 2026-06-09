package com.sujana.backend.plugins

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.bearer
import java.io.ByteArrayInputStream

data class UserPrincipal(
    val uid: String,
    val email: String?,
    val name: String?,
    val emailVerified: Boolean,
    val role: String?,
    val tenantId: String?,
)

const val FIREBASE_AUTH = "firebase"

fun Application.configureFirebaseAuth() {
    val serviceAccountJson = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON")
    if (serviceAccountJson != null) {
        val credentials = GoogleCredentials.fromStream(
            ByteArrayInputStream(serviceAccountJson.toByteArray()),
        )
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(
                FirebaseOptions.builder().setCredentials(credentials).build(),
            )
        }
        environment.log.info("Firebase Admin SDK initialized")
    } else {
        environment.log.warn("FIREBASE_SERVICE_ACCOUNT_JSON not set — auth verification disabled for local dev")
    }

    val isLocalDev = System.getenv("SUJANA_ENV") == "local"

    install(Authentication) {
        bearer(FIREBASE_AUTH) {
            authenticate { tokenCredential ->
                if (FirebaseApp.getApps().isEmpty() && isLocalDev) {
                    // Local dev mode only (SUJANA_ENV=local): accept any token, return stub principal
                    return@authenticate UserPrincipal(
                        uid = "dev-uid",
                        email = "dev@sujana.local",
                        name = "Dev User",
                        emailVerified = true,
                        role = null,
                        tenantId = null,
                    )
                }
                try {
                    val decoded = FirebaseAuth.getInstance().verifyIdToken(tokenCredential.token)
                    UserPrincipal(
                        uid = decoded.uid,
                        email = decoded.email,
                        name = decoded.name,
                        emailVerified = decoded.isEmailVerified,
                        role = decoded.claims["role"] as? String,
                        tenantId = decoded.claims["tenantId"] as? String,
                    )
                } catch (_: FirebaseAuthException) {
                    null
                }
            }
        }
    }
}
