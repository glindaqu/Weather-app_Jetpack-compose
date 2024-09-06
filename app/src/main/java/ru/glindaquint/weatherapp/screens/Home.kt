package ru.glindaquint.weatherapp.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import ru.glindaquint.weatherapp.R
import ru.glindaquint.weatherapp.types.openWeatherMap.OWMApiAnswer
import ru.glindaquint.weatherapp.viewModels.implementation.WeatherViewModel

@SuppressLint("MissingPermission")
@Suppress("ktlint:standard:function-naming")
@Composable
fun Home() {
    val weatherViewModel =
        ViewModelProvider(LocalContext.current as ComponentActivity)[WeatherViewModel::class.java]

    var cityData by remember { mutableStateOf<OWMApiAnswer?>(null) }
    val apiKey = stringResource(R.string.open_weather_key)

    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()

    val fusedLocationClient =
        LocationServices
            .getFusedLocationProviderClient(LocalContext.current)

    if (!checkLocationPermissions(LocalContext.current)) {
        ActivityCompat.requestPermissions(
            context as ComponentActivity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            101,
        )
    }

    fusedLocationClient.lastLocation.addOnSuccessListener {
        if (it != null) {
            coroutineScope.launch {
                cityData = weatherViewModel.getWeatherByLocation(it, apiKey)
            }
        }
    }

    when (cityData) {
        null -> Error()
        else -> City(cityData!!)
    }
}

private fun checkLocationPermissions(context: Context): Boolean =
    !(
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED
    )

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
