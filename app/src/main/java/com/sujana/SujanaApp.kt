package com.sujana

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class SujanaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                TRACKING_CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW,
            ).apply { description = "Active while tracking your delivery position" }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                NOTIFICATIONS_CHANNEL_ID,
                "Updates",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply { description = "Request and assignment status updates" }
        )
    }

    companion object {
        const val TRACKING_CHANNEL_ID = "tracking_service"
        const val NOTIFICATIONS_CHANNEL_ID = "sujana_notifications"
    }
}
