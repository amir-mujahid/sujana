package com.sujana.domain.usecase.assignment

import com.sujana.core.common.AppResult
import com.sujana.domain.model.PickupRequest
import com.sujana.domain.repository.IRequestRepository
import javax.inject.Inject

class SelfAssignRequest @Inject constructor(
    private val repo: IRequestRepository,
) {
    suspend operator fun invoke(requestId: String): AppResult<PickupRequest> =
        repo.acceptRequest(requestId)
}
