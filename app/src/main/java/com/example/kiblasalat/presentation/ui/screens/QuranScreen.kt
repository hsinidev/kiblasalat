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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kiblasalat.R
import com.example.kiblasalat.domain.model.Ayah
import com.example.kiblasalat.domain.model.Surah
import com.example.kiblasalat.presentation.viewmodel.QuranViewModel

enum class QuranTheme(val label: String, val bgColor: Color, val textColor: Color, val cardColor: Color) {
    CREAM("Cream", Color(0xFFFDFBF7), Color(0xFF2C2518), Color(0xFFF5EFE2)),
    GOLD("Warm Gold", Color(0xFFF7F1E3), Color(0xFF2C2518), Color(0xFFEFE8D4)),
    DARK("Night Mode", Color(0xFF121212), Color(0xFFE5E0D8), Color(0xFF1E1E1E))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(
    viewModel: QuranViewModel = hiltViewModel()
) {
    val selectedSurahId by viewModel.selectedSurahId.collectAsState()

    AnimatedContent(
        targetState = selectedSurahId,
        transitionSpec = {
            if (targetState != null) {
                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                    slideOutHorizontally { width -> -width } + fadeOut()
                )
            } else {
                (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                    slideOutHorizontally { width -> width } + fadeOut()
                )
            }
        },
        label = "QuranScreenTransitions"
    ) { surahId ->
        if (surahId == null) {
            SurahListTabContainer(viewModel)
        } else {
            QuranReaderView(
                surahId = surahId,
                viewModel = viewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahListTabContainer(viewModel: QuranViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("All Surahs", "Bookmarks")
    val searchQuery by viewModel.searchQuery.collectAsState()
    val surahs by viewModel.surahs.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = FontWeight.Bold) }
                )
            }
        }

        if (selectedTab == 0) {
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search Surah name...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }

        Box(modifier = Modifier.weight(1f)) {
            if (selectedTab == 0) {
                if (surahs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No Surahs found", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(surahs) { surah ->
                            SurahCardRow(surah = surah, onClick = { viewModel.selectSurah(surah.id) })
                        }
                    }
                }
            } else {
                if (bookmarks.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.BookmarkBorder,
                                contentDescription = "No bookmarks",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No bookmarked verses yet", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(bookmarks) { ayah ->
                            BookmarkedAyahRow(
                                ayah = ayah,
                                onRemoveBookmark = { viewModel.toggleBookmark(ayah.surahId, ayah.numberInSurah) },
                                onGoToSurah = { viewModel.selectSurah(ayah.surahId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SurahCardRow(
    surah: Surah,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), shape = CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.secondary, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = surah.id.toString(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = surah.englishName,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${surah.revelationType} • ${surah.totalAyahs} Verses",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (surah.isBookmarkedAny) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = "Bookmarked",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp).padding(end = 8.dp)
                    )
                }
                Text(
                    text = surah.name,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
fun BookmarkedAyahRow(
    ayah: Ayah,
    onRemoveBookmark: () -> Unit,
    onGoToSurah: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Surah Index: ${ayah.surahId} • Verse: ${ayah.numberInSurah}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Row {
                    IconButton(onClick = onGoToSurah, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Read Surah",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(onClick = onRemoveBookmark, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Remove Bookmark",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = ayah.textArabic,
                fontFamily = FontFamily.Serif,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = ayah.textEnglish,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranReaderView(
    surahId: Int,
    viewModel: QuranViewModel
) {
    val ayahs by viewModel.ayahs.collectAsState()
    val surahs by viewModel.surahs.collectAsState()
    val currentSurah = remember(surahId, surahs) {
        surahs.find { it.id == surahId }
    }

    // Audio Playback states
    val isPlaying by viewModel.isPlaying.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val activeAudioSurahId by viewModel.activeAudioSurahId.collectAsState()
    val selectedReciterId by viewModel.selectedReciterId.collectAsState()
    val downloadProgress by viewModel.audioDownloadProgress.collectAsState()
    val downloadError by viewModel.audioDownloadError.collectAsState()

    var activeTheme by remember { mutableStateOf(QuranTheme.CREAM) }
    var readingMode by remember { mutableStateOf(0) } // 0 = Mushaf (Continuous Text), 1 = Verse-by-Verse List
    var showTranslation by remember { mutableStateOf(false) } // Only applicable for Verse-by-Verse

    val arabicFontSize by viewModel.arabicFontSize.collectAsState()
    val translationFontSize by viewModel.translationFontSize.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = currentSurah?.englishName ?: "Quran",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (activeTheme == QuranTheme.DARK) Color.White else Color(0xFF0D5C3A)
                        )
                        Text(
                            text = "${currentSurah?.revelationType?.uppercase()} • ${currentSurah?.totalAyahs} VERSES",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.selectSurah(null) }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = if (activeTheme == QuranTheme.DARK) Color.White else Color(0xFF0D5C3A)
                        )
                    }
                },
                actions = {
                    // Font adjustment controls
                    IconButton(onClick = { viewModel.decreaseArabicFont() }) {
                        Icon(Icons.Default.ZoomOut, contentDescription = "Font Down", tint = if (activeTheme == QuranTheme.DARK) Color.White else Color(0xFF0D5C3A))
                    }
                    IconButton(onClick = { viewModel.increaseArabicFont() }) {
                        Icon(Icons.Default.ZoomIn, contentDescription = "Font Up", tint = if (activeTheme == QuranTheme.DARK) Color.White else Color(0xFF0D5C3A))
                    }

                    // Reading Mode toggle
                    IconButton(onClick = { readingMode = if (readingMode == 0) 1 else 0 }) {
                        Icon(
                            imageVector = if (readingMode == 0) Icons.Default.List else Icons.Default.MenuBook,
                            contentDescription = "Mode",
                            tint = if (activeTheme == QuranTheme.DARK) Color.White else Color(0xFF0D5C3A)
                        )
                    }

                    // Theme selector menu
                    var showThemeMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showThemeMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Theme",
                                tint = if (activeTheme == QuranTheme.DARK) Color.White else Color(0xFF0D5C3A)
                            )
                        }
                        DropdownMenu(
                            expanded = showThemeMenu,
                            onDismissRequest = { showThemeMenu = false }
                        ) {
                            QuranTheme.values().forEach { theme ->
                                DropdownMenuItem(
                                    text = { Text(theme.label) },
                                    onClick = {
                                        activeTheme = theme
                                        showThemeMenu = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (activeTheme == QuranTheme.DARK) Color(0xFF1E1E1E) else Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(activeTheme.bgColor)
        ) {
            // Audio recitation control panel
            AudioPanel(
                viewModel = viewModel,
                currentSurahId = surahId,
                isPlaying = isPlaying,
                duration = duration,
                currentPosition = currentPosition,
                activeAudioSurahId = activeAudioSurahId,
                selectedReciterId = selectedReciterId,
                downloadProgress = downloadProgress,
                downloadError = downloadError,
                isDark = activeTheme == QuranTheme.DARK
            )

            // Dynamic Content Area
            if (ayahs.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF0D5C3A))
                }
            } else {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (readingMode == 0) {
                        // Mushaf Continuous Text Layout
                        val mushafText = buildAnnotatedString {
                            ayahs.forEach { ayah ->
                                append(ayah.textArabic)
                                append(" ")
                                val arabicNumber = convertToArabicDigits(ayah.numberInSurah)
                                withStyle(
                                    style = SpanStyle(
                                        color = Color(0xFFC5A880),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = (arabicFontSize * 0.75f).sp
                                    )
                                ) {
                                    append("﴿$arabicNumber﴾")
                                }
                                append("   ")
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(16.dp)
                        ) {
                            item {
                                // Islamic Header Card
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = activeTheme.cardColor),
                                    border = BorderStroke(1.dp, Color(0xFFC5A880).copy(alpha = 0.5f))
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = currentSurah?.name ?: "",
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 24.sp,
                                            color = activeTheme.textColor
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${currentSurah?.englishNameTranslation}",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                // Bismillah (unless Surah Al-Fatiha or At-Tawbah)
                                if (surahId != 1 && surahId != 9) {
                                    Text(
                                        text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                        fontFamily = FontFamily.Serif,
                                        fontSize = (arabicFontSize * 0.95f).sp,
                                        fontWeight = FontWeight.Bold,
                                        color = activeTheme.textColor,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                                    )
                                }

                                // Arabic text flows right-to-left
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                    Text(
                                        text = mushafText,
                                        style = TextStyle(
                                            fontFamily = FontFamily.Serif,
                                            fontSize = arabicFontSize.sp,
                                            lineHeight = (arabicFontSize * 1.7f).sp,
                                            textAlign = TextAlign.Justify,
                                            color = activeTheme.textColor
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    } else {
                        // Interactive Verse-by-Verse Layout
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Sub-header to toggle translation
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(activeTheme.cardColor)
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Translation English",
                                    fontSize = 12.sp,
                                    color = activeTheme.textColor,
                                    fontWeight = FontWeight.Bold
                                )
                                Switch(
                                    checked = showTranslation,
                                    onCheckedChange = { showTranslation = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF0D5C3A),
                                        checkedTrackColor = Color(0xFF0D5C3A).copy(alpha = 0.4f)
                                    )
                                )
                            }

                            LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                // Optional Bismillah Header in list mode
                                if (surahId != 1 && surahId != 9) {
                                    item {
                                        Text(
                                            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                            fontFamily = FontFamily.Serif,
                                            fontSize = (arabicFontSize * 0.95f).sp,
                                            fontWeight = FontWeight.Bold,
                                            color = activeTheme.textColor,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                                        )
                                    }
                                }

                                items(ayahs) { ayah ->
                                    InteractiveAyahCard(
                                        ayah = ayah,
                                        arabicFontSize = arabicFontSize,
                                        translationFontSize = translationFontSize,
                                        showTranslation = showTranslation,
                                        theme = activeTheme,
                                        viewModel = viewModel
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

@Composable
fun InteractiveAyahCard(
    ayah: Ayah,
    arabicFontSize: Float,
    translationFontSize: Float,
    showTranslation: Boolean,
    theme: QuranTheme,
    viewModel: QuranViewModel
) {
    val clipboardManager = LocalClipboardManager.current
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = theme.cardColor),
        border = BorderStroke(0.5.dp, Color(0xFFC5A880).copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ayah Star Marker
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color(0xFFC5A880).copy(alpha = 0.15f), shape = CircleShape)
                        .border(1.dp, Color(0xFFC5A880), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ayah.numberInSurah.toString(),
                        fontWeight = FontWeight.Bold,
                        color = theme.textColor,
                        fontSize = 11.sp
                    )
                }

                // Action Buttons
                Row {
                    IconButton(
                        onClick = { viewModel.toggleBookmark(ayah.surahId, ayah.numberInSurah) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (ayah.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = Color(0xFFC5A880),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { clipboardManager.setText(AnnotatedString(ayah.textArabic)) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = Color(0xFFC5A880),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Arabic text flows right-to-left
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Text(
                    text = ayah.textArabic,
                    fontFamily = FontFamily.Serif,
                    fontSize = arabicFontSize.sp,
                    lineHeight = (arabicFontSize * 1.5f).sp,
                    color = theme.textColor,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }

            if (showTranslation) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = theme.textColor.copy(alpha = 0.1f), thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = ayah.textEnglish,
                    fontSize = translationFontSize.sp,
                    lineHeight = (translationFontSize * 1.4f).sp,
                    color = theme.textColor.copy(alpha = 0.8f),
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

fun convertToArabicDigits(number: Int): String {
    val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    val builder = StringBuilder()
    val numStr = number.toString()
    for (element in numStr) {
        if (element in '0'..'9') {
            builder.append(arabicDigits[element - '0'])
        } else {
            builder.append(element)
        }
    }
    return builder.toString()
}

@Composable
fun AudioPanel(
    viewModel: QuranViewModel,
    currentSurahId: Int,
    isPlaying: Boolean,
    duration: Int,
    currentPosition: Int,
    activeAudioSurahId: Int?,
    selectedReciterId: String,
    downloadProgress: Float?,
    downloadError: String?,
    isDark: Boolean
) {
    val panelBg = if (isDark) Color(0xFF2C2C2C) else Color(0xFFF0EBE1)
    val textColor = if (isDark) Color.White else Color(0xFF0D5C3A)
    val isCurrentSurahPlaying = activeAudioSurahId == currentSurahId

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = panelBg),
        border = BorderStroke(0.5.dp, Color(0xFFC5A880).copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Top Row: Reciter dropdown and play buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reciter Selector Dropdown
                var showRecitersMenu by remember { mutableStateOf(false) }
                val currentReciter = viewModel.reciters.find { it.id == selectedReciterId } ?: viewModel.reciters[0]

                Box {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showRecitersMenu = true }
                            .background(Color(0xFFC5A880).copy(alpha = 0.2f))
                            .border(0.5.dp, Color(0xFFC5A880), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = textColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currentReciter.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = textColor, modifier = Modifier.size(16.dp))
                        }
                    }

                    DropdownMenu(
                        expanded = showRecitersMenu,
                        onDismissRequest = { showRecitersMenu = false }
                    ) {
                        viewModel.reciters.forEach { reciter ->
                            DropdownMenuItem(
                                text = { Text(reciter.name, fontSize = 12.sp) },
                                onClick = {
                                    viewModel.selectReciter(reciter.id)
                                    showRecitersMenu = false
                                }
                            )
                        }
                    }
                }

                // Play / Pause / Stop Buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isCurrentSurahPlaying && downloadProgress != null) {
                        CircularProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier.size(28.dp),
                            color = Color(0xFF0D5C3A),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Caching: ${(downloadProgress * 100).toInt()}%",
                            fontSize = 10.sp,
                            color = textColor
                        )
                    } else {
                        // Play/Pause button
                        FilledIconButton(
                            onClick = { viewModel.togglePlayPause(currentSurahId) },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Color(0xFF0D5C3A)
                            ),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isCurrentSurahPlaying && isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play Recitation",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        if (isCurrentSurahPlaying) {
                            Spacer(modifier = Modifier.width(8.dp))
                            // Stop button
                            IconButton(
                                onClick = { viewModel.stopAudio() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.Red.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Stop Recitation",
                                    tint = Color.Red,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Row: Seek bar (only visible when playing this surah)
            if (isCurrentSurahPlaying && duration > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTime(currentPosition),
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Slider(
                        value = currentPosition.toFloat(),
                        onValueChange = { viewModel.seekTo(it.toInt()) },
                        valueRange = 0f..duration.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF0D5C3A),
                            activeTrackColor = Color(0xFF0D5C3A)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )
                    Text(
                        text = formatTime(duration),
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }

            if (downloadError != null && isCurrentSurahPlaying) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = downloadError,
                    color = Color.Red,
                    fontSize = 10.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun formatTime(millis: Int): String {
    val sec = (millis / 1000) % 60
    val min = (millis / (1000 * 60)) % 60
    return String.format(java.util.Locale.US, "%02d:%02d", min, sec)
}
