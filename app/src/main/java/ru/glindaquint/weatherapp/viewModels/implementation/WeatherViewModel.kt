package ru.glindaquint.weatherapp.viewModels.implementation

import android.app.Application
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
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    override suspend fun getWeatherByCity(
        cityName: String,
        apiKey: String,
    ): OWMApiAnswer {
        val service = retrofit.create(OpenWeatherMapService::class.java)
        return service.getWeatherByCity(cityName, apiKey)
    }
}
