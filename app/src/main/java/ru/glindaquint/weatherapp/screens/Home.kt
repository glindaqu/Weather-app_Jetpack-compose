package ru.glindaquint.weatherapp.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModelProvider
import ru.glindaquint.weatherapp.R
import ru.glindaquint.weatherapp.types.openWeatherMap.OWMApiAnswer
import ru.glindaquint.weatherapp.viewModels.implementation.WeatherViewModel

@Suppress("ktlint:standard:function-naming")
@Composable
fun Home() {
    val weatherViewModel =
        ViewModelProvider(LocalContext.current as ComponentActivity)[WeatherViewModel::class.java]
    var cityData by remember { mutableStateOf<OWMApiAnswer?>(null) }
    val apiKey = stringResource(R.string.open_weather_key)

    LaunchedEffect(Unit) {
        cityData = weatherViewModel.getWeatherByCity("Novosibirsk", apiKey)
    }

    when (cityData) {
        null -> Error()
        else -> City(cityData!!)
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun Error() {
    Text(text = "Error occurred when attempting to connect to the server")
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun City(data: OWMApiAnswer) {
    Column {
        Text(text = "City")
        Text(text = data.name!!)
    }
}
