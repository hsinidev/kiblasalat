package com.example.kiblasalat.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.kiblasalat.data.database.SettingDao
import com.example.kiblasalat.domain.usecase.GetPrayerTimesUseCase
import com.example.kiblasalat.receiver.AdhanReceiver
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.Calendar
import java.util.Date

object AdhanAlarmHelper {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AlarmEntryPoint {
        fun getSettingDao(): SettingDao
        fun getGetPrayerTimesUseCase(): GetPrayerTimesUseCase
    }

    suspend fun schedulePrayerAlarms(context: Context) {
        val appContext = context.applicationContext
        val entryPoint = EntryPoints.get(appContext, AlarmEntryPoint::class.java)
        val settingDao = entryPoint.getSettingDao()
        val getPrayerTimesUseCase = entryPoint.getGetPrayerTimesUseCase()

        val lat = settingDao.getSettingValue("latitude")?.toDoubleOrNull() ?: return
        val lng = settingDao.getSettingValue("longitude")?.toDoubleOrNull() ?: return
        val method = settingDao.getSettingValue("calculation_method") ?: "MUSLIM_WORLD_LEAGUE"
        val madhab = settingDao.getSettingValue("asr_madhab") ?: "SHAFI"

        val prayers = listOf("Fajr", "Sunrise", "Dhuhr", "Asr", "Maghrib", "Isha")
        val offsets = prayers.associateWith { name ->
            settingDao.getSettingValue("prayer_offset_${name.lowercase()}")?.toIntOrNull() ?: 0
        }

        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val now = Date()
        val calendar = Calendar.getInstance()
        
        val getTimesForDate = { date: Date ->
            getPrayerTimesUseCase(
                latitude = lat,
                longitude = lng,
                calculationMethod = method,
                asrMadhab = madhab,
                date = date,
                offsets = offsets
            )
        }

        val todayTimes = getTimesForDate(now)
        
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrowTimes = getTimesForDate(calendar.time)

        val allUpcomingPrayers = (todayTimes.prayers + tomorrowTimes.prayers)
            .filter { it.time.after(now) }
            .sortedBy { it.time }

        // Cancel previous alarms to prevent duplicates
        prayers.forEach { name ->
            val intent = Intent(appContext, AdhanReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                appContext,
                name.hashCode(),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
            }
        }

        // Schedule the next 5 upcoming prayers
        val alarmCountToSchedule = minOf(allUpcomingPrayers.size, 5)
        for (i in 0 until alarmCountToSchedule) {
            val prayer = allUpcomingPrayers[i]
            val intent = Intent(appContext, AdhanReceiver::class.java).apply {
                putExtra("prayer_name", prayer.name)
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                appContext,
                prayer.name.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val triggerTime = prayer.time.time

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        }
    }
}
