package ru.glindaquint.weatherapp.services.openWeatherMap.api

import com.google.gson.annotations.SerializedName
import ru.glindaquint.weatherapp.services.openWeatherMap.types.LocalNames

data class OWMGeoApiAnswer(
    @SerializedName("name") var name: String? = null,
    @SerializedName("local_names") var localNames: LocalNames? = LocalNames(),
    @SerializedName("lat") var lat: Double? = null,
    @SerializedName("lon") var lon: Double? = null,
    @SerializedName("country") var country: String? = null,
    @SerializedName("state") var state: String? = null,
)
