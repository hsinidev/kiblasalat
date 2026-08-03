package com.example.kiblasalat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiblasalat.data.location.LocationProvider
import com.example.kiblasalat.domain.model.SalatTimes
import com.example.kiblasalat.domain.repository.SettingsRepository
import com.example.kiblasalat.domain.usecase.GetPrayerTimesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PrayerParameters(
    val latitude: Double?,
    val longitude: Double?,
    val method: String,
    val madhab: String,
    val manualLocationName: String?
)

data class FallbackCity(
    val name: String,
    val latitude: Double,
    val longitude: Double
)

sealed interface SalatTimesState {
    object Loading : SalatTimesState
    data class Success(val salatTimes: SalatTimes) : SalatTimesState
    data class Error(val message: String) : SalatTimesState
}

@HiltViewModel
class PrayerViewModel @Inject constructor(
    private val locationProvider: LocationProvider,
    private val settingsRepository: SettingsRepository,
    private val getPrayerTimesUseCase: GetPrayerTimesUseCase
) : ViewModel() {

    val fallbackCities = listOf(
        FallbackCity("Makkah", 21.4225, 39.8262),
        FallbackCity("Medina", 24.4672, 39.6111),
        FallbackCity("Cairo", 30.0444, 31.2357),
        FallbackCity("Karachi", 24.8607, 67.0011),
        FallbackCity("Dubai", 25.2048, 55.2708),
        FallbackCity("London", 51.5074, -0.1278),
        FallbackCity("New York", 40.7128, -74.0060),
        FallbackCity("Jakarta", -6.2088, 106.8456),
        FallbackCity("Kuala Lumpur", 3.1390, 101.6869),
        FallbackCity("Istanbul", 41.0082, 28.9784)
    )

    private val _gpsError = MutableStateFlow<String?>(null)
    val gpsError: StateFlow<String?> = _gpsError.asStateFlow()

    private val _isGpsLoading = MutableStateFlow(false)
    val isGpsLoading: StateFlow<Boolean> = _isGpsLoading.asStateFlow()

    private val offsetsFlow = combine(
        settingsRepository.getPrayerOffset("Fajr"),
        settingsRepository.getPrayerOffset("Sunrise"),
        settingsRepository.getPrayerOffset("Dhuhr"),
        settingsRepository.getPrayerOffset("Asr"),
        settingsRepository.getPrayerOffset("Maghrib"),
        settingsRepository.getPrayerOffset("Isha")
    ) { array: Array<Int> ->
        mapOf(
            "Fajr" to array[0],
            "Sunrise" to array[1],
            "Dhuhr" to array[2],
            "Asr" to array[3],
            "Maghrib" to array[4],
            "Isha" to array[5]
        )
    }

    private val prayerParametersFlow = combine(
        settingsRepository.getLatitude(),
        settingsRepository.getLongitude(),
        settingsRepository.getCalculationMethod(),
        settingsRepository.getAsrMadhab(),
        settingsRepository.getManualLocationName()
    ) { lat, lng, method, madhab, manualName ->
        PrayerParameters(lat, lng, method, madhab, manualName)
    }

    private val tickerFlow = flow {
        while (true) {
            emit(Unit)
            delay(1000)
        }
    }

    val salatTimesStateFlow: StateFlow<SalatTimesState> = combine(
        prayerParametersFlow,
        offsetsFlow,
        tickerFlow
    ) { params, offsets, _ ->
        val lat = params.latitude ?: 21.4225 // Fallback Makkah
        val lng = params.longitude ?: 39.8262
        val name = params.manualLocationName ?: if (params.latitude == null) "Makkah (Default)" else "GPS Location"

        try {
            val times = getPrayerTimesUseCase(
                latitude = lat,
                longitude = lng,
                calculationMethod = params.method,
                asrMadhab = params.madhab,
                locationName = name,
                offsets = offsets
            )
            SalatTimesState.Success(times)
        } catch (e: Exception) {
            SalatTimesState.Error(e.localizedMessage ?: "Failed to calculate prayer times")
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SalatTimesState.Loading
    )

    fun selectFallbackCity(city: FallbackCity) {
        viewModelScope.launch {
            _gpsError.value = null
            settingsRepository.saveCoordinates(city.latitude, city.longitude)
            settingsRepository.setManualLocationName(city.name)
        }
    }

    fun requestGpsLocation() {
        viewModelScope.launch {
            _isGpsLoading.value = true
            _gpsError.value = null
            val location = locationProvider.getCurrentLocation()
            if (location != null) {
                settingsRepository.saveCoordinates(location.latitude, location.longitude)
                settingsRepository.setManualLocationName(null) // Resets to GPS
            } else {
                _gpsError.value = "Unable to fetch GPS. Using last saved location or select a city."
            }
            _isGpsLoading.value = false
        }
    }
}
