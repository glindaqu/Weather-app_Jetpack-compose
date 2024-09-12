package ru.glindaquint.weatherapp.services.openWeatherMap.api

import com.google.gson.annotations.SerializedName
import ru.glindaquint.weatherapp.services.openWeatherMap.types.Clouds
import ru.glindaquint.weatherapp.services.openWeatherMap.types.Coord
import ru.glindaquint.weatherapp.services.openWeatherMap.types.Main
import ru.glindaquint.weatherapp.services.openWeatherMap.types.Sys
import ru.glindaquint.weatherapp.services.openWeatherMap.types.Weather
import ru.glindaquint.weatherapp.services.openWeatherMap.types.Wind

data class OWMApiAnswer(
    @SerializedName("coord") var coord: Coord? = Coord(),
    @SerializedName("weather") var weather: ArrayList<Weather> = arrayListOf(),
    @SerializedName("base") var base: String? = null,
    @SerializedName("main") var main: Main? = Main(),
    @SerializedName("visibility") var visibility: Int? = null,
    @SerializedName("wind") var wind: Wind? = Wind(),
    @SerializedName("clouds") var clouds: Clouds? = Clouds(),
    @SerializedName("dt") var dt: Int? = null,
    @SerializedName("sys") var sys: Sys? = Sys(),
    @SerializedName("timezone") var timezone: Int? = null,
    @SerializedName("id") var id: Int? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("cod") var cod: Int? = null,
)
