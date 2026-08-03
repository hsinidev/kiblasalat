package com.example.kiblasalat.presentation.viewmodel

import android.content.Context
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiblasalat.domain.model.Ayah
import com.example.kiblasalat.domain.model.Surah
import com.example.kiblasalat.domain.repository.SettingsRepository
import com.example.kiblasalat.domain.usecase.GetQuranSurahListUseCase
import com.example.kiblasalat.domain.usecase.GetSurahWithAyahsUseCase
import com.example.kiblasalat.domain.usecase.ToggleBookmarkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

@HiltViewModel
class QuranViewModel @Inject constructor(
    private val getQuranSurahListUseCase: GetQuranSurahListUseCase,
    private val getSurahWithAyahsUseCase: GetSurahWithAyahsUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val settingsRepository: SettingsRepository,
    private val quranRepository: com.example.kiblasalat.domain.repository.QuranRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val surahs: StateFlow<List<Surah>> = _searchQuery
        .flatMapLatest { query ->
            getQuranSurahListUseCase(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedSurahId = MutableStateFlow<Int?>(null)
    val selectedSurahId: StateFlow<Int?> = _selectedSurahId.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val selectedSurah: StateFlow<Surah?> = _selectedSurahId
        .flatMapLatest { id ->
            if (id == null) kotlinx.coroutines.flow.flowOf(null)
            else quranRepository.getSurahById(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val ayahs: StateFlow<List<Ayah>> = _selectedSurahId
        .flatMapLatest { id ->
            if (id == null) kotlinx.coroutines.flow.flowOf(emptyList())
            else getSurahWithAyahsUseCase(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val bookmarks: StateFlow<List<Ayah>> = quranRepository.getBookmarkedAyahs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val arabicFontSize: StateFlow<Float> = settingsRepository.getArabicFontSize()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 24.0f
        )

    val translationFontSize: StateFlow<Float> = settingsRepository.getTranslationFontSize()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 16.0f
        )

    // Audio Player State
    private var mediaPlayer: MediaPlayer? = null
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0)
    val currentPosition = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0)
    val duration = _duration.asStateFlow()

    private val _activeAudioSurahId = MutableStateFlow<Int?>(null)
    val activeAudioSurahId = _activeAudioSurahId.asStateFlow()

    // Reciters list
    data class Reciter(val id: String, val name: String, val urlPattern: String)
    val reciters = listOf(
        Reciter("saad", "Saad Al-Ghamdi (Said Lahandy)", "https://server7.mp3quran.net/s_gmd/%03d.mp3"),
        Reciter("shuraim", "Saud Al-Shuraim", "https://server7.mp3quran.net/shur/%03d.mp3"),
        Reciter("maher", "Maher Al-Muaiqly", "https://server12.mp3quran.net/maher/%03d.mp3"),
        Reciter("alijaber", "Ali Jaber", "https://server11.mp3quran.net/a_jbr/%03d.mp3")
    )

    private val _selectedReciterId = MutableStateFlow("saad")
    val selectedReciterId = _selectedReciterId.asStateFlow()

    // Audio download state
    private val _audioDownloadProgress = MutableStateFlow<Float?>(null) // null means not downloading
    val audioDownloadProgress = _audioDownloadProgress.asStateFlow()

    private val _audioDownloadError = MutableStateFlow<String?>(null)
    val audioDownloadError = _audioDownloadError.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectSurah(surahId: Int?) {
        _selectedSurahId.value = surahId
    }

    fun toggleBookmark(surahId: Int, ayahNumber: Int) {
        viewModelScope.launch {
            toggleBookmarkUseCase(surahId, ayahNumber)
        }
    }

    fun selectReciter(reciterId: String) {
        _selectedReciterId.value = reciterId
        val surahId = _activeAudioSurahId.value
        if (surahId != null && _isPlaying.value) {
            stopAudio()
            playAudio(surahId)
        }
    }

    fun isAudioDownloaded(surahId: Int): Boolean {
        val reciterId = _selectedReciterId.value
        val file = getAudioFile(reciterId, surahId)
        return file.exists() && file.length() > 0
    }

    private fun getAudioFile(reciterId: String, surahId: Int): File {
        val audioDir = File(context.filesDir, "quran_audio")
        if (!audioDir.exists()) {
            audioDir.mkdirs()
        }
        return File(audioDir, "reciter_${reciterId}_surah_${surahId}.mp3")
    }

    fun togglePlayPause(surahId: Int) {
        if (_activeAudioSurahId.value == surahId) {
            if (_isPlaying.value) {
                pauseAudio()
            } else {
                resumeAudio()
            }
        } else {
            playAudio(surahId)
        }
    }

    private fun playAudio(surahId: Int) {
        stopAudio()
        _activeAudioSurahId.value = surahId
        val reciterId = _selectedReciterId.value
        val localFile = getAudioFile(reciterId, surahId)

        if (localFile.exists() && localFile.length() > 0) {
            startMediaPlayer(localFile.absolutePath)
        } else {
            val reciter = reciters.find { it.id == reciterId } ?: reciters[0]
            val url = String.format(reciter.urlPattern, surahId)
            downloadAudioAndPlay(reciterId, surahId, url)
        }
    }

    private fun startMediaPlayer(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val mp = MediaPlayer().apply {
                    setDataSource(path)
                    prepare()
                    setOnCompletionListener {
                        _isPlaying.value = false
                        _currentPosition.value = 0
                        _activeAudioSurahId.value = null
                        release()
                        mediaPlayer = null
                    }
                }
                mediaPlayer = mp
                mp.start()
                _isPlaying.value = true
                _duration.value = mp.duration

                // Launch a job to track current position
                launch(Dispatchers.Main) {
                    while (mediaPlayer != null && _isPlaying.value) {
                        _currentPosition.value = mediaPlayer?.currentPosition ?: 0
                        delay(1000)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // If it fails to play local file, it might be corrupted, so delete it
                val reciterId = _selectedReciterId.value
                val surahId = _activeAudioSurahId.value
                if (surahId != null) {
                    getAudioFile(reciterId, surahId).delete()
                }
            }
        }
    }

    private fun downloadAudioAndPlay(reciterId: String, surahId: Int, urlString: String) {
        _audioDownloadProgress.value = 0f
        _audioDownloadError.value = null

        viewModelScope.launch(Dispatchers.IO) {
            var success = false
            var tempFile = File(context.cacheDir, "temp_reciter_${reciterId}_surah_${surahId}.mp3")
            
            // Try HTTPS first, if it fails, fallback to HTTP to bypass SSL issues
            val urlsToTry = listOf(urlString, urlString.replace("https://", "http://"))
            for (urlStr in urlsToTry) {
                try {
                    val url = URL(urlStr)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 10000
                    connection.readTimeout = 15000
                    connection.connect()

                    if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                        throw IOException("HTTP error code: ${connection.responseCode}")
                    }

                    val fileLength = connection.contentLength
                    tempFile = File(context.cacheDir, "temp_reciter_${reciterId}_surah_${surahId}.mp3")
                    
                    connection.inputStream.use { input ->
                        tempFile.outputStream().use { output ->
                            val data = ByteArray(4096)
                            var total: Long = 0
                            var count: Int
                            while (input.read(data).also { count = it } != -1) {
                                total += count
                                if (fileLength > 0) {
                                    _audioDownloadProgress.value = total.toFloat() / fileLength
                                } else {
                                    // Estimated progress if content-length is missing
                                    _audioDownloadProgress.value = (total.toFloat() / (5 * 1024 * 1024)).coerceAtMost(0.99f)
                                }
                                output.write(data, 0, count)
                            }
                        }
                    }
                    success = true
                    break
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val destFile = getAudioFile(reciterId, surahId)
            if (success && tempFile.exists() && tempFile.length() > 0) {
                tempFile.renameTo(destFile)
                _audioDownloadProgress.value = null
                viewModelScope.launch(Dispatchers.Main) {
                    // Play the downloaded local file if user hasn't changed Surah
                    if (_activeAudioSurahId.value == surahId && _selectedReciterId.value == reciterId) {
                        startMediaPlayer(destFile.absolutePath)
                    }
                }
            } else {
                _audioDownloadProgress.value = null
                _audioDownloadError.value = "Failed to download audio for offline use."
            }
        }
    }

    private fun pauseAudio() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _isPlaying.value = false
            }
        }
    }

    private fun resumeAudio() {
        mediaPlayer?.let {
            it.start()
            _isPlaying.value = true
            viewModelScope.launch(Dispatchers.Main) {
                while (mediaPlayer != null && _isPlaying.value) {
                    _currentPosition.value = mediaPlayer?.currentPosition ?: 0
                    delay(1000)
                }
            }
        }
    }

    fun stopAudio() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
            _isPlaying.value = false
            _currentPosition.value = 0
            _duration.value = 0
            _activeAudioSurahId.value = null
        }
    }

    fun seekTo(position: Int) {
        mediaPlayer?.let {
            it.seekTo(position)
            _currentPosition.value = position
        }
    }

    fun increaseArabicFont() {
        viewModelScope.launch {
            val current = arabicFontSize.value
            if (current < 40f) {
                settingsRepository.setArabicFontSize(current + 2f)
            }
        }
    }

    fun decreaseArabicFont() {
        viewModelScope.launch {
            val current = arabicFontSize.value
            if (current > 18f) {
                settingsRepository.setArabicFontSize(current - 2f)
            }
        }
    }

    fun increaseTranslationFont() {
        viewModelScope.launch {
            val current = translationFontSize.value
            if (current < 26f) {
                settingsRepository.setTranslationFontSize(current + 1f)
            }
        }
    }

    fun decreaseTranslationFont() {
        viewModelScope.launch {
            val current = translationFontSize.value
            if (current > 12f) {
                settingsRepository.setTranslationFontSize(current - 1f)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAudio()
    }
}
