package ru.glindaquint.weatherapp.types.openWeatherMap

import com.google.gson.annotations.SerializedName

data class Wind(
    @SerializedName("speed") var speed: Int? = null,
    @SerializedName("deg") var deg: Int? = null,
)
