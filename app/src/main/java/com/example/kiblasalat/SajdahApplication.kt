package com.example.kiblasalat

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SajdahApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Salat Notifications"
            val descriptionText = "Triggers alarms and alerts for daily Salat prayer times"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("salat_alerts", name, importance).apply {
                description = descriptionText
                enableLights(true)
                enableVibration(true)
            }
            
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
