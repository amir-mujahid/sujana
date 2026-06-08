package com.sujana.core.network

import com.sujana.shared.dto.HealthDto
import retrofit2.http.GET

interface SujanaApi {
    @GET("health")
    suspend fun health(): HealthDto
}
