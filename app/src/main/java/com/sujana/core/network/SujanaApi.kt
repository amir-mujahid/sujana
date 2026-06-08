package com.sujana.core.network

import com.sujana.shared.dto.HealthDto
import com.sujana.shared.dto.MeResponse
import retrofit2.http.GET

interface SujanaApi {
    @GET("health")
    suspend fun health(): HealthDto

    @GET("auth/me")
    suspend fun getMe(): MeResponse
}
