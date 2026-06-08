package com.sujana.domain.model

import com.sujana.shared.Role

data class User(
    val id: String,
    val firebaseUid: String,
    val name: String,
    val email: String,
    val role: Role,
    val tenantId: String? = null,
    val phone: String? = null,
)
