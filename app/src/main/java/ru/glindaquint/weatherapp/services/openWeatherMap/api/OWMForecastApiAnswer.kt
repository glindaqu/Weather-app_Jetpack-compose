package ru.glindaquint.weatherapp.services.openWeatherMap.api

import com.google.gson.annotations.SerializedName
import ru.glindaquint.weatherapp.services.openWeatherMap.types.City
import ru.glindaquint.weatherapp.services.openWeatherMap.types.List

data class OWMForecastApiAnswer(
    @SerializedName("cod") var cod: String? = null,
    @SerializedName("message") var message: Int? = null,
    @SerializedName("cnt") var cnt: Int? = null,
    @SerializedName("list") var list: ArrayList<List> = arrayListOf(),
    @SerializedName("city") var city: City? = City(),
)
