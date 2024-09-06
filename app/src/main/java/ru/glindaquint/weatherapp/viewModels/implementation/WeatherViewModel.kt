package ru.glindaquint.weatherapp.viewModels.implementation

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.glindaquint.weatherapp.types.openWeatherMap.OWMApiAnswer
import ru.glindaquint.weatherapp.types.openWeatherMap.OpenWeatherMapService
import ru.glindaquint.weatherapp.viewModels.api.IWeatherViewModel

class WeatherViewModel(
    application: Application,
) : AndroidViewModel(application),
    IWeatherViewModel {
    private val retrofit =
        Retrofit
            .Builder()
            .baseUrl("https://ru.api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    private val service = retrofit.create(OpenWeatherMapService::class.java)

    override suspend fun getWeatherByCity(
        cityName: String,
        apiKey: String,
    ): OWMApiAnswer = service.getWeatherByCity(cityName, apiKey)

    override suspend fun getWeatherByLocation(
        location: Location,
        apiKey: String,
    ): OWMApiAnswer =
        service.getWeatherByLocation(
            lat = location.latitude,
            lon = location.longitude,
            apiKey = apiKey,
        )

    companion object {
        const val DEFAULT_CITY = "St. Petersburg"
    }
}
