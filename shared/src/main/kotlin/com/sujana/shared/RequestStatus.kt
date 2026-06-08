package com.sujana.shared

import kotlinx.serialization.Serializable

@Serializable
enum class RequestStatus {
    PENDING,
    ASSIGNED,
    COLLECTED,
    DELIVERED,
    COMPLETED,
    CANCELLED,
}
