package com.sujana.domain.repository

import com.sujana.core.common.AppResult
import com.sujana.domain.model.PickupRequest
import com.sujana.domain.model.School

interface IRequestRepository {
    suspend fun createRequest(
        pickupLat: Double,
        pickupLng: Double,
        pickupAddress: String,
        dropoffSchoolId: String?,
        notes: String?,
        photoUrl: String?,
    ): AppResult<PickupRequest>

    suspend fun getMyRequests(): AppResult<List<PickupRequest>>
    suspend fun getRequestDetail(id: String): AppResult<PickupRequest>
    suspend fun cancelRequest(id: String): AppResult<PickupRequest>
    suspend fun getSchools(): AppResult<List<School>>
}
