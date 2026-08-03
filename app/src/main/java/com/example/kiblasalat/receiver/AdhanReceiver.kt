package com.example.kiblasalat.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.example.kiblasalat.MainActivity
import com.example.kiblasalat.data.database.SettingDao
import com.example.kiblasalat.service.AdhanAlarmHelper
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class AdhanReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ReceiverEntryPoint {
        fun getSettingDao(): SettingDao
    }

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val prayerName = intent.getStringExtra("prayer_name") ?: "Salat"

        val appContext = context.applicationContext
        val entryPoint = EntryPoints.get(appContext, ReceiverEntryPoint::class.java)
        val settingDao = entryPoint.getSettingDao()

        CoroutineScope(Dispatchers.Default).launch {
            try {
                val isEnabled = settingDao.getSettingValue("adhan_enabled_${prayerName.lowercase()}")?.toBoolean() ?: true
                val isSunrise = prayerName.equals("Sunrise", ignoreCase = true)

                if (isEnabled && !isSunrise) {
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                    val contentIntent = Intent(context, MainActivity::class.java)
                    val pendingContentIntent = PendingIntent.getActivity(
                        context,
                        0,
                        contentIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    // For notification sound, we default to silent channel or default if no file.
                    // We play the voice files programmatically using MediaPlayer.
                    val notification = NotificationCompat.Builder(context, "salat_alerts")
                        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                        .setContentTitle("Salat Time: $prayerName")
                        .setContentText("It is time for the $prayerName prayer.")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setContentIntent(pendingContentIntent)
                        .setAutoCancel(true)
                        .build()

                    notificationManager.notify(prayerName.hashCode(), notification)

                    // Play the selected Adhan voice programmatically
                    val voiceId = settingDao.getSettingValue("selected_adhan_voice") ?: "mecca"
                    val rawResName = when (voiceId) {
                        "mecca" -> "adhan_mecca"
                        "medina" -> "adhan_medina"
                        "al_aqsa" -> "adhan_al_aqsa"
                        "morocco" -> "adhan_morocco"
                        "abdul_basit" -> "adhan_abdul_basit"
                        else -> "adhan_mecca"
                    }
                    val rawId = context.resources.getIdentifier(rawResName, "raw", context.packageName)
                    if (rawId != 0) {
                        try {
                            val mediaPlayer = MediaPlayer.create(context, rawId)
                            mediaPlayer?.apply {
                                setOnCompletionListener { release() }
                                start()

                                val sharedPrefs = context.getSharedPreferences("kiblasalat_settings", Context.MODE_PRIVATE)
                                val isShortAdhan = sharedPrefs.getBoolean("short_adhan_enabled", false)
                                if (isShortAdhan) {
                                    CoroutineScope(Dispatchers.Default).launch {
                                        kotlinx.coroutines.delay(10000)
                                        try {
                                            if (isPlaying) {
                                                stop()
                                            }
                                            release()
                                        } catch (e: Exception) {
                                            // Handle cases where player is already released or stopped
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                // Reschedule next prayer times in the background
                AdhanAlarmHelper.schedulePrayerAlarms(context)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
