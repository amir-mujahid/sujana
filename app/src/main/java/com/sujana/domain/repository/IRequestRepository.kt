package com.sujana.domain.repository

import com.sujana.core.common.AppResult
import com.sujana.domain.model.PickupRequest
import com.sujana.domain.model.School

interface IRequestRepository {
    suspend fun createRequest(
        pickupLat: Double,
        pickupLng: Double,
        pickupAddress: String,
        dropoffSchoolId: String? = null,
        notes: String? = null,
        photoUrl: String? = null,
        type: com.sujana.shared.RequestType = com.sujana.shared.RequestType.CONTRIBUTOR,
        scheduledFor: String? = null,
    ): AppResult<PickupRequest>

    suspend fun getMyRequests(): AppResult<List<PickupRequest>>
    suspend fun getAvailableRequests(): AppResult<List<PickupRequest>>
    suspend fun getNearbyRequests(lat: Double, lng: Double, radiusMetres: Double = 5000.0): AppResult<List<PickupRequest>>
    suspend fun getRequestDetail(id: String): AppResult<PickupRequest>
    suspend fun cancelRequest(id: String): AppResult<PickupRequest>
    suspend fun acceptRequest(id: String): AppResult<PickupRequest>
    suspend fun getSchools(): AppResult<List<School>>
}
