package com.sujana.shared.dto

import com.sujana.shared.AssignmentStatus
import kotlinx.serialization.Serializable

@Serializable
data class CreateAssignmentRequest(
    val requestId: String,
    val riderId: String,
)

@Serializable
data class TransitionRequest(
    val status: AssignmentStatus,
)

@Serializable
data class AssignmentDto(
    val id: String,
    val requestId: String,
    val riderId: String,
    val riderName: String?,
    val dispatcherId: String?,
    val status: AssignmentStatus,
    val request: RequestDto,
    val assignedAt: String,
    val acceptedAt: String? = null,
    val collectedAt: String? = null,
    val deliveredAt: String? = null,
    val completedAt: String? = null,
)
