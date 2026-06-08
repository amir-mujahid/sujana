package com.sujana.domain.usecase.request

import com.sujana.core.common.AppResult
import com.sujana.domain.model.PickupRequest
import com.sujana.domain.repository.IRequestRepository
import javax.inject.Inject

class GetMyRequests @Inject constructor(
    private val repository: IRequestRepository,
) {
    suspend operator fun invoke(): AppResult<List<PickupRequest>> = repository.getMyRequests()
}
