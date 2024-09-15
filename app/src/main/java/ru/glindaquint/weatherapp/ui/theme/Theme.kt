package ru.glindaquint.weatherapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val lightScheme =
    lightColorScheme(
        primary = primaryLight,
        onPrimary = backgroundLight,
        secondary = secondaryLight,
        background = backgroundLight,
        onBackground = onBackgroundLight,
        surface = surfaceLight,
    )

private val darkScheme =
    darkColorScheme(
        primary = primaryDark,
        onPrimary = onPrimaryDark,
        secondary = secondaryDark,
        background = backgroundDark,
        onBackground = onBackgroundDark,
        surface = surfaceDark,
    )

@Suppress("ktlint:standard:function-naming")
@Composable
fun WeatherAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            darkTheme -> darkScheme
            else -> lightScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
