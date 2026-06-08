package com.sujana.domain.usecase.assignment

import com.sujana.core.common.AppResult
import com.sujana.domain.model.UserProfile
import com.sujana.domain.repository.IAssignmentRepository
import javax.inject.Inject

class GetAvailableRiders @Inject constructor(
    private val repo: IAssignmentRepository,
) {
    suspend operator fun invoke(): AppResult<List<UserProfile>> = repo.getRiders()
}
