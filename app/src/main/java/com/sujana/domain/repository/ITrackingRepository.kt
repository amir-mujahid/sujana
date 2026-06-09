package com.sujana.domain.repository

import com.sujana.domain.model.TrackingUpdate
import kotlinx.coroutines.flow.Flow

interface ITrackingRepository {
    fun observeTracking(assignmentId: String): Flow<TrackingUpdate?>
    suspend fun writeTrackingUpdate(
        assignmentId: String,
        lat: Double,
        lng: Double,
        heading: Float,
        riderId: String,
    )
    suspend fun clearTracking(assignmentId: String)
}
