package com.sujana.domain.model

import com.sujana.shared.RequestStatus
import com.sujana.shared.RequestType

data class PickupRequest(
    val id: String,
    val type: RequestType,
    val requesterId: String,
    val status: RequestStatus,
    val pickupLat: Double,
    val pickupLng: Double,
    val pickupAddress: String,
    val dropoffSchoolId: String?,
    val dropoffSchoolName: String?,
    val dropoffSchoolLat: Double? = null,
    val dropoffSchoolLng: Double? = null,
    val assignmentId: String? = null,
    val notes: String?,
    val photoUrl: String?,
    val scheduledFor: String? = null,
    val requesterSchoolId: String? = null,
    val createdAt: String,
    val updatedAt: String,
)

data class School(
    val id: String,
    val name: String,
    val lat: Double,
    val lng: Double,
)
