package com.example.kiblasalat.presentation.viewmodel

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiblasalat.R
import com.example.kiblasalat.data.network.FileDownloader
import com.example.kiblasalat.domain.repository.SettingsRepository
import com.example.kiblasalat.service.AdhanAlarmHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class OnboardingCity(val name: String, val latitude: Double, val longitude: Double, val stringResId: Int)
data class OnboardingCountry(val id: String, val name: String, val stringResId: Int, val defaultMethod: String, val cities: List<OnboardingCity>)
data class AdhanVoice(val id: String, val stringResId: Int, val url: String)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val countries = listOf(
        OnboardingCountry("morocco", "Morocco", R.string.morocco, "MOROCCO", listOf(
            OnboardingCity("Casablanca", 33.5731, -7.5898, R.string.casablanca),
            OnboardingCity("Rabat", 34.0209, -6.8416, R.string.rabat),
            OnboardingCity("Marrakech", 31.6295, -7.9811, R.string.marrakech),
            OnboardingCity("Fes", 34.0181, -5.0078, R.string.fes),
            OnboardingCity("Tangier", 35.7595, -5.8340, R.string.tangier),
            OnboardingCity("Agadir", 30.4278, -9.5981, R.string.agadir),
            OnboardingCity("Oujda", 34.6853, -1.9078, R.string.oujda)
        )),
        OnboardingCountry("saudi_arabia", "Saudi Arabia", R.string.saudi_arabia, "UMM_AL_QURA", listOf(
            OnboardingCity("Makkah", 21.4225, 39.8262, R.string.makkah),
            OnboardingCity("Medina", 24.4672, 39.6111, R.string.medina)
        )),
        OnboardingCountry("egypt", "Egypt", R.string.egypt, "EGYPT", listOf(
            OnboardingCity("Cairo", 30.0444, 31.2357, R.string.cairo)
        )),
        OnboardingCountry("turkey", "Turkey", R.string.turkey, "TURKEY", listOf(
            OnboardingCity("Istanbul", 41.0082, 28.9784, R.string.istanbul)
        )),
        OnboardingCountry("indonesia", "Indonesia", R.string.indonesia, "SINGAPORE", listOf(
            OnboardingCity("Jakarta", -6.2088, 106.8456, R.string.jakarta)
        )),
        OnboardingCountry("pakistan", "Pakistan", R.string.pakistan, "KARACHI", listOf(
            OnboardingCity("Karachi", 24.8607, 67.0011, R.string.karachi)
        )),
        OnboardingCountry("united_states", "United States", R.string.united_states, "ISNA", listOf(
            OnboardingCity("New York", 40.7128, -74.0060, R.string.new_york),
            OnboardingCity("London", 51.5074, -0.1278, R.string.london)
        ))
    )

    val adhanVoices = listOf(
        AdhanVoice("mecca", R.string.voice_mecca, "https://www.islamcan.com/audio/adhan/azan1.mp3"),
        AdhanVoice("medina", R.string.voice_medina, "https://www.islamcan.com/audio/adhan/azan2.mp3"),
        AdhanVoice("al_aqsa", R.string.voice_al_aqsa, "https://www.islamcan.com/audio/adhan/azan3.mp3"),
        AdhanVoice("morocco", R.string.voice_morocco, "https://www.islamcan.com/audio/adhan/azan4.mp3"),
        AdhanVoice("abdul_basit", R.string.voice_abdul_basit, "https://www.islamcan.com/audio/adhan/azan5.mp3")
    )

    // Current State
    private val _currentStep = MutableStateFlow(1)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("en")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _selectedCountry = MutableStateFlow(countries[0])
    val selectedCountry: StateFlow<OnboardingCountry> = _selectedCountry.asStateFlow()

    private val _selectedCity = MutableStateFlow(countries[0].cities[0])
    val selectedCity: StateFlow<OnboardingCity> = _selectedCity.asStateFlow()

    // Offsets in minutes
    private val _offsetFajr = MutableStateFlow(0)
    val offsetFajr: StateFlow<Int> = _offsetFajr.asStateFlow()

    private val _offsetSunrise = MutableStateFlow(0)
    val offsetSunrise: StateFlow<Int> = _offsetSunrise.asStateFlow()

    private val _offsetDhuhr = MutableStateFlow(0)
    val offsetDhuhr: StateFlow<Int> = _offsetDhuhr.asStateFlow()

    private val _offsetAsr = MutableStateFlow(0)
    val offsetAsr: StateFlow<Int> = _offsetAsr.asStateFlow()

    private val _offsetMaghrib = MutableStateFlow(0)
    val offsetMaghrib: StateFlow<Int> = _offsetMaghrib.asStateFlow()

    private val _offsetIsha = MutableStateFlow(0)
    val offsetIsha: StateFlow<Int> = _offsetIsha.asStateFlow()

    // Adhan Enabled state per prayer
    private val _adhanEnabledFajr = MutableStateFlow(true)
    val adhanEnabledFajr: StateFlow<Boolean> = _adhanEnabledFajr.asStateFlow()

    private val _adhanEnabledDhuhr = MutableStateFlow(true)
    val adhanEnabledDhuhr: StateFlow<Boolean> = _adhanEnabledDhuhr.asStateFlow()

    private val _adhanEnabledAsr = MutableStateFlow(true)
    val adhanEnabledAsr: StateFlow<Boolean> = _adhanEnabledAsr.asStateFlow()

    private val _adhanEnabledMaghrib = MutableStateFlow(true)
    val adhanEnabledMaghrib: StateFlow<Boolean> = _adhanEnabledMaghrib.asStateFlow()

    private val _adhanEnabledIsha = MutableStateFlow(true)
    val adhanEnabledIsha: StateFlow<Boolean> = _adhanEnabledIsha.asStateFlow()

    private val _selectedAdhanVoice = MutableStateFlow("mecca")
    val selectedAdhanVoice: StateFlow<String> = _selectedAdhanVoice.asStateFlow()

    private val _shortAdhanEnabled = MutableStateFlow(false)
    val shortAdhanEnabled: StateFlow<Boolean> = _shortAdhanEnabled.asStateFlow()

    fun setShortAdhanEnabled(enabled: Boolean) {
        _shortAdhanEnabled.value = enabled
    }

    // Download/Preview state
    private val _downloadProgress = MutableStateFlow<Float?>(null)
    val downloadProgress: StateFlow<Float?> = _downloadProgress.asStateFlow()

    private val _downloadError = MutableStateFlow<String?>(null)
    val downloadError: StateFlow<String?> = _downloadError.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var autoStopJob: kotlinx.coroutines.Job? = null

    fun nextStep() {
        if (_currentStep.value < 4) {
            _currentStep.value += 1
        }
    }

    fun prevStep() {
        if (_currentStep.value > 1) {
            _currentStep.value -= 1
        }
    }

    fun selectLanguage(lang: String) {
        _selectedLanguage.value = lang
        // Instantly switch UI language globally
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang))
    }

    fun selectCountry(country: OnboardingCountry) {
        _selectedCountry.value = country
        // Auto select first city in that country
        if (country.cities.isNotEmpty()) {
            _selectedCity.value = country.cities[0]
        }
        
        // Custom requirements for Morocco
        if (country.id == "morocco") {
            // "If Morocco is selected, automatically lock calculation method to MOROCCO"
            // For Morocco, Habous offsets are: -3 Sunrise, +5 Dhuhr, +5 Maghrib. Let's preset them!
            _offsetSunrise.value = -3
            _offsetDhuhr.value = 5
            _offsetMaghrib.value = 5
            _offsetFajr.value = 0
            _offsetAsr.value = 0
            _offsetIsha.value = 0
            
            // Auto switch to Morocco adhan style voice
            _selectedAdhanVoice.value = "morocco"
        } else {
            // Reset offsets
            _offsetSunrise.value = 0
            _offsetDhuhr.value = 0
            _offsetMaghrib.value = 0
            _offsetFajr.value = 0
            _offsetAsr.value = 0
            _offsetIsha.value = 0
        }
    }

    fun selectCity(city: OnboardingCity) {
        _selectedCity.value = city
    }

    fun setOffset(prayer: String, value: Int) {
        when (prayer) {
            "Fajr" -> _offsetFajr.value = value
            "Sunrise" -> _offsetSunrise.value = value
            "Dhuhr" -> _offsetDhuhr.value = value
            "Asr" -> _offsetAsr.value = value
            "Maghrib" -> _offsetMaghrib.value = value
            "Isha" -> _offsetIsha.value = value
        }
    }

    fun toggleAdhan(prayer: String) {
        when (prayer) {
            "Fajr" -> _adhanEnabledFajr.value = !_adhanEnabledFajr.value
            "Dhuhr" -> _adhanEnabledDhuhr.value = !_adhanEnabledDhuhr.value
            "Asr" -> _adhanEnabledAsr.value = !_adhanEnabledAsr.value
            "Maghrib" -> _adhanEnabledMaghrib.value = !_adhanEnabledMaghrib.value
            "Isha" -> _adhanEnabledIsha.value = !_adhanEnabledIsha.value
        }
    }

    fun selectAdhanVoice(voiceId: String) {
        _selectedAdhanVoice.value = voiceId
        stopPreview()
    }

    fun getVoiceFile(voiceId: String): File {
        val dir = File(context.filesDir, "adhan")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "adhan_voice_$voiceId.mp3")
    }

    fun isVoiceDownloaded(voiceId: String): Boolean {
        return true
    }

    fun playPreview(voiceId: String) {
        stopPreview()
        
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
                mediaPlayer = MediaPlayer.create(context, rawId).apply {
                    start()
                }
                if (_shortAdhanEnabled.value) {
                    autoStopJob = viewModelScope.launch {
                        kotlinx.coroutines.delay(10000)
                        stopPreview()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _downloadError.value = "Failed to play audio"
            }
        } else {
            _downloadError.value = "Audio file not found"
        }
    }

    fun stopPreview() {
        autoStopJob?.cancel()
        autoStopJob = null
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
    }

    fun saveAndFinish() {
        viewModelScope.launch {
            stopPreview()
            
            // Save settings
            settingsRepository.setSelectedLanguage(_selectedLanguage.value)
            settingsRepository.setSelectedCountry(_selectedCountry.value.name)
            settingsRepository.setSelectedCity(_selectedCity.value.name)
            settingsRepository.saveCoordinates(_selectedCity.value.latitude, _selectedCity.value.longitude)
            settingsRepository.setManualLocationName(_selectedCity.value.name)
            settingsRepository.setCalculationMethod(_selectedCountry.value.defaultMethod)
            settingsRepository.setSelectedAdhanVoice(_selectedAdhanVoice.value)
            settingsRepository.setShortAdhanEnabled(_shortAdhanEnabled.value)

            // Save offsets
            settingsRepository.setPrayerOffset("Fajr", _offsetFajr.value)
            settingsRepository.setPrayerOffset("Sunrise", _offsetSunrise.value)
            settingsRepository.setPrayerOffset("Dhuhr", _offsetDhuhr.value)
            settingsRepository.setPrayerOffset("Asr", _offsetAsr.value)
            settingsRepository.setPrayerOffset("Maghrib", _offsetMaghrib.value)
            settingsRepository.setPrayerOffset("Isha", _offsetIsha.value)

            // Save adhan toggles
            settingsRepository.setAdhanEnabledForPrayer("Fajr", _adhanEnabledFajr.value)
            settingsRepository.setAdhanEnabledForPrayer("Dhuhr", _adhanEnabledDhuhr.value)
            settingsRepository.setAdhanEnabledForPrayer("Asr", _adhanEnabledAsr.value)
            settingsRepository.setAdhanEnabledForPrayer("Maghrib", _adhanEnabledMaghrib.value)
            settingsRepository.setAdhanEnabledForPrayer("Isha", _adhanEnabledIsha.value)

            // Schedule alarms immediately using local raw resources
            AdhanAlarmHelper.schedulePrayerAlarms(context)

            // Complete onboarding
            settingsRepository.setOnboardingCompleted(true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPreview()
    }
}
