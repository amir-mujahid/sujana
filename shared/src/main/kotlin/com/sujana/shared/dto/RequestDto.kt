package com.sujana.shared.dto

import com.sujana.shared.RequestStatus
import com.sujana.shared.RequestType
import kotlinx.serialization.Serializable

@Serializable
data class CreateRequestRequest(
    val type: RequestType = RequestType.CONTRIBUTOR,
    val pickupLat: Double,
    val pickupLng: Double,
    val pickupAddress: String,
    val dropoffSchoolId: String? = null,
    val notes: String? = null,
    val photoUrl: String? = null,
)

@Serializable
data class RequestDto(
    val id: String,
    val type: RequestType,
    val requesterId: String,
    val status: RequestStatus,
    val pickupLat: Double,
    val pickupLng: Double,
    val pickupAddress: String,
    val dropoffSchoolId: String?,
    val dropoffSchoolName: String?,
    val notes: String?,
    val photoUrl: String?,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class SchoolDto(
    val id: String,
    val name: String,
    val lat: Double,
    val lng: Double,
)
