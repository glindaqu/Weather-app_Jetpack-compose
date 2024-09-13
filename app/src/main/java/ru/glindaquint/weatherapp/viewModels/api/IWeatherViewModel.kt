package ru.glindaquint.weatherapp.viewModels.api

import android.location.Location

interface IWeatherViewModel {
    fun getWeatherByCity(cityName: String)

    fun getWeatherByLocation(location: Location)
}
