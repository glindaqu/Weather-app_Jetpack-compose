package ru.glindaquint.weatherapp.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import ru.glindaquint.weatherapp.R
import ru.glindaquint.weatherapp.types.openWeatherMap.OWMApiAnswer
import ru.glindaquint.weatherapp.viewModels.implementation.WeatherViewModel

@SuppressLint("MissingPermission", "CommitPrefEdits")
@Suppress("ktlint:standard:function-naming")
@Composable
fun Home() {
    val weatherViewModel =
        ViewModelProvider(LocalContext.current as ComponentActivity)[WeatherViewModel::class.java]

    var cityData by remember { mutableStateOf<OWMApiAnswer?>(null) }
    val apiKey = stringResource(R.string.open_weather_key)

    val context = LocalContext.current

    val sharedPreferences =
        LocalContext.current.getSharedPreferences(
            LocalContext.current.packageName,
            Context.MODE_PRIVATE,
        )
    val shouldShowLocalePermission =
        sharedPreferences.getBoolean("SHOULD_SHOW_LOCALE_PERMISSION", true)

    val coroutineScope = rememberCoroutineScope()

    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(LocalContext.current)

    val launcherForActivityResult =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
            sharedPreferences.edit().putBoolean("SHOULD_SHOW_LOCALE_PERMISSION", false).apply()
            if (isGranted) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    coroutineScope.launch {
                        cityData = weatherViewModel.getWeatherByLocation(location, apiKey)
                    }
                }
            } else {
                coroutineScope.launch {
                    cityData =
                        weatherViewModel.getWeatherByCity(WeatherViewModel.DEFAULT_CITY, apiKey)
                }
            }
        }

    LaunchedEffect(Unit) {
        when (
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
            )
        ) {
            PackageManager.PERMISSION_GRANTED -> {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    coroutineScope.launch {
                        cityData = weatherViewModel.getWeatherByLocation(location, apiKey)
                    }
                }
            }

            else -> {
                if (shouldShowLocalePermission) {
                    launcherForActivityResult.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                } else {
                    coroutineScope.launch {
                        cityData =
                            weatherViewModel.getWeatherByCity(WeatherViewModel.DEFAULT_CITY, apiKey)
                    }
                }
            }
        }
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
