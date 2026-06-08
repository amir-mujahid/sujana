package com.sujana.domain.usecase.request

import com.sujana.core.common.AppResult
import com.sujana.domain.model.PickupRequest
import com.sujana.domain.repository.IRequestRepository
import javax.inject.Inject

class CreatePickupRequest @Inject constructor(
    private val repository: IRequestRepository,
) {
    suspend operator fun invoke(
        pickupLat: Double,
        pickupLng: Double,
        pickupAddress: String,
        dropoffSchoolId: String?,
        notes: String?,
        photoUrl: String?,
    ): AppResult<PickupRequest> = repository.createRequest(
        pickupLat, pickupLng, pickupAddress, dropoffSchoolId, notes, photoUrl,
    )
}
