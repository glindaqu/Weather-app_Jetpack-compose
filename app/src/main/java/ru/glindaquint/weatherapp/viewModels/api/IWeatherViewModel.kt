package ru.glindaquint.weatherapp.viewModels.api

import android.location.Location
import androidx.lifecycle.MutableLiveData
import ru.glindaquint.weatherapp.services.openWeatherMap.api.OWMApiAnswer

interface IWeatherViewModel {
    fun getWeatherByCity(cityName: String): MutableLiveData<OWMApiAnswer?>

    fun getWeatherByLocation(location: Location): MutableLiveData<OWMApiAnswer?>
}
