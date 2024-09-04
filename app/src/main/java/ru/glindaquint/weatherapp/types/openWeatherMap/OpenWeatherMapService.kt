package ru.glindaquint.weatherapp.types.openWeatherMap

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherMapService {
    @GET("weather")
    suspend fun getWeatherByCity(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String,
    ): OWMApiAnswer
}
