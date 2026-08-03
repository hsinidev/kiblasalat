package com.example.kiblasalat.presentation.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kiblasalat.R
import com.example.kiblasalat.presentation.viewmodel.OnboardingViewModel
import com.example.kiblasalat.presentation.viewmodel.OnboardingCountry
import com.example.kiblasalat.presentation.viewmodel.OnboardingCity
import com.example.kiblasalat.presentation.viewmodel.AdhanVoice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val currentStep by viewModel.currentStep.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()
    val selectedAdhanVoice by viewModel.selectedAdhanVoice.collectAsState()

    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val downloadError by viewModel.downloadError.collectAsState()

    val primaryGreen = Color(0xFF0D5C3A)
    val secondaryGold = Color(0xFFC5A880)
    val premiumCream = Color(0xFFFDFBF7)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = premiumCream
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Step indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (step in 1..4) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (step <= currentStep) primaryGreen else Color.LightGray.copy(alpha = 0.5f)
                            )
                    )
                }
            }

            // Title block
            Text(
                text = stringResource(id = R.string.onboarding_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = primaryGreen,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(id = R.string.onboarding_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Step Content
            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                        } else {
                            (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                        }
                    },
                    label = "StepTransition"
                ) { step ->
                    when (step) {
                        1 -> CountryStep(
                            countries = viewModel.countries,
                            selectedCountry = selectedCountry,
                            selectedCity = selectedCity,
                            onCountrySelected = { viewModel.selectCountry(it) },
                            onCitySelected = { viewModel.selectCity(it) },
                            primaryGreen = primaryGreen,
                            secondaryGold = secondaryGold
                        )
                        2 -> LanguageStep(
                            selectedLanguage = selectedLanguage,
                            onLanguageSelected = { viewModel.selectLanguage(it) },
                            primaryGreen = primaryGreen,
                            secondaryGold = secondaryGold
                        )
                        3 -> CalibrationStep(
                            viewModel = viewModel,
                            selectedCountry = selectedCountry,
                            primaryGreen = primaryGreen,
                            secondaryGold = secondaryGold
                        )
                        4 -> AdhanVoiceStep(
                            viewModel = viewModel,
                            selectedVoiceId = selectedAdhanVoice,
                            downloadProgress = downloadProgress,
                            downloadError = downloadError,
                            primaryGreen = primaryGreen,
                            secondaryGold = secondaryGold
                        )
                    }
                }
            }

            // Bottom Navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentStep > 1) {
                    OutlinedButton(
                        onClick = { viewModel.prevStep() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryGreen),
                        border = BorderStroke(1.dp, primaryGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(id = R.string.button_back))
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = {
                        if (currentStep < 4) {
                            viewModel.nextStep()
                        } else {
                            viewModel.saveAndFinish()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryGreen, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (currentStep < 4) stringResource(id = R.string.button_next) else stringResource(id = R.string.button_finish),
                        fontWeight = FontWeight.Bold
                    )
                    if (currentStep < 4) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
fun CountryStep(
    countries: List<OnboardingCountry>,
    selectedCountry: OnboardingCountry,
    selectedCity: OnboardingCity,
    onCountrySelected: (OnboardingCountry) -> Unit,
    onCitySelected: (OnboardingCity) -> Unit,
    primaryGreen: Color,
    secondaryGold: Color
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(id = R.string.select_country),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = primaryGreen,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Highlight Morocco by putting it at top or adding badge
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(countries) { country ->
                val isMorocco = country.id == "morocco"
                val isSelected = country.id == selectedCountry.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCountrySelected(country) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) primaryGreen.copy(alpha = 0.1f) else Color.White
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 0.5.dp,
                        color = if (isSelected) primaryGreen else Color.LightGray
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isSelected) primaryGreen else Color.LightGray
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(id = country.stringResId),
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) primaryGreen else Color.DarkGray
                            )
                        }

                        if (isMorocco) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(secondaryGold)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Morocco",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(id = R.string.select_city),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = primaryGreen,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            var expanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, primaryGreen, RoundedCornerShape(12.dp))
                        .clickable { expanded = true }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = selectedCity.stringResId),
                            fontWeight = FontWeight.Bold,
                            color = primaryGreen
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = primaryGreen)
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    selectedCountry.cities.forEach { city ->
                        DropdownMenuItem(
                            text = { Text(stringResource(id = city.stringResId)) },
                            onClick = {
                                onCitySelected(city)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageStep(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    primaryGreen: Color,
    secondaryGold: Color
) {
    val languages = listOf(
        Pair("ar", "العربية"),
        Pair("fr", "Français"),
        Pair("en", "English"),
        Pair("es", "Español")
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(id = R.string.language_setting),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = primaryGreen,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(languages) { (code, name) ->
                val isSelected = selectedLanguage == code
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLanguageSelected(code) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) primaryGreen.copy(alpha = 0.1f) else Color.White
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 0.5.dp,
                        color = if (isSelected) primaryGreen else Color.LightGray
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isSelected) primaryGreen else Color.LightGray
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = name,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) primaryGreen else Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalibrationStep(
    viewModel: OnboardingViewModel,
    selectedCountry: OnboardingCountry,
    primaryGreen: Color,
    secondaryGold: Color
) {
    val isMorocco = selectedCountry.id == "morocco"

    val offsetFajr by viewModel.offsetFajr.collectAsState()
    val offsetSunrise by viewModel.offsetSunrise.collectAsState()
    val offsetDhuhr by viewModel.offsetDhuhr.collectAsState()
    val offsetAsr by viewModel.offsetAsr.collectAsState()
    val offsetMaghrib by viewModel.offsetMaghrib.collectAsState()
    val offsetIsha by viewModel.offsetIsha.collectAsState()

    val adhanFajr by viewModel.adhanEnabledFajr.collectAsState()
    val adhanDhuhr by viewModel.adhanEnabledDhuhr.collectAsState()
    val adhanAsr by viewModel.adhanEnabledAsr.collectAsState()
    val adhanMaghrib by viewModel.adhanEnabledMaghrib.collectAsState()
    val adhanIsha by viewModel.adhanEnabledIsha.collectAsState()

    val prayers = listOf(
        Triple("Fajr", offsetFajr, adhanFajr),
        Triple("Sunrise", offsetSunrise, null),
        Triple("Dhuhr", offsetDhuhr, adhanDhuhr),
        Triple("Asr", offsetAsr, adhanAsr),
        Triple("Maghrib", offsetMaghrib, adhanMaghrib),
        Triple("Isha", offsetIsha, adhanIsha)
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(id = R.string.calibrate_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = primaryGreen
        )
        Text(
            text = stringResource(id = R.string.calibrate_desc),
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (isMorocco) {
            Card(
                colors = CardDefaults.cardColors(containerColor = primaryGreen.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, primaryGreen.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = primaryGreen)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Moroccan Ministry of Habous locked: Fajr 19°, Isha 17° plus default offsets (-3 Sunrise, +5 Dhuhr, +5 Maghrib) are pre-applied.",
                        fontSize = 11.sp,
                        color = primaryGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(prayers) { (name, offset, adhanEnabled) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(0.5.dp, Color.LightGray)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (adhanEnabled != null) {
                                Checkbox(
                                    checked = adhanEnabled,
                                    onCheckedChange = { viewModel.toggleAdhan(name) },
                                    colors = CheckboxDefaults.colors(checkedColor = primaryGreen)
                                )
                            } else {
                                Spacer(modifier = Modifier.width(48.dp))
                            }
                            Text(
                                text = name,
                                fontWeight = FontWeight.Bold,
                                color = primaryGreen
                            )
                        }

                        // Offset sizers
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.setOffset(name, offset - 1) }) {
                                Icon(Icons.Default.Remove, contentDescription = null, tint = primaryGreen)
                            }
                            Text(
                                text = if (offset >= 0) "+$offset min" else "$offset min",
                                fontWeight = FontWeight.Bold,
                                color = if (offset == 0) Color.DarkGray else secondaryGold
                            )
                            IconButton(onClick = { viewModel.setOffset(name, offset + 1) }) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = primaryGreen)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdhanVoiceStep(
    viewModel: OnboardingViewModel,
    selectedVoiceId: String,
    downloadProgress: Float?,
    downloadError: String?,
    primaryGreen: Color,
    secondaryGold: Color
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(id = R.string.adhan_voice_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = primaryGreen
        )
        Text(
            text = stringResource(id = R.string.adhan_voice_desc),
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val isShortAdhan by viewModel.shortAdhanEnabled.collectAsState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(0.5.dp, Color.LightGray, RoundedCornerShape(12.dp))
                .clickable { viewModel.setShortAdhanEnabled(!isShortAdhan) }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Short Adhan (Allahu Akbar only)",
                    fontWeight = FontWeight.Bold,
                    color = primaryGreen,
                    fontSize = 14.sp
                )
                Text(
                    text = "Play only first 10 seconds of Adhan",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
            Switch(
                checked = isShortAdhan,
                onCheckedChange = { viewModel.setShortAdhanEnabled(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = primaryGreen,
                    checkedTrackColor = primaryGreen.copy(alpha = 0.3f)
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (downloadError != null) {
            Text(
                text = downloadError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(viewModel.adhanVoices) { voice ->
                val isSelected = selectedVoiceId == voice.id
                val isDownloaded = viewModel.isVoiceDownloaded(voice.id)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectAdhanVoice(voice.id) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) primaryGreen.copy(alpha = 0.1f) else Color.White
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 0.5.dp,
                        color = if (isSelected) primaryGreen else Color.LightGray
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isSelected) primaryGreen else Color.LightGray
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(id = voice.stringResId),
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) primaryGreen else Color.DarkGray
                            )
                        }

                        // Preview player button
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSelected && downloadProgress != null) {
                                CircularProgressIndicator(
                                    progress = downloadProgress,
                                    modifier = Modifier.size(24.dp),
                                    color = primaryGreen,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Button(
                                    onClick = { viewModel.playPreview(voice.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isDownloaded) stringResource(id = R.string.preview_audio) else "Download",
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
