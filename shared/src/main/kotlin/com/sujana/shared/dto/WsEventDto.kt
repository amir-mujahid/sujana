package com.sujana.shared.dto

import com.sujana.shared.WsEventType
import kotlinx.serialization.Serializable

@Serializable
data class WsEventDto(
    val event: WsEventType,
    val entityId: String,
    val status: String,
)
