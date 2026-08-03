package com.example.kiblasalat.domain.repository

import com.example.kiblasalat.domain.model.Ayah
import com.example.kiblasalat.domain.model.Surah
import kotlinx.coroutines.flow.Flow

interface QuranRepository {
    fun getAllSurahs(): Flow<List<Surah>>
    fun getSurahById(id: Int): Flow<Surah?>
    fun searchSurahs(query: String): Flow<List<Surah>>
    fun getAyahsForSurah(surahId: Int): Flow<List<Ayah>>
    fun searchAyahs(query: String): Flow<List<Ayah>>
    fun getBookmarkedAyahs(): Flow<List<Ayah>>
    suspend fun toggleBookmark(surahId: Int, ayahNumber: Int)
    fun isBookmarked(surahId: Int, ayahNumber: Int): Flow<Boolean>
}

interface SettingsRepository {
    fun getCalculationMethod(): Flow<String>
    suspend fun setCalculationMethod(method: String)
    
    fun getAsrMadhab(): Flow<String>
    suspend fun setAsrMadhab(madhab: String)
    
    fun getLatitude(): Flow<Double?>
    fun getLongitude(): Flow<Double?>
    suspend fun saveCoordinates(latitude: Double, longitude: Double)
    
    fun getManualLocationName(): Flow<String?>
    suspend fun setManualLocationName(name: String?)
    
    fun getArabicFontSize(): Flow<Float>
    suspend fun setArabicFontSize(size: Float)
    
    fun getTranslationFontSize(): Flow<Float>
    suspend fun setTranslationFontSize(size: Float)

    fun isOnboardingCompleted(): Flow<Boolean>
    suspend fun setOnboardingCompleted(completed: Boolean)

    fun getSelectedCountry(): Flow<String?>
    suspend fun setSelectedCountry(country: String)

    fun getSelectedCity(): Flow<String?>
    suspend fun setSelectedCity(city: String)

    fun getSelectedLanguage(): Flow<String>
    suspend fun setSelectedLanguage(language: String)

    fun getSelectedAdhanVoice(): Flow<String>
    suspend fun setSelectedAdhanVoice(voice: String)

    fun getPrayerOffset(prayerName: String): Flow<Int>
    suspend fun setPrayerOffset(prayerName: String, offset: Int)

    fun isAdhanEnabledForPrayer(prayerName: String): Flow<Boolean>
    suspend fun setAdhanEnabledForPrayer(prayerName: String, enabled: Boolean)

    fun getBookmarkedPages(): Flow<Set<Int>>
    suspend fun togglePageBookmark(page: Int)

    fun isShortAdhanEnabled(): Flow<Boolean>
    suspend fun setShortAdhanEnabled(enabled: Boolean)
}

