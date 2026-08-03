package com.example.kiblasalat.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.kiblasalat.service.AdhanAlarmHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    AdhanAlarmHelper.schedulePrayerAlarms(context)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
