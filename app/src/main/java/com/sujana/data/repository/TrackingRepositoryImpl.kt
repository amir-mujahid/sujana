package com.sujana.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.sujana.domain.model.TrackingUpdate
import com.sujana.domain.repository.ITrackingRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackingRepositoryImpl @Inject constructor() : ITrackingRepository {

    private val db get() = Firebase.database.reference

    override fun observeTracking(assignmentId: String): Flow<TrackingUpdate?> = callbackFlow {
        val ref = db.child("tracking").child(assignmentId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    trySend(null)
                    return
                }
                val lat     = snapshot.child("lat").getValue(Double::class.java) ?: return
                val lng     = snapshot.child("lng").getValue(Double::class.java) ?: return
                val heading = (snapshot.child("heading").getValue(Double::class.java) ?: 0.0).toFloat()
                val ts      = snapshot.child("ts").getValue(Long::class.java) ?: 0L
                val riderId = snapshot.child("riderId").getValue(String::class.java) ?: return
                trySend(TrackingUpdate(lat = lat, lng = lng, heading = heading, ts = ts, riderId = riderId))
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override suspend fun writeTrackingUpdate(
        assignmentId: String,
        lat: Double,
        lng: Double,
        heading: Float,
        riderId: String,
    ) {
        db.child("tracking").child(assignmentId).setValue(
            mapOf(
                "lat"     to lat,
                "lng"     to lng,
                "heading" to heading.toDouble(),
                "ts"      to System.currentTimeMillis(),
                "riderId" to riderId,
            )
        )
    }

    override suspend fun clearTracking(assignmentId: String) {
        db.child("tracking").child(assignmentId).removeValue()
    }
}
