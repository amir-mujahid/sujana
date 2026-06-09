package com.sujana.core.network

import com.sujana.shared.dto.AssignmentDto
import com.sujana.shared.dto.CreateAssignmentRequest
import com.sujana.shared.dto.CreateRequestRequest
import com.sujana.shared.dto.HealthDto
import com.sujana.shared.dto.MeResponse
import com.sujana.shared.dto.RequestDto
import com.sujana.shared.dto.SchoolDto
import com.sujana.shared.dto.TransitionRequest
import com.sujana.shared.dto.UserDto
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

    @GET("requests/available")
    suspend fun getAvailableRequests(): List<RequestDto>

    @GET("requests/nearby")
    suspend fun getNearbyRequests(
        @retrofit2.http.Query("lat") lat: Double,
        @retrofit2.http.Query("lng") lng: Double,
        @retrofit2.http.Query("radius") radius: Double = 5000.0,
    ): List<RequestDto>

    @POST("requests/{id}/accept")
    suspend fun acceptRequest(@Path("id") id: String): RequestDto

    @GET("riders")
    suspend fun getRiders(): List<UserDto>

    @POST("assignments")
    suspend fun createAssignment(@Body body: CreateAssignmentRequest): AssignmentDto

    @GET("assignments")
    suspend fun getAssignments(): List<AssignmentDto>

    @GET("assignments/{id}")
    suspend fun getAssignment(@Path("id") id: String): AssignmentDto

    @POST("assignments/{id}/transition")
    suspend fun transitionAssignment(
        @Path("id") id: String,
        @Body body: TransitionRequest,
    ): AssignmentDto
}
