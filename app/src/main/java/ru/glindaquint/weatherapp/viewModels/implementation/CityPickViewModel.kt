package ru.glindaquint.weatherapp.viewModels.implementation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.glindaquint.weatherapp.R
import ru.glindaquint.weatherapp.services.openWeatherMap.OpenWeatherMapService
import ru.glindaquint.weatherapp.services.openWeatherMap.api.OWMGeoApiAnswer
import ru.glindaquint.weatherapp.viewModels.api.ICityPickViewModel

class CityPickViewModel(
    application: Application,
) : AndroidViewModel(application),
    ICityPickViewModel {
    private val retrofit =
        Retrofit
            .Builder()
            .baseUrl("https://ru.api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    private val service = retrofit.create(OpenWeatherMapService::class.java)
    private val apiKey = application.resources.getString(R.string.open_weather_key)

    val cities = MutableStateFlow<List<OWMGeoApiAnswer>?>(null)

    init {
        viewModelScope.launch {
            cities.value =
                this@CityPickViewModel.findCitiesByName(apiKey = apiKey, cityName = "Уфа")
        }
    }

    override suspend fun findCitiesByName(
        cityName: String,
        apiKey: String,
    ): List<OWMGeoApiAnswer> = service.getCitiesByName(name = cityName, apiKey = apiKey)

    override fun refreshCities(name: String) {
        viewModelScope.launch {
            cities.value =
                this@CityPickViewModel.findCitiesByName(cityName = name, apiKey = apiKey)
        }
    }
}
