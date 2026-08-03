package com.example.kiblasalat.presentation.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kiblasalat.presentation.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val selectedMethod by viewModel.calculationMethod.collectAsState()
    val selectedMadhab by viewModel.asrMadhab.collectAsState()
    val selectedAdhanVoice by viewModel.selectedAdhanVoice.collectAsState()
    val cacheSizeBytes by viewModel.cacheSize.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val downloadError by viewModel.downloadError.collectAsState()

    val cacheSizeMB = remember(cacheSizeBytes) {
        String.format(java.util.Locale.US, "%.2f MB", cacheSizeBytes.toDouble() / (1024 * 1024))
    }

    val methodsMap = mapOf(
        "MUSLIM_WORLD_LEAGUE" to "Muslim World League (Default)",
        "ISNA" to "ISNA (North America)",
        "EGYPT" to "Egyptian General Authority",
        "UMM_AL_QURA" to "Umm Al-Qura (Makkah)",
        "KARACHI" to "University of Islamic Sciences (Karachi)",
        "GULF" to "Gulf Region",
        "DUBAI" to "Dubai Method",
        "TURKEY" to "Diyanet (Turkey)",
        "MOROCCO" to "Morocco Habous Method"
    )

    var showMethodDropdown by remember { mutableStateOf(false) }
    var showVoiceDropdown by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Salat Parameters Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Prayer Calculation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // 1. Calculation Method Dropdown Selector
                Text(
                    text = "CALCULATION METHOD",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .clickable { showMethodDropdown = true }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = methodsMap[selectedMethod] ?: "Muslim World League",
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select"
                        )
                    }

                    DropdownMenu(
                        expanded = showMethodDropdown,
                        onDismissRequest = { showMethodDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        methodsMap.forEach { (key, value) ->
                            DropdownMenuItem(
                                text = { Text(value) },
                                onClick = {
                                    viewModel.setCalculationMethod(key)
                                    showMethodDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Asr Madhab Toggle
                Text(
                    text = "ASR JURISTIC SCHOOL (MADHAB)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Shafi / Standard Button
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.setAsrMadhab("SHAFI") },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedMadhab == "SHAFI") {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            } else {
                                MaterialTheme.colorScheme.background
                            }
                        ),
                        border = if (selectedMadhab == "SHAFI") {
                            BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                        } else {
                            BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        }
                    ) {
                        Box(modifier = Modifier.padding(14.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Standard", fontWeight = FontWeight.Bold, color = if (selectedMadhab == "SHAFI") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                                Text("Shafi'i, Maliki, Hanbali", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }
                    }

                    // Hanafi Button
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.setAsrMadhab("HANAFI") },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedMadhab == "HANAFI") {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            } else {
                                MaterialTheme.colorScheme.background
                            }
                        ),
                        border = if (selectedMadhab == "HANAFI") {
                            BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                        } else {
                            BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        }
                    ) {
                        Box(modifier = Modifier.padding(14.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Hanafi", fontWeight = FontWeight.Bold, color = if (selectedMadhab == "HANAFI") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                                Text("Later Asr shadow rule", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Adhan Voice Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Adhan Voice Alert",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    text = "PREFERRED ADHAN STYLE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )

                val activeVoiceOption = viewModel.adhanVoices.find { it.id == selectedAdhanVoice } ?: viewModel.adhanVoices[0]

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .clickable { showVoiceDropdown = true }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = activeVoiceOption.name,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select"
                        )
                    }

                    DropdownMenu(
                        expanded = showVoiceDropdown,
                        onDismissRequest = { showVoiceDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        viewModel.adhanVoices.forEach { voice ->
                            DropdownMenuItem(
                                text = { Text(voice.name) },
                                onClick = {
                                    viewModel.selectAdhanVoice(voice.id)
                                    showVoiceDropdown = false
                                }
                            )
                        }
                    }
                }

                val isShortAdhan by viewModel.isShortAdhanEnabled.collectAsState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .clickable { viewModel.setShortAdhanEnabled(!isShortAdhan) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Short Adhan (Allahu Akbar only)",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Play only first 10 seconds of Adhan",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = isShortAdhan,
                        onCheckedChange = { viewModel.setShortAdhanEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Preview player
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Preview selection:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    if (downloadProgress != null) {
                        CircularProgressIndicator(
                            progress = downloadProgress!!,
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Button(
                            onClick = { viewModel.playPreview(selectedAdhanVoice) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Play Preview", fontSize = 11.sp)
                        }
                    }
                }

                if (downloadError != null) {
                    Text(
                        text = downloadError!!,
                        color = Color.Red,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Storage and Cache Dashboard Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Storage & Cache Dashboard",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Clear offline cached Quran recitations and Adhan voices to optimize phone memory storage.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTAL CACHED AUDIO",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = cacheSizeMB,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Button(
                        onClick = { viewModel.clearCache() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear Cache")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Notifications Verification Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Notification Check",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Trigger a mockup notification to test the background notification sound channel.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = { viewModel.triggerTestNotification() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Trigger Test Notification")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Developer Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Developer Profile",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.kiblasalat.R.drawable.hsini),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )

                    Column {
                        Text(
                            text = "Hsini Mohamed",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "contact@hsini.dev",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { uriHandler.openUri("https://github.com/hsinidev") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "GitHub",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("GitHub", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { uriHandler.openUri("https://hsini.dev") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Website",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Website", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { uriHandler.openUri("https://linkedin.com/in/hsinidev/") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "LinkedIn",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("LinkedIn", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Footnote Info
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sajdah Link / Al-Mumin",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
            Text(
                text = "Version 1.0.0 • Offline Production Grade Utility",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
            )
        }
    }
}
