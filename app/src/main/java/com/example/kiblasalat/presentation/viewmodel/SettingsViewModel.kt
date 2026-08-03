package com.example.kiblasalat.presentation.viewmodel

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiblasalat.domain.repository.SettingsRepository
import com.example.kiblasalat.receiver.AdhanReceiver
import com.example.kiblasalat.service.AdhanAlarmHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val calculationMethod: StateFlow<String> = settingsRepository.getCalculationMethod()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "MUSLIM_WORLD_LEAGUE"
        )

    val asrMadhab: StateFlow<String> = settingsRepository.getAsrMadhab()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "SHAFI"
        )

    val selectedAdhanVoice: StateFlow<String> = settingsRepository.getSelectedAdhanVoice()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "mecca"
        )

    val isShortAdhanEnabled: StateFlow<Boolean> = settingsRepository.isShortAdhanEnabled()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun setShortAdhanEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setShortAdhanEnabled(enabled)
            AdhanAlarmHelper.schedulePrayerAlarms(context)
        }
    }

    // Storage and Cache States
    private val _cacheSize = MutableStateFlow(0L)
    val cacheSize: StateFlow<Long> = _cacheSize.asStateFlow()

    // Download/Preview States
    private val _downloadProgress = MutableStateFlow<Float?>(null)
    val downloadProgress = _downloadProgress.asStateFlow()

    private val _downloadError = MutableStateFlow<String?>(null)
    val downloadError = _downloadError.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var autoStopJob: kotlinx.coroutines.Job? = null

    data class AdhanVoiceOption(val id: String, val name: String, val url: String)
    val adhanVoices = listOf(
        AdhanVoiceOption("mecca", "Makkah (Al-Haram)", "https://www.islamcan.com/audio/adhan/azan1.mp3"),
        AdhanVoiceOption("medina", "Medina (An-Nabawi)", "https://www.islamcan.com/audio/adhan/azan2.mp3"),
        AdhanVoiceOption("al_aqsa", "Al-Aqsa", "https://www.islamcan.com/audio/adhan/azan3.mp3"),
        AdhanVoiceOption("morocco", "Morocco style", "https://www.islamcan.com/audio/adhan/azan4.mp3"),
        AdhanVoiceOption("abdul_basit", "Abdul Basit", "https://www.islamcan.com/audio/adhan/azan5.mp3")
    )

    init {
        updateCacheSize()
    }

    fun setCalculationMethod(method: String) {
        viewModelScope.launch {
            settingsRepository.setCalculationMethod(method)
            AdhanAlarmHelper.schedulePrayerAlarms(context)
        }
    }

    fun setAsrMadhab(madhab: String) {
        viewModelScope.launch {
            settingsRepository.setAsrMadhab(madhab)
            AdhanAlarmHelper.schedulePrayerAlarms(context)
        }
    }

    fun selectAdhanVoice(voiceId: String) {
        viewModelScope.launch {
            settingsRepository.setSelectedAdhanVoice(voiceId)
            AdhanAlarmHelper.schedulePrayerAlarms(context)
        }
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
                if (isShortAdhanEnabled.value) {
                    autoStopJob = viewModelScope.launch {
                        kotlinx.coroutines.delay(10000)
                        stopPreview()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _downloadError.value = "Failed to play preview"
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

    fun updateCacheSize() {
        viewModelScope.launch(Dispatchers.IO) {
            val quranSize = getFolderSize(File(context.filesDir, "quran_audio"))
            val adhanSize = getFolderSize(File(context.filesDir, "adhan"))
            _cacheSize.value = quranSize + adhanSize
        }
    }

    fun clearCache() {
        viewModelScope.launch(Dispatchers.IO) {
            val quranDir = File(context.filesDir, "quran_audio")
            if (quranDir.exists()) {
                quranDir.listFiles()?.forEach { it.delete() }
            }
            val adhanDir = File(context.filesDir, "adhan")
            if (adhanDir.exists()) {
                // Delete downloaded voice files to clear storage
                adhanDir.listFiles()?.forEach { it.delete() }
            }
            updateCacheSize()
        }
    }

    private fun getFolderSize(dir: File): Long {
        if (!dir.exists()) return 0L
        var size = 0L
        dir.listFiles()?.forEach { file ->
            if (file.isFile) {
                size += file.length()
            } else if (file.isDirectory) {
                size += getFolderSize(file)
            }
        }
        return size
    }

    fun triggerTestNotification() {
        val intent = Intent(context, AdhanReceiver::class.java).apply {
            putExtra("prayer_name", "Maghrib (Test Alert)")
        }
        context.sendBroadcast(intent)
    }

    override fun onCleared() {
        super.onCleared()
        stopPreview()
    }
}
