package ru.glindaquint.weatherapp.types.openWeatherMap

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherMapService {
    @GET("weather")
    suspend fun getWeatherByCity(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String,
        @Query("lang") language: String = "ru",
        @Query("units") unitsType: String = "metric",
    ): OWMApiAnswer

    @GET("weather")
    suspend fun getWeatherByLocation(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("lang") lang: String = "ru",
        @Query("units") unitsType: String = "metric",
    ): OWMApiAnswer
}
