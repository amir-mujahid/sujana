package com.sujana.domain.usecase.assignment

import com.sujana.core.common.AppResult
import com.sujana.domain.model.Assignment
import com.sujana.domain.repository.IAssignmentRepository
import javax.inject.Inject

class GetDispatchQueue @Inject constructor(
    private val repo: IAssignmentRepository,
) {
    suspend operator fun invoke(): AppResult<List<Assignment>> = repo.getAssignments()
}
