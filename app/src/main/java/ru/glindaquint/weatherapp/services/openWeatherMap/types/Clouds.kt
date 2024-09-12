package ru.glindaquint.weatherapp.services.openWeatherMap.types

import com.google.gson.annotations.SerializedName

data class Clouds(
    @SerializedName("all") var all: Int? = null,
)
