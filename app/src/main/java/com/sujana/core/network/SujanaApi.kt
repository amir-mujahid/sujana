package com.sujana.core.network

import com.sujana.shared.dto.CreateRequestRequest
import com.sujana.shared.dto.HealthDto
import com.sujana.shared.dto.MeResponse
import com.sujana.shared.dto.RequestDto
import com.sujana.shared.dto.SchoolDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SujanaApi {
    @GET("health")
    suspend fun health(): HealthDto

    @GET("auth/me")
    suspend fun getMe(): MeResponse

    @GET("schools")
    suspend fun getSchools(): List<SchoolDto>

    @POST("requests")
    suspend fun createRequest(@Body body: CreateRequestRequest): RequestDto

    @GET("requests")
    suspend fun getRequests(): List<RequestDto>

    @GET("requests/{id}")
    suspend fun getRequest(@Path("id") id: String): RequestDto

    @POST("requests/{id}/cancel")
    suspend fun cancelRequest(@Path("id") id: String): RequestDto
}
