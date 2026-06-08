package com.sujana.domain.repository

import com.sujana.core.common.AppResult
import com.sujana.domain.model.Assignment
import com.sujana.domain.model.UserProfile
import com.sujana.shared.AssignmentStatus

interface IAssignmentRepository {
    suspend fun createAssignment(requestId: String, riderId: String): AppResult<Assignment>
    suspend fun getAssignments(): AppResult<List<Assignment>>
    suspend fun transitionAssignment(assignmentId: String, newStatus: AssignmentStatus): AppResult<Assignment>
    suspend fun getRiders(): AppResult<List<UserProfile>>
}
