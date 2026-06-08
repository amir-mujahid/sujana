package com.sujana.domain.usecase.request

import com.sujana.core.common.AppResult
import com.sujana.domain.model.PickupRequest
import com.sujana.domain.repository.IRequestRepository
import javax.inject.Inject

class GetAvailablePickups @Inject constructor(
    private val repo: IRequestRepository,
) {
    suspend operator fun invoke(): AppResult<List<PickupRequest>> = repo.getAvailableRequests()
}
