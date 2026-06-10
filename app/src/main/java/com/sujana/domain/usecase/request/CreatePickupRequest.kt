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
        dropoffSchoolId: String? = null,
        notes: String? = null,
        photoUrl: String? = null,
        type: com.sujana.shared.RequestType = com.sujana.shared.RequestType.CONTRIBUTOR,
        scheduledFor: String? = null,
    ): AppResult<PickupRequest> = repository.createRequest(
        pickupLat, pickupLng, pickupAddress, dropoffSchoolId, notes, photoUrl, type, scheduledFor,
    )
}
