package com.example.kiblasalat.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = SandGold,
    secondary = EmeraldGreen,
    tertiary = GoldAccent,
    background = DarkForestBlack,
    surface = ForestSurface,
    onPrimary = DarkForestBlack,
    onSecondary = TextLight,
    onBackground = TextLight,
    onSurface = TextLight
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldGreen,
    secondary = SandGold,
    tertiary = GoldAccent,
    background = OffWhiteCream,
    surface = PureWhite,
    onPrimary = PureWhite,
    onSecondary = TextDark,
    onBackground = TextDark,
    onSurface = TextDark
)

@Composable
fun KiblaSalatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}