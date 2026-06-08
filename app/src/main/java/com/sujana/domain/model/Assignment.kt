package com.sujana.domain.model

import com.sujana.shared.AssignmentStatus

data class Assignment(
    val id: String,
    val requestId: String,
    val riderId: String,
    val riderName: String?,
    val dispatcherId: String?,
    val status: AssignmentStatus,
    val request: PickupRequest,
    val assignedAt: String,
    val acceptedAt: String?,
    val collectedAt: String?,
    val deliveredAt: String?,
    val completedAt: String?,
)

data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
)
