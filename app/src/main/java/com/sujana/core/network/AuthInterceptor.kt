package com.sujana.core.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenProvider: TokenProvider,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider.getToken()
        val request = if (token != null) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}

interface TokenProvider {
    fun getToken(): String?
}

// Stub used until Stage 1 wires in FirebaseAuth.currentUser.getIdToken(false)
@Singleton
class StubTokenProvider @Inject constructor() : TokenProvider {
    override fun getToken(): String? = null
}
