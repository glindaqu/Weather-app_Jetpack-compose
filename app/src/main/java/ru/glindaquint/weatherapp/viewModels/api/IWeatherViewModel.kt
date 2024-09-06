package ru.glindaquint.weatherapp.viewModels.api

import android.location.Location
import ru.glindaquint.weatherapp.types.openWeatherMap.OWMApiAnswer

interface IWeatherViewModel {
    suspend fun getWeatherByCity(
        cityName: String,
        apiKey: String,
    ): OWMApiAnswer

    suspend fun getWeatherByLocation(
        location: Location,
        apiKey: String,
    ): OWMApiAnswer
}
