package com.example.kiblasalat.presentation.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kiblasalat.R

data class AdkarItem(
    val arabic: String,
    val translation: String,
    val source: String,
    val targetCount: Int
)

data class HadithItem(
    val arabic: String,
    val translation: String,
    val source: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdkarScreen() {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Morning Adkar", "Evening Adkar", "Hadith & Advice")

    val primaryGreen = Color(0xFF0D5C3A)
    val secondaryGold = Color(0xFFC5A880)
    val premiumCream = Color(0xFFFDFBF7)

    // Morning Supplications
    val morningAdkar = remember {
        listOf(
            AdkarItem(
                arabic = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ",
                translation = "Allah - there is no deity except Him, the Ever-Living, the Sustainer of [all] existence. Neither drowsiness overtakes Him nor sleep. To Him belongs whatever is in the heavens and whatever is on the earth...",
                source = "Ayat al-Kursi (2:255)",
                targetCount = 1
            ),
            AdkarItem(
                arabic = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ. قُلْ هُوَ اللَّهُ أَحَدٌ. اللَّهُ الصَّمَدُ. لَمْ يَلِدْ وَلَمْ يُولَدْ. وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ.",
                translation = "Say, \"He is Allah, [who is] One. Allah, the Eternal Refuge. He neither begets nor is born, Nor is there to Him any equivalent.\"",
                source = "Surah Al-Ikhlas (3 times)",
                targetCount = 3
            ),
            AdkarItem(
                arabic = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ. قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ. مِن شَرِّ مَا خَلَقَ. وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ. وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ. وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ.",
                translation = "Say, \"I seek refuge in the Lord of daybreak, From the evil of that which He created, And from the evil of darkness when it settles, And from the evil of the blowers in knots, And from the evil of an envier when he envies.\"",
                source = "Surah Al-Falaq (3 times)",
                targetCount = 3
            ),
            AdkarItem(
                arabic = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ. قُلْ أَعُوذُ بِرَبِّ النَّاسِ. مَلِكِ النَّاسِ. إِلَٰهِ النَّاسِ. مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ. الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ. مِنَ الْجِنَّةِ وَالنَّاسِ.",
                translation = "Say, \"I seek refuge in the Lord of mankind, The Sovereign of mankind, The God of mankind, From the evil of the retreating whisperer - Who whispers [evil] into the breasts of mankind - From among the jinn and mankind.\"",
                source = "Surah An-Nas (3 times)",
                targetCount = 3
            ),
            AdkarItem(
                arabic = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
                translation = "We have entered a new day and with it all dominion belongs to Allah, praise be to Allah. There is no deity worthy of worship except Allah alone, without partner...",
                source = "Hadith Muslim (1 time)",
                targetCount = 1
            ),
            AdkarItem(
                arabic = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
                translation = "O Allah, You are my Lord, there is no deity worthy of worship except You. You created me and I am Your servant, and I am faithful to Your covenant and promise as much as I can...",
                source = "Sayyid al-Istighfar (1 time)",
                targetCount = 1
            ),
            AdkarItem(
                arabic = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
                translation = "In the name of Allah with Whose name nothing can harm on earth or in heaven, and He is the All-Hearing, the All-Knowing.",
                source = "Hadith Abu Dawud (3 times)",
                targetCount = 3
            ),
            AdkarItem(
                arabic = "رَضِيتُ بِاللَّهِ رَبَّاً، وَبِالْإِسْلَامِ دِيناً، وَبِمُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ نَبِيَّاً",
                translation = "I am pleased with Allah as my Lord, with Islam as my religion, and with Muhammad (peace and blessings of Allah be upon him) as my Prophet.",
                source = "Hadith Tirmidhi (3 times)",
                targetCount = 3
            )
        )
    }

    // Evening Supplications
    val eveningAdkar = remember {
        listOf(
            AdkarItem(
                arabic = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ",
                translation = "Allah - there is no deity except Him, the Ever-Living, the Sustainer of [all] existence...",
                source = "Ayat al-Kursi (2:255)",
                targetCount = 1
            ),
            AdkarItem(
                arabic = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ. قُلْ هُوَ اللَّهُ أَحَدٌ. اللَّهُ الصَّمَدُ. لَمْ يَلِدْ وَلَمْ يُولَدْ. وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ.",
                translation = "Say, \"He is Allah, [who is] One. Allah, the Eternal Refuge...\"",
                source = "Surah Al-Ikhlas (3 times)",
                targetCount = 3
            ),
            AdkarItem(
                arabic = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ. قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ. مِن شَرِّ مَا خَلَقَ. وَمِن شَرِّ وَقَبَ. وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ. وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ.",
                translation = "Say, \"I seek refuge in the Lord of daybreak...\"",
                source = "Surah Al-Falaq (3 times)",
                targetCount = 3
            ),
            AdkarItem(
                arabic = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ. قُلْ أَعُوذُ بِرَبِّ النَّاسِ. مَلِكِ النَّاسِ. إِلَٰهِ النَّاسِ. مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ. الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ. مِنَ الْجِنَّةِ وَالنَّاسِ.",
                translation = "Say, \"I seek refuge in the Lord of mankind...\"",
                source = "Surah An-Nas (3 times)",
                targetCount = 3
            ),
            AdkarItem(
                arabic = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
                translation = "We have entered the evening and with it all dominion belongs to Allah, praise be to Allah...",
                source = "Hadith Muslim (1 time)",
                targetCount = 1
            ),
            AdkarItem(
                arabic = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
                translation = "O Allah, You are my Lord, there is no deity worthy of worship except You...",
                source = "Sayyid al-Istighfar (1 time)",
                targetCount = 1
            ),
            AdkarItem(
                arabic = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
                translation = "In the name of Allah with Whose name nothing can harm on earth or in heaven...",
                source = "Hadith Abu Dawud (3 times)",
                targetCount = 3
            ),
            AdkarItem(
                arabic = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
                translation = "I seek refuge in the perfect words of Allah from the evil of that which He has created.",
                source = "Hadith Muslim (3 times)",
                targetCount = 3
            )
        )
    }

    // Hadith & Advice
    val hadiths = remember {
        listOf(
            HadithItem(
                arabic = "«إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى»",
                translation = "\"Actions are judged by motives (intentions), and each person will be rewarded according to what they intended.\"",
                source = "Sahih al-Bukhari & Sahih Muslim"
            ),
            HadithItem(
                arabic = "«مَنْ سَلَكَ طَرِيقًا يَلْتَمِسُ فِيهِ عِلْمًا سَهَّلَ اللَّهُ لَهُ بِهِ طَرِيقًا إِلَى الْجَنَّةِ»",
                translation = "\"Whoever treads a path in search of knowledge, Allah will make easy for him the path to Paradise.\"",
                source = "Sahih Muslim"
            ),
            HadithItem(
                arabic = "«لاَ يُؤْمِنُ أَحَدُكُمْ حَتَّى يُحِبَّ لأَخِيهِ مَا يُحِبُّ لِنَفْسِهِ»",
                translation = "\"None of you will believe until he loves for his brother what he loves for himself.\"",
                source = "Sahih al-Bukhari"
            ),
            HadithItem(
                arabic = "«الدَّالُّ عَلَى الْخَيْرِ كَفَاعِلِهِ»",
                translation = "\"Whoever guides someone to goodness will have a reward equal to that of the doer.\"",
                source = "Jami` at-Tirmidhi"
            ),
            HadithItem(
                arabic = "«مَنْ كَانَ يُؤْمِنُ بِاللَّهِ وَالْيَوْمِ الآخِرِ فَلْيَقُلْ خَيْرًا أَوْ لِيَصْمُتْ»",
                translation = "\"Whoever believes in Allah and the Last Day, let him speak goodness or remain silent.\"",
                source = "Sahih al-Bukhari & Sahih Muslim"
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Spiritual Companion",
                        fontWeight = FontWeight.Bold,
                        color = primaryGreen
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(premiumCream)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = primaryGreen
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                        } else {
                            (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                        }
                    },
                    label = "TabContentTransition"
                ) { tab ->
                    when (tab) {
                        0 -> SupplicationsTab(morningAdkar, primaryGreen, secondaryGold)
                        1 -> SupplicationsTab(eveningAdkar, primaryGreen, secondaryGold)
                        2 -> HadithsTab(hadiths, primaryGreen, secondaryGold)
                    }
                }
            }
        }
    }
}

@Composable
fun SupplicationsTab(
    items: List<AdkarItem>,
    primaryGreen: Color,
    secondaryGold: Color
) {
    // Local state to track click counts for each item in the list
    val currentCounts = remember(items) { mutableStateListOf(*Array(items.size) { items[it].targetCount }) }
    val hapticFeedback = LocalHapticFeedback.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(items) { index, adkar ->
            val remaining = currentCounts[index]
            val isCompleted = remaining == 0

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isCompleted) {
                        if (remaining > 0) {
                            currentCounts[index] = remaining - 1
                            if (remaining - 1 == 0) {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            } else {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCompleted) primaryGreen.copy(alpha = 0.05f) else Color.White
                ),
                border = BorderStroke(
                    width = if (isCompleted) 1.5.dp else 0.5.dp,
                    color = if (isCompleted) primaryGreen else Color.LightGray
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(secondaryGold.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = adkar.source,
                                color = secondaryGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Digital Counter Box
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (isCompleted) primaryGreen else Color(0xFFF7F1E3))
                                .border(1.dp, if (isCompleted) primaryGreen else secondaryGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted) {
                                Icon(Icons.Default.Check, contentDescription = "Done", tint = Color.White)
                            } else {
                                Text(
                                    text = remaining.toString(),
                                    fontWeight = FontWeight.Bold,
                                    color = primaryGreen,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = adkar.arabic,
                        fontFamily = FontFamily.Serif,
                        fontSize = 20.sp,
                        lineHeight = 30.sp,
                        color = if (isCompleted) Color.Gray else primaryGreen,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = adkar.translation,
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun HadithsTab(
    hadiths: List<HadithItem>,
    primaryGreen: Color,
    secondaryGold: Color
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(hadiths) { _, hadith ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(0.5.dp, Color.LightGray)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(secondaryGold.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = hadith.source,
                            color = secondaryGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = hadith.arabic,
                        fontFamily = FontFamily.Serif,
                        fontSize = 22.sp,
                        lineHeight = 32.sp,
                        color = primaryGreen,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = hadith.translation,
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        lineHeight = 18.sp,
                        fontStyle = FontStyle.Italic
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.5.dp)

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Copy action
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString("${hadith.arabic}\n\n${hadith.translation}\n(${hadith.source})"))
                                Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = primaryGreen)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Share action
                        IconButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "${hadith.arabic}\n\n${hadith.translation}\n(${hadith.source})")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Hadith"))
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = primaryGreen)
                        }
                    }
                }
            }
        }
    }
}
