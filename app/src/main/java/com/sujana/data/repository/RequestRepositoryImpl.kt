package com.sujana.data.repository

import com.sujana.core.common.AppError
import com.sujana.core.common.AppResult
import com.sujana.core.network.SujanaApi
import com.sujana.domain.model.PickupRequest
import com.sujana.domain.model.School
import com.sujana.domain.repository.IRequestRepository
import com.sujana.shared.dto.CreateRequestRequest
import com.sujana.shared.dto.RequestDto
import com.sujana.shared.dto.SchoolDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RequestRepositoryImpl @Inject constructor(
    private val api: SujanaApi,
) : IRequestRepository {

    override suspend fun createRequest(
        pickupLat: Double,
        pickupLng: Double,
        pickupAddress: String,
        dropoffSchoolId: String?,
        notes: String?,
        photoUrl: String?,
    ): AppResult<PickupRequest> = runCatching {
        api.createRequest(
            CreateRequestRequest(
                pickupLat       = pickupLat,
                pickupLng       = pickupLng,
                pickupAddress   = pickupAddress,
                dropoffSchoolId = dropoffSchoolId,
                notes           = notes,
                photoUrl        = photoUrl,
            )
        ).toDomain()
    }.toAppResult()

    override suspend fun getMyRequests(): AppResult<List<PickupRequest>> = runCatching {
        api.getRequests().map { it.toDomain() }
    }.toAppResult()

    override suspend fun getAvailableRequests(): AppResult<List<PickupRequest>> = runCatching {
        api.getAvailableRequests().map { it.toDomain() }
    }.toAppResult()

    override suspend fun getRequestDetail(id: String): AppResult<PickupRequest> = runCatching {
        api.getRequest(id).toDomain()
    }.toAppResult()

    override suspend fun cancelRequest(id: String): AppResult<PickupRequest> = runCatching {
        api.cancelRequest(id).toDomain()
    }.toAppResult()

    override suspend fun acceptRequest(id: String): AppResult<PickupRequest> = runCatching {
        api.acceptRequest(id).toDomain()
    }.toAppResult()

    override suspend fun getSchools(): AppResult<List<School>> = runCatching {
        api.getSchools().map { it.toDomain() }
    }.toAppResult()
}

private fun RequestDto.toDomain() = PickupRequest(
    id               = id,
    type             = type,
    requesterId      = requesterId,
    status           = status,
    pickupLat        = pickupLat,
    pickupLng        = pickupLng,
    pickupAddress    = pickupAddress,
    dropoffSchoolId  = dropoffSchoolId,
    dropoffSchoolName = dropoffSchoolName,
    notes            = notes,
    photoUrl         = photoUrl,
    createdAt        = createdAt,
    updatedAt        = updatedAt,
)

private fun SchoolDto.toDomain() = School(id = id, name = name, lat = lat, lng = lng)

private fun <T> Result<T>.toAppResult(): AppResult<T> = fold(
    onSuccess = { AppResult.Success(it) },
    onFailure = { AppResult.Error(it.toAppError()) },
)

private fun Throwable.toAppError(): AppError = when (this) {
    is retrofit2.HttpException -> when (code()) {
        401  -> AppError.Unauthorized()
        404  -> AppError.NotFound("request")
        else -> AppError.Server(code().toString(), message() ?: "Server error", code())
    }
    else -> AppError.Network(localizedMessage ?: "Network error", this)
}
