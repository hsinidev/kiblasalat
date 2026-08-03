package com.example.kiblasalat

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import com.example.kiblasalat.domain.repository.SettingsRepository
import com.example.kiblasalat.presentation.navigation.MainScreenContainer
import com.example.kiblasalat.ui.theme.KiblaSalatTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply saved language before layout creation
        lifecycleScope.launch {
            val savedLang = settingsRepository.getSelectedLanguage().first()
            val currentLocales = AppCompatDelegate.getApplicationLocales()
            if (currentLocales.isEmpty || currentLocales.get(0)?.language != savedLang) {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(savedLang))
            }
        }

        enableEdgeToEdge()
        setContent {
            KiblaSalatTheme {
                val isOnboardingCompleted by settingsRepository.isOnboardingCompleted()
                    .collectAsState(initial = null)

                if (isOnboardingCompleted == null) {
                    // Loading placeholder
                    androidx.compose.material3.Surface(
                        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.background
                    ) {
                        Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                            androidx.compose.material3.CircularProgressIndicator(
                                color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                } else if (isOnboardingCompleted == true) {
                    MainScreenContainer()
                } else {
                    com.example.kiblasalat.presentation.ui.screens.OnboardingScreen()
                }
            }
        }
    }
}