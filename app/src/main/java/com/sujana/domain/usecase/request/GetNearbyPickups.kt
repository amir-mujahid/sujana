package com.sujana.domain.usecase.request

import com.sujana.core.common.AppResult
import com.sujana.domain.model.PickupRequest
import com.sujana.domain.repository.IRequestRepository
import javax.inject.Inject

class GetNearbyPickups @Inject constructor(
    private val repo: IRequestRepository,
) {
    suspend operator fun invoke(
        lat: Double,
        lng: Double,
        radiusMetres: Double = RADIUS_METRES,
    ): AppResult<List<PickupRequest>> = repo.getNearbyRequests(lat, lng, radiusMetres)

    companion object {
        const val RADIUS_METRES = 10_000.0
    }
}
