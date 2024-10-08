package ru.glindaquint.weatherapp.services.openWeatherMap

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import ru.glindaquint.weatherapp.services.openWeatherMap.api.OWMApiAnswer
import ru.glindaquint.weatherapp.services.openWeatherMap.api.OWMForecastApiAnswer
import ru.glindaquint.weatherapp.services.openWeatherMap.api.OWMGeoApiAnswer

interface OpenWeatherMapService {
    @GET("data/2.5/weather")
    fun getWeatherByCity(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String,
        @Query("lang") language: String = "ru",
        @Query("units") unitsType: String = "metric",
    ): Call<OWMApiAnswer>

    @GET("data/2.5/weather")
    fun getWeatherByLocation(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("lang") lang: String = "ru",
        @Query("units") unitsType: String = "metric",
    ): Call<OWMApiAnswer>

    @GET("geo/1.0/direct")
    fun getCitiesByName(
        @Query("q") name: String,
        @Query("lang") lang: String = "ru",
        @Query("limit") limit: Int = 20,
        @Query("appid") apiKey: String,
    ): Call<List<OWMGeoApiAnswer>>

    @GET("data/2.5/forecast")
    fun getForecastByCityName(
        @Query("q") name: String,
        @Query("appid") apiKey: String,
        @Query("lang") lang: String = "ru",
        @Query("units") units: String = "metric",
    ): Call<OWMForecastApiAnswer>

    @GET("data/2.5/forecast")
    fun getForecastByLocation(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("lang") lang: String = "ru",
        @Query("units") units: String = "metric",
    ): Call<OWMForecastApiAnswer>
}
