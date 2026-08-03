package com.example.kiblasalat.presentation.navigation

sealed class Screen(val route: String, val label: String) {
    object PrayerTimes : Screen("prayer_times", "Prayer")
    object Qibla : Screen("qibla", "Qibla")
    object Quran : Screen("quran", "Quran")
    object Adkar : Screen("adkar", "Adkar")
    object Settings : Screen("settings", "Settings")
}
