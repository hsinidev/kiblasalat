package com.example.kiblasalat.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.kiblasalat.data.database.SettingDao
import com.example.kiblasalat.data.database.SettingEntity
import com.example.kiblasalat.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingDao: SettingDao,
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private val sharedPrefs: SharedPreferences by lazy {
        context.getSharedPreferences("kiblasalat_settings", Context.MODE_PRIVATE)
    }

    override fun getCalculationMethod(): Flow<String> {
        return settingDao.getSetting("calculation_method").map { it?.value ?: "MUSLIM_WORLD_LEAGUE" }
    }

    override suspend fun setCalculationMethod(method: String) {
        settingDao.insertSetting(SettingEntity("calculation_method", method))
    }

    override fun getAsrMadhab(): Flow<String> {
        return settingDao.getSetting("asr_madhab").map { it?.value ?: "SHAFI" }
    }

    override suspend fun setAsrMadhab(madhab: String) {
        settingDao.insertSetting(SettingEntity("asr_madhab", madhab))
    }

    override fun getLatitude(): Flow<Double?> {
        return settingDao.getSetting("latitude").map { it?.value?.toDoubleOrNull() }
    }

    override fun getLongitude(): Flow<Double?> {
        return settingDao.getSetting("longitude").map { it?.value?.toDoubleOrNull() }
    }

    override suspend fun saveCoordinates(latitude: Double, longitude: Double) {
        settingDao.insertSetting(SettingEntity("latitude", latitude.toString()))
        settingDao.insertSetting(SettingEntity("longitude", longitude.toString()))
    }

    override fun getManualLocationName(): Flow<String?> {
        return settingDao.getSetting("manual_location_name").map { 
            if (it?.value.isNullOrEmpty()) null else it?.value 
        }
    }

    override suspend fun setManualLocationName(name: String?) {
        settingDao.insertSetting(SettingEntity("manual_location_name", name ?: ""))
    }

    override fun getArabicFontSize(): Flow<Float> {
        return settingDao.getSetting("arabic_font_size").map { it?.value?.toFloatOrNull() ?: 24.0f }
    }

    override suspend fun setArabicFontSize(size: Float) {
        settingDao.insertSetting(SettingEntity("arabic_font_size", size.toString()))
    }

    override fun getTranslationFontSize(): Flow<Float> {
        return settingDao.getSetting("translation_font_size").map { it?.value?.toFloatOrNull() ?: 16.0f }
    }

    override suspend fun setTranslationFontSize(size: Float) {
        settingDao.insertSetting(SettingEntity("translation_font_size", size.toString()))
    }

    override fun isOnboardingCompleted(): Flow<Boolean> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "onboarding_completed") {
                trySend(sharedPrefs.getBoolean("onboarding_completed", false))
            }
        }
        sharedPrefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(sharedPrefs.getBoolean("onboarding_completed", false))
        awaitClose {
            sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        sharedPrefs.edit().putBoolean("onboarding_completed", completed).apply()
    }

    override fun getSelectedCountry(): Flow<String?> {
        return settingDao.getSetting("selected_country").map { if (it?.value.isNullOrEmpty()) null else it?.value }
    }

    override suspend fun setSelectedCountry(country: String) {
        settingDao.insertSetting(SettingEntity("selected_country", country))
    }

    override fun getSelectedCity(): Flow<String?> {
        return settingDao.getSetting("selected_city").map { if (it?.value.isNullOrEmpty()) null else it?.value }
    }

    override suspend fun setSelectedCity(city: String) {
        settingDao.insertSetting(SettingEntity("selected_city", city))
    }

    override fun getSelectedLanguage(): Flow<String> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "selected_language") {
                trySend(sharedPrefs.getString("selected_language", "en") ?: "en")
            }
        }
        sharedPrefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(sharedPrefs.getString("selected_language", "en") ?: "en")
        awaitClose {
            sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    override suspend fun setSelectedLanguage(language: String) {
        sharedPrefs.edit().putString("selected_language", language).apply()
    }

    override fun getSelectedAdhanVoice(): Flow<String> {
        return settingDao.getSetting("selected_adhan_voice").map { it?.value ?: "mecca" }
    }

    override suspend fun setSelectedAdhanVoice(voice: String) {
        settingDao.insertSetting(SettingEntity("selected_adhan_voice", voice))
    }

    override fun getPrayerOffset(prayerName: String): Flow<Int> {
        return settingDao.getSetting("prayer_offset_${prayerName.lowercase()}").map { it?.value?.toIntOrNull() ?: 0 }
    }

    override suspend fun setPrayerOffset(prayerName: String, offset: Int) {
        settingDao.insertSetting(SettingEntity("prayer_offset_${prayerName.lowercase()}", offset.toString()))
    }

    override fun isAdhanEnabledForPrayer(prayerName: String): Flow<Boolean> {
        return settingDao.getSetting("adhan_enabled_${prayerName.lowercase()}").map { it?.value?.toBoolean() ?: true }
    }

    override suspend fun setAdhanEnabledForPrayer(prayerName: String, enabled: Boolean) {
        settingDao.insertSetting(SettingEntity("adhan_enabled_${prayerName.lowercase()}", enabled.toString()))
    }

    override fun getBookmarkedPages(): Flow<Set<Int>> {
        return settingDao.getSetting("bookmarked_pages").map { entity ->
            val value = entity?.value ?: ""
            if (value.isEmpty()) emptySet()
            else value.split(",").mapNotNull { it.toIntOrNull() }.toSet()
        }
    }

    override suspend fun togglePageBookmark(page: Int) {
        val currentString = settingDao.getSettingValue("bookmarked_pages") ?: ""
        val currentSet = if (currentString.isEmpty()) mutableSetOf()
        else currentString.split(",").mapNotNull { it.toIntOrNull() }.toMutableSet()
        
        if (currentSet.contains(page)) {
            currentSet.remove(page)
        } else {
            currentSet.add(page)
        }
        
        settingDao.insertSetting(SettingEntity("bookmarked_pages", currentSet.joinToString(",")))
    }

    override fun isShortAdhanEnabled(): Flow<Boolean> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "short_adhan_enabled") {
                trySend(sharedPrefs.getBoolean("short_adhan_enabled", false))
            }
        }
        sharedPrefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(sharedPrefs.getBoolean("short_adhan_enabled", false))
        awaitClose {
            sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    override suspend fun setShortAdhanEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("short_adhan_enabled", enabled).apply()
    }
}
