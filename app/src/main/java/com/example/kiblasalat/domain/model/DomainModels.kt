package com.example.kiblasalat.domain.model

import java.util.Date

data class Surah(
    val id: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val revelationType: String,
    val totalAyahs: Int,
    val isBookmarkedAny: Boolean = false
)

data class Ayah(
    val id: Int,
    val surahId: Int,
    val numberInSurah: Int,
    val textArabic: String,
    val textEnglish: String,
    val juz: Int,
    val isBookmarked: Boolean = false
)

data class PrayerTimeItem(
    val name: String,
    val time: Date,
    val isNext: Boolean = false
)

data class SalatTimes(
    val locationName: String,
    val date: Date,
    val prayers: List<PrayerTimeItem>,
    val nextPrayerName: String,
    val nextPrayerTime: Date,
    val countdownMillis: Long
)

data class QiblaDirection(
    val qiblaBearing: Float,      // Angle of Kaaba relative to North (0-360 degrees)
    val userAzimuth: Float,       // Device's current rotation relative to North (0-360 degrees)
    val relativeAngle: Float,     // Angle of Kaaba relative to device orientation
    val isAligned: Boolean        // True if phone points towards Kaaba within threshold
)
