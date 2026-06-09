package com.sujana.domain.model

data class TrackingUpdate(
    val lat: Double,
    val lng: Double,
    val heading: Float,
    val ts: Long,
    val riderId: String,
)
