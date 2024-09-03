package ru.glindaquint.weatherapp.types.openWeatherMap

import com.google.gson.annotations.SerializedName

data class Clouds(
    @SerializedName("all") var all: Int? = null,
)
