package ru.glindaquint.weatherapp.viewModels.api

import androidx.lifecycle.MutableLiveData
import ru.glindaquint.weatherapp.services.openWeatherMap.api.OWMGeoApiAnswer

interface ICityPickViewModel {
    fun findCitiesByName(
        cityName: String,
        apiKey: String,
    ): MutableLiveData<List<OWMGeoApiAnswer>?>

    fun refreshCities(name: String)
}
