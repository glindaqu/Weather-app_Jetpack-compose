package ru.glindaquint.weatherapp.screens.home

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import ru.glindaquint.weatherapp.DEBOUNCE_DELAY
import ru.glindaquint.weatherapp.PADDING
import ru.glindaquint.weatherapp.R
import ru.glindaquint.weatherapp.services.openWeatherMap.api.OWMApiAnswer
import ru.glindaquint.weatherapp.ui.components.WeatherScaffold
import ru.glindaquint.weatherapp.ui.theme.Typography
import ru.glindaquint.weatherapp.viewModels.implementation.CityPickViewModel
import ru.glindaquint.weatherapp.viewModels.implementation.WeatherViewModel
import kotlin.math.roundToInt

@SuppressLint("MissingPermission", "CommitPrefEdits", "UnusedMaterial3ScaffoldPaddingParameter")
@Suppress("ktlint:standard:function-naming")
@Composable
fun Home() {
    val weatherViewModel =
        ViewModelProvider(LocalContext.current as ComponentActivity)[WeatherViewModel::class.java]
    var cityData by remember { mutableStateOf<OWMApiAnswer?>(null) }
    val observer =
        Observer<OWMApiAnswer?> { currentWeather ->
            cityData = currentWeather
        }
    weatherViewModel.currentWeather.observe(LocalLifecycleOwner.current, observer)
    val context = LocalContext.current
    val uiState by weatherViewModel.uiState.collectAsState()
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(LocalContext.current)
    val launcherForActivityResult =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
            weatherViewModel.shouldShowPermissionsRequire = false
            if (isGranted) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    weatherViewModel.getWeatherByLocation(location)
                }
            } else {
                weatherViewModel.getWeatherByCity(WeatherViewModel.DEFAULT_CITY)
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
                    if (location != null) {
                        weatherViewModel.getWeatherByLocation(location)
                    }
                }
            }

            else -> {
                if (weatherViewModel.shouldShowPermissionsRequire) {
                    launcherForActivityResult.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                } else {
                    weatherViewModel.getWeatherByCity(WeatherViewModel.DEFAULT_CITY)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            weatherViewModel.currentWeather.removeObserver(observer)
        }
    }

    when (uiState) {
        UIState.NoInternet -> NoInternetError()
        UIState.WeatherLoading -> Loading()
        UIState.WeatherLoaded ->
            WeatherDetail(
                weather = cityData!!,
                onScaffoldIconClick = { weatherViewModel.uiState.value = UIState.SearchCity },
            )

        UIState.SearchCity ->
            SearchCity(
                onCityPick = { name, country, _ ->
                    weatherViewModel.getWeatherByCity("$name,$country")
                },
                onScaffoldIconClick = { weatherViewModel.uiState.value = UIState.WeatherLoaded },
            )

        UIState.WeatherLoadingError -> WeatherLoadingError()
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun SearchCity(
    onCityPick: (String, String, String) -> Unit,
    onScaffoldIconClick: () -> Unit,
) {
    val cityPickViewModel =
        ViewModelProvider(LocalContext.current as ComponentActivity)[CityPickViewModel::class.java]
    val cities by cityPickViewModel.cities.collectAsState(initial = null)
    WeatherScaffold(
        title = "Поиск города",
        icon = Icons.Default.Close,
        onIconClick = onScaffoldIconClick,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            CitySearchField(onInput = {
                cityPickViewModel.refreshCities(it)
            })
            Spacer(modifier = Modifier.weight(0.01f))
            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(0.9f),
                verticalArrangement = Arrangement.spacedBy(0.5.dp),
            ) {
                items(cities ?: listOf()) {
                    CityItem(
                        name = it.localNames?.ru ?: it.name!!,
                        country = it.country!!,
                        state = it.state ?: "",
                        onClick = { city, country, st ->
                            onCityPick(city, country, st)
                        },
                    )
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun CityItem(
    name: String,
    country: String,
    state: String,
    onClick: (String, String, String) -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.onPrimary)
                .clickable { onClick(name, country, state) },
    ) {
        Text(
            text = "$name ($country, $state)",
            style = Typography.bodyLarge,
            modifier = Modifier.padding(PADDING),
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun CitySearchField(onInput: (String) -> Unit) {
    var textFieldState by remember { mutableStateOf(TextFieldValue("")) }
    TextField(
        value = textFieldState,
        onValueChange = { textFieldState = it },
        modifier = Modifier.fillMaxWidth().padding(PADDING),
        trailingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = null)
        },
    )
    LaunchedEffect(key1 = textFieldState, block = {
        if (textFieldState.text.isNotBlank()) {
            delay(DEBOUNCE_DELAY)
            onInput(textFieldState.text)
        }
    })
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun WeatherLoadingError() {
    val animation by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.error))
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LottieAnimation(
            composition = animation,
            modifier = Modifier.fillMaxSize(0.8f),
            iterations = LottieConstants.IterateForever,
        )
        Text(text = "Что-то пошло не так...", style = Typography.titleMedium)
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun WeatherDetail(
    weather: OWMApiAnswer,
    onScaffoldIconClick: () -> Unit,
) {
    WeatherScaffold(title = "Погода", onIconClick = onScaffoldIconClick, icon = Icons.Default.Search) {
        City(weather)
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun NoInternetError() {
    val animation by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.no_internet))
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LottieAnimation(
            composition = animation,
            modifier = Modifier.fillMaxSize(0.8f),
            iterations = LottieConstants.IterateForever,
        )
        Text(text = "Нет подключения к интернету", style = Typography.titleMedium)
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun Loading() {
    val animation by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading))
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LottieAnimation(
            composition = animation,
            modifier = Modifier.fillMaxSize(0.3f),
            iterations = LottieConstants.IterateForever,
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun City(data: OWMApiAnswer) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(0.5f),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = data.name ?: "Неизвестно",
                    textAlign = TextAlign.Center,
                    style = Typography.titleLarge,
                )
                Row(horizontalArrangement = Arrangement.Center) {
                    Text(
                        text =
                            "${data.weather[0].description?.capitalize(Locale.current)} " +
                                "${data.main?.temp?.roundToInt()}°C",
                        style = Typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        Column(modifier = Modifier.weight(0.5f)) {}
    }
}
