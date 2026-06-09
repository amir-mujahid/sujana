package com.sujana.feature.tracking

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.sujana.MainActivity
import com.sujana.R
import com.sujana.SujanaApp.Companion.TRACKING_CHANNEL_ID
import com.sujana.domain.repository.ITrackingRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LocationTrackingService : Service() {

    @Inject lateinit var trackingRepository: ITrackingRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private var assignmentId: String? = null
    private var riderId: String? = null

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                assignmentId = intent.getStringExtra(EXTRA_ASSIGNMENT_ID)
                riderId = intent.getStringExtra(EXTRA_RIDER_ID)
                startForeground(NOTIFICATION_ID, buildNotification())
                requestLocationUpdates()
            }
            ACTION_STOP -> stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun requestLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000L)
            .setMinUpdateDistanceMeters(20f)
            .build()
        val cb = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                val aid = assignmentId ?: return
                val rid = riderId ?: return
                serviceScope.launch {
                    trackingRepository.writeTrackingUpdate(
                        assignmentId = aid,
                        lat          = loc.latitude,
                        lng          = loc.longitude,
                        heading      = loc.bearing,
                        riderId      = rid,
                    )
                }
            }
        }
        locationCallback = cb
        try {
            fusedLocationClient.requestLocationUpdates(request, cb, mainLooper)
        } catch (_: SecurityException) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        val aid = assignmentId
        if (aid != null) {
            Firebase.database.reference.child("tracking").child(aid).removeValue()
        }
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        val tapIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, tapIntent, PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, TRACKING_CHANNEL_ID)
            .setContentTitle("I-Sujana")
            .setContentText("Sharing your location for active delivery")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val ACTION_START          = "com.sujana.tracking.START"
        const val ACTION_STOP           = "com.sujana.tracking.STOP"
        const val EXTRA_ASSIGNMENT_ID   = "assignment_id"
        const val EXTRA_RIDER_ID        = "rider_id"
        private const val NOTIFICATION_ID = 1001

        fun startIntent(context: Context, assignmentId: String, riderId: String) =
            Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_ASSIGNMENT_ID, assignmentId)
                putExtra(EXTRA_RIDER_ID, riderId)
            }

        fun stopIntent(context: Context) =
            Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_STOP
            }
    }
}
