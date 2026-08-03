package com.example.kiblasalat.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.kiblasalat.presentation.ui.screens.PrayerScreen
import com.example.kiblasalat.presentation.ui.screens.QiblaScreen
import com.example.kiblasalat.presentation.ui.screens.QuranScreen
import com.example.kiblasalat.presentation.ui.screens.SettingsScreen

import androidx.compose.material.icons.filled.MenuBook
import com.example.kiblasalat.presentation.ui.screens.AdkarScreen

@Composable
fun MainScreenContainer() {
    val navController = rememberNavController()
    val items = listOf(
        Screen.PrayerTimes,
        Screen.Qibla,
        Screen.Quran,
        Screen.Adkar,
        Screen.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = when (screen) {
                                    Screen.PrayerTimes -> Icons.Default.DateRange
                                    Screen.Qibla -> Icons.Default.Explore
                                    Screen.Quran -> Icons.Default.Book
                                    Screen.Adkar -> Icons.Default.MenuBook
                                    Screen.Settings -> Icons.Default.Settings
                                },
                                contentDescription = screen.label
                            )
                        },
                        label = { Text(screen.label) },
                        selected = isSelected,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            indicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                        ),
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.PrayerTimes.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.PrayerTimes.route) {
                PrayerScreen()
            }
            composable(Screen.Qibla.route) {
                QiblaScreen()
            }
            composable(Screen.Quran.route) {
                QuranScreen()
            }
            composable(Screen.Adkar.route) {
                AdkarScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
