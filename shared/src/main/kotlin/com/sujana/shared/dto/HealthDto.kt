package com.sujana.shared.dto

import kotlinx.serialization.Serializable

@Serializable
data class HealthDto(
    val status: String,
    val dbConnected: Boolean,
    val time: String,
)
