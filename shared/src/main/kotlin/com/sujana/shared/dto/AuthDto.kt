package com.sujana.shared.dto

import com.sujana.shared.Role
import kotlinx.serialization.Serializable

@Serializable
data class MeResponse(
    val id: String,
    val firebaseUid: String,
    val name: String,
    val email: String,
    val role: Role,
    val tenantId: String? = null,
    val phone: String? = null,
)
