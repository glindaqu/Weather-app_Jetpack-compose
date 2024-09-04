package ru.glindaquint.weatherapp.viewModels.api

import ru.glindaquint.weatherapp.types.openWeatherMap.OWMApiAnswer

interface IWeatherViewModel {
    suspend fun getWeatherByCity(
        cityName: String,
        apiKey: String,
    ): OWMApiAnswer
}
