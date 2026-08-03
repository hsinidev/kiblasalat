package com.example.kiblasalat

import com.example.kiblasalat.domain.usecase.GetPrayerTimesUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.Calendar

class PrayerTimesTest {

    private val getPrayerTimesUseCase = GetPrayerTimesUseCase()

    @Test
    fun testPrayerTimesCalculation() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.JUNE)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
        }

        val result = getPrayerTimesUseCase(
            latitude = 21.4225,
            longitude = 39.8262,
            calculationMethod = "UMM_AL_QURA",
            asrMadhab = "SHAFI",
            date = calendar.time,
            locationName = "Makkah"
        )

        assertNotNull(result)
        assertEquals("Makkah", result.locationName)
        assertEquals(6, result.prayers.size)
        
        assertEquals("Fajr", result.prayers[0].name)
        assertEquals("Sunrise", result.prayers[1].name)
        assertEquals("Dhuhr", result.prayers[2].name)
        assertEquals("Asr", result.prayers[3].name)
        assertEquals("Maghrib", result.prayers[4].name)
        assertEquals("Isha", result.prayers[5].name)
    }
}
