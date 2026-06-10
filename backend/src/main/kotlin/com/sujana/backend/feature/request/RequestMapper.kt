package com.sujana.backend.feature.request

import com.sujana.backend.db.RequestsTable
import com.sujana.shared.RequestStatus
import com.sujana.shared.RequestType
import com.sujana.shared.dto.RequestDto
import org.jetbrains.exposed.sql.ResultRow

data class SchoolInfo(val name: String?, val lat: Double?, val lng: Double?)

fun ResultRow.toRequestDto(school: SchoolInfo?, assignmentId: String? = null) = RequestDto(
    id                = this[RequestsTable.id].toString(),
    type              = RequestType.valueOf(this[RequestsTable.type]),
    requesterId       = this[RequestsTable.requesterId].toString(),
    status            = RequestStatus.valueOf(this[RequestsTable.status]),
    pickupLat         = this[RequestsTable.pickupLat],
    pickupLng         = this[RequestsTable.pickupLng],
    pickupAddress     = this[RequestsTable.pickupAddress],
    dropoffSchoolId   = this[RequestsTable.dropoffSchoolId]?.toString(),
    dropoffSchoolName = school?.name,
    dropoffSchoolLat  = school?.lat,
    dropoffSchoolLng  = school?.lng,
    assignmentId      = assignmentId,
    notes             = this[RequestsTable.notes],
    photoUrl          = this[RequestsTable.photoUrl],
    scheduledFor      = this[RequestsTable.scheduledFor]?.toString(),
    requesterSchoolId = this[RequestsTable.requesterSchoolId]?.toString(),
    createdAt         = this[RequestsTable.createdAt].toString(),
    updatedAt         = this[RequestsTable.updatedAt].toString(),
)
