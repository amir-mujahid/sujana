package com.sujana.shared

import kotlinx.serialization.Serializable

@Serializable
enum class AssignmentStatus {
    ASSIGNED,
    ACCEPTED,
    COLLECTED,
    DELIVERED,
    COMPLETED,
    CANCELLED,
}
