package com.example.kiblasalat.presentation.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kiblasalat.domain.model.PrayerTimeItem
import com.example.kiblasalat.domain.model.SalatTimes
import com.example.kiblasalat.presentation.viewmodel.FallbackCity
import com.example.kiblasalat.presentation.viewmodel.PrayerViewModel
import com.example.kiblasalat.presentation.viewmodel.SalatTimesState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerScreen(
    viewModel: PrayerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val salatTimesState by viewModel.salatTimesStateFlow.collectAsState()
    val gpsError by viewModel.gpsError.collectAsState()
    val isGpsLoading by viewModel.isGpsLoading.collectAsState()

    var showCityDialog by remember { mutableStateFlowOf(false) }
    var locationPermissionGranted by remember {
        mutableStateFlowOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        locationPermissionGranted = granted
        if (granted) {
            viewModel.requestGpsLocation()
        }
    }

    // Ensure notification permission for Android 13+
    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (locationPermissionGranted) {
            viewModel.requestGpsLocation()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Location & Date Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Al-Mumin / Sajdah Link",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    val activeLocationName = when (val state = salatTimesState) {
                        is SalatTimesState.Success -> state.salatTimes.locationName
                        else -> "Makkah (Default)"
                    }
                    Text(
                        text = activeLocationName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            
            Row {
                IconButton(
                    onClick = {
                        if (locationPermissionGranted) {
                            viewModel.requestGpsLocation()
                        } else {
                            launcher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    }
                ) {
                    if (isGpsLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "GPS Sync",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                IconButton(onClick = { showCityDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search City",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // GPS Denied Warning Banner
        if (!locationPermissionGranted) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Warning",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "GPS Location Disabled",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Grant location access for automatic local calculations, or select a city manually.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                        )
                    }
                    Button(
                        onClick = {
                            launcher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Enable", fontSize = 11.sp)
                    }
                }
            }
        }

        // GPS Fetch Errors
        gpsError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // Main content depending on state
        when (val state = salatTimesState) {
            is SalatTimesState.Loading -> {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is SalatTimesState.Error -> {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Error loading times: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
            is SalatTimesState.Success -> {
                val times = state.salatTimes
                
                // Countdown Card
                CountdownCard(times = times)

                Spacer(modifier = Modifier.height(16.dp))

                // Salat Times List
                Text(
                    text = "Daily Salat Times",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(times.prayers) { prayer ->
                        PrayerRow(prayer = prayer)
                    }
                }
            }
        }
    }

    // Predefined City Selector Dialog
    if (showCityDialog) {
        CitySelectionDialog(
            cities = viewModel.fallbackCities,
            onCitySelected = { city ->
                viewModel.selectFallbackCity(city)
                showCityDialog = false
            },
            onDismiss = { showCityDialog = false }
        )
    }
}

@Composable
fun CountdownCard(times: SalatTimes) {
    val locale = if (Locale.getDefault().language == "ar") Locale.forLanguageTag("ar-u-nu-latn") else Locale.getDefault()
    val df = SimpleDateFormat("hh:mm a", locale)
    val remainingSec = times.countdownMillis / 1000
    val hours = remainingSec / 3600
    val minutes = (remainingSec % 3600) / 60
    val seconds = remainingSec % 60
    val countdownString = String.format(Locale.US, "%02dh %02dm %02ds", hours, minutes, seconds)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.Companion.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
                .padding(20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column {
                Text(
                    text = "UPCOMING SALAT",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${times.nextPrayerName} at ${df.format(times.nextPrayerTime)}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Countdown",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "- $countdownString",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PrayerRow(prayer: PrayerTimeItem) {
    val locale = if (Locale.getDefault().language == "ar") Locale.forLanguageTag("ar-u-nu-latn") else Locale.getDefault()
    val df = SimpleDateFormat("hh:mm a", locale)
    val isNext = prayer.isNext

    val cardColor = if (isNext) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val borderStroke = if (isNext) {
        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = borderStroke
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (prayer.name) {
                        "Fajr" -> Icons.Default.WbTwilight
                        "Sunrise" -> Icons.Default.WbSunny
                        "Dhuhr" -> Icons.Default.LightMode
                        "Asr" -> Icons.Default.WbCloudy
                        "Maghrib" -> Icons.Default.WbSunny
                        "Isha" -> Icons.Default.NightsStay
                        else -> Icons.Default.Schedule
                    },
                    contentDescription = prayer.name,
                    tint = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = prayer.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium,
                        color = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    if (prayer.name == "Sunrise") {
                        Text(
                            text = "No Adhan alert",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = df.format(prayer.time),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium,
                    color = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                if (isNext) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "NEXT",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CitySelectionDialog(
    cities: List<FallbackCity>,
    onCitySelected: (FallbackCity) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateFlowOf("") }
    val filteredCities = remember(searchQuery) {
        cities.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Select City",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search location...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(modifier = Modifier.height(200.dp)) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filteredCities) { city ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onCitySelected(city) }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = city.name,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = String.format(Locale.US, "%.2f°, %.2f°", city.latitude, city.longitude),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

// Helpers for mutableStateOf inside Compose to look clean
fun <T> mutableStateFlowOf(value: T) = mutableStateOf(value)
