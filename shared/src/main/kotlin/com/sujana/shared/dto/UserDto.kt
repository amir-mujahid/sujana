package com.sujana.shared.dto

import com.sujana.shared.Role
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val role: Role,
)
