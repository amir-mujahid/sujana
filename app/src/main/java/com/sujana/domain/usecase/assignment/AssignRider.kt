package com.sujana.domain.usecase.assignment

import com.sujana.core.common.AppResult
import com.sujana.domain.model.Assignment
import com.sujana.domain.repository.IAssignmentRepository
import javax.inject.Inject

class AssignRider @Inject constructor(
    private val repo: IAssignmentRepository,
) {
    suspend operator fun invoke(requestId: String, riderId: String): AppResult<Assignment> =
        repo.createAssignment(requestId, riderId)
}
