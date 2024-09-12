package ru.glindaquint.weatherapp.viewModels.api

import ru.glindaquint.weatherapp.services.openWeatherMap.api.OWMGeoApiAnswer

interface ICityPickViewModel {
    suspend fun findCitiesByName(
        cityName: String,
        apiKey: String,
    ): List<OWMGeoApiAnswer>

    fun refreshCities(name: String)
}
