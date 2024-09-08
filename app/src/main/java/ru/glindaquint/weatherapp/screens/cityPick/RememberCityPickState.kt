package ru.glindaquint.weatherapp.screens.cityPick

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun rememberCityPickState(): CityPickState =
    remember {
        CityPickState()
    }
