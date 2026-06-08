package com.sujana.domain.usecase.assignment

import com.sujana.core.common.AppResult
import com.sujana.domain.model.Assignment
import com.sujana.domain.repository.IAssignmentRepository
import com.sujana.shared.AssignmentStatus
import javax.inject.Inject

class TransitionAssignment @Inject constructor(
    private val repo: IAssignmentRepository,
) {
    suspend operator fun invoke(assignmentId: String, newStatus: AssignmentStatus): AppResult<Assignment> =
        repo.transitionAssignment(assignmentId, newStatus)
}
