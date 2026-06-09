package com.sujana.data.repository

import com.sujana.core.common.AppError
import com.sujana.core.common.AppResult
import com.sujana.core.network.SujanaApi
import com.sujana.domain.model.Assignment
import com.sujana.domain.model.PickupRequest
import com.sujana.domain.model.UserProfile
import com.sujana.domain.repository.IAssignmentRepository
import com.sujana.shared.AssignmentStatus
import com.sujana.shared.dto.AssignmentDto
import com.sujana.shared.dto.CreateAssignmentRequest
import com.sujana.shared.dto.RequestDto
import com.sujana.shared.dto.TransitionRequest
import com.sujana.shared.dto.UserDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssignmentRepositoryImpl @Inject constructor(
    private val api: SujanaApi,
) : IAssignmentRepository {

    override suspend fun createAssignment(requestId: String, riderId: String): AppResult<Assignment> =
        runCatching {
            api.createAssignment(CreateAssignmentRequest(requestId = requestId, riderId = riderId)).toDomain()
        }.toAppResult()

    override suspend fun getAssignment(id: String): AppResult<Assignment> = runCatching {
        api.getAssignment(id).toDomain()
    }.toAppResult()

    override suspend fun getAssignments(): AppResult<List<Assignment>> = runCatching {
        api.getAssignments().map { it.toDomain() }
    }.toAppResult()

    override suspend fun transitionAssignment(
        assignmentId: String,
        newStatus: AssignmentStatus,
    ): AppResult<Assignment> = runCatching {
        api.transitionAssignment(assignmentId, TransitionRequest(status = newStatus)).toDomain()
    }.toAppResult()

    override suspend fun getRiders(): AppResult<List<UserProfile>> = runCatching {
        api.getRiders().map { it.toDomain() }
    }.toAppResult()
}

private fun AssignmentDto.toDomain() = Assignment(
    id           = id,
    requestId    = requestId,
    riderId      = riderId,
    riderName    = riderName,
    dispatcherId = dispatcherId,
    status       = status,
    request      = request.toDomain(),
    assignedAt   = assignedAt,
    acceptedAt   = acceptedAt,
    collectedAt  = collectedAt,
    deliveredAt  = deliveredAt,
    completedAt  = completedAt,
)

private fun RequestDto.toDomain() = PickupRequest(
    id                = id,
    type              = type,
    requesterId       = requesterId,
    status            = status,
    pickupLat         = pickupLat,
    pickupLng         = pickupLng,
    pickupAddress     = pickupAddress,
    dropoffSchoolId   = dropoffSchoolId,
    dropoffSchoolName = dropoffSchoolName,
    dropoffSchoolLat  = dropoffSchoolLat,
    dropoffSchoolLng  = dropoffSchoolLng,
    assignmentId      = assignmentId,
    notes             = notes,
    photoUrl          = photoUrl,
    createdAt         = createdAt,
    updatedAt         = updatedAt,
)

private fun UserDto.toDomain() = UserProfile(id = id, name = name, email = email)

private fun <T> Result<T>.toAppResult(): AppResult<T> = fold(
    onSuccess = { AppResult.Success(it) },
    onFailure = { AppResult.Error(it.toAppError()) },
)

private fun Throwable.toAppError(): AppError = when (this) {
    is retrofit2.HttpException -> when (code()) {
        401  -> AppError.Unauthorized()
        404  -> AppError.NotFound("assignment")
        else -> AppError.Server(code().toString(), message() ?: "Server error", code())
    }
    else -> AppError.Network(localizedMessage ?: "Network error", this)
}
