package com.example.kiblasalat.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdhanAlarmService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default)

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == "SCHEDULE_ALARMS") {
            serviceScope.launch {
                try {
                    AdhanAlarmHelper.schedulePrayerAlarms(this@AdhanAlarmService)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    stopSelf(startId)
                }
            }
        } else {
            stopSelf(startId)
        }
        return START_NOT_STICKY
    }
}
