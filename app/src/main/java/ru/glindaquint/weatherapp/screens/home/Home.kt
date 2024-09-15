package ru.glindaquint.weatherapp.screens.home

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import coil.compose.AsyncImage
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
import ru.glindaquint.weatherapp.services.openWeatherMap.api.OWMForecastApiAnswer
import ru.glindaquint.weatherapp.services.openWeatherMap.api.OWMGeoApiAnswer
import ru.glindaquint.weatherapp.ui.components.WeatherScaffold
import ru.glindaquint.weatherapp.ui.theme.Typography
import ru.glindaquint.weatherapp.viewModels.implementation.CityPickViewModel
import ru.glindaquint.weatherapp.viewModels.implementation.WeatherViewModel
import java.util.Date
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("MissingPermission", "CommitPrefEdits", "UnusedMaterial3ScaffoldPaddingParameter")
@Suppress("ktlint:standard:function-naming")
@Composable
fun Home() {
    val weatherViewModel =
        ViewModelProvider(LocalContext.current as ComponentActivity)[WeatherViewModel::class.java]

    var weather by remember { mutableStateOf<OWMApiAnswer?>(null) }
    var forecast by remember { mutableStateOf<OWMForecastApiAnswer?>(null) }

    val weatherObserver = Observer<OWMApiAnswer?> { weather = it }
    val forecastObserver = Observer<OWMForecastApiAnswer?> { forecast = it }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val uiState by weatherViewModel.uiState.collectAsState()

    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(LocalContext.current)
    val launcherForActivityResult =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
            weatherViewModel.shouldShowPermissionsRequire = false
            if (isGranted) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        weatherViewModel.getWeatherByLocation(location)
                    }
                }
            } else {
                weatherViewModel.getWeatherByCity(WeatherViewModel.DEFAULT_CITY)
            }
        }

    LaunchedEffect(Unit) {
        weatherViewModel.currentWeather.observe(lifecycleOwner, weatherObserver)
        weatherViewModel.currentWeatherForecast.observe(lifecycleOwner, forecastObserver)
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
                    weatherViewModel.getWeatherByCity(weatherViewModel.lastLocatedCity)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            weatherViewModel.currentWeather.removeObserver(weatherObserver)
            weatherViewModel.currentWeatherForecast.removeObserver(forecastObserver)
        }
    }

    when (uiState) {
        UIState.WeatherLoading -> Loading()
        UIState.WeatherLoaded ->
            WeatherDetail(
                weather = weather!!,
                forecast = forecast!!,
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
    var cities by remember { mutableStateOf<List<OWMGeoApiAnswer>?>(null) }
    val observer = Observer<List<OWMGeoApiAnswer>?> { cities = it }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        cityPickViewModel.cities.observe(lifecycleOwner, observer)
    }

    DisposableEffect(Unit) {
        onDispose {
            cityPickViewModel.cities.removeObserver(observer)
        }
    }

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
            CitySearchField(onInput = { cityPickViewModel.refreshCities(it) })
            Spacer(modifier = Modifier.weight(0.01f))
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .weight(0.9f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
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
                .background(MaterialTheme.colorScheme.secondary)
                .clickable { onClick(name, country, state) }
                .padding(PADDING)
                .heightIn(min = 40.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "$name ($country, $state)",
            style = Typography.bodyLarge,
            modifier = Modifier.fillMaxWidth(),
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
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(PADDING),
        trailingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = null)
        },
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
            ),
        placeholder = {
            Text(text = "Название города", style = Typography.bodyLarge)
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
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
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

@RequiresApi(Build.VERSION_CODES.O)
@Suppress("ktlint:standard:function-naming")
@Composable
private fun WeatherDetail(
    weather: OWMApiAnswer,
    forecast: OWMForecastApiAnswer,
    onScaffoldIconClick: () -> Unit,
) {
    WeatherScaffold(
        title = "Погода",
        onIconClick = onScaffoldIconClick,
        icon = Icons.Default.Search,
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        ) {
            City(weather)
            Forecast(forecast)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("SimpleDateFormat", "DefaultLocale")
@Suppress("ktlint:standard:function-naming")
@Composable
private fun Forecast(forecast: OWMForecastApiAnswer) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        val forecastForToday = forecast.list.filter { Date(it.dt!! * 1000).date == Date().date }
        val forecastForTomorrow =
            forecast.list.filter { Date(it.dt!! * 1000).date == Date().date + 1 }
        Text(text = "Сегодня", modifier = Modifier.fillMaxWidth().padding(PADDING))
        forecastForToday.forEach {
            WeatherForecastRow(model = it)
        }
        Text(text = "Завтра", modifier = Modifier.fillMaxWidth().padding(PADDING))
        for (i in 0..<9 - forecastForToday.size) {
            WeatherForecastRow(model = forecastForTomorrow[i])
        }
    }
}

@SuppressLint("DefaultLocale")
@Suppress("ktlint:standard:function-naming")
@Composable
private fun WeatherForecastRow(
    model: ru.glindaquint.weatherapp.services.openWeatherMap.types.List,
    @SuppressLint("SimpleDateFormat") format: SimpleDateFormat = SimpleDateFormat("HH:mm"),
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondary)
                .padding(
                    PADDING,
                ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        val dateStamp = model.dt!! * 1000
        Text(
            text = format.format(dateStamp),
            style = Typography.bodyMedium,
            modifier = Modifier.fillMaxWidth(0.4f),
        )
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.wrapContentSize()) {
            AsyncImage(
                model = "https://openweathermap.org/img/w/${model.weather[0].icon}.png",
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(0.3f).size(40.dp),
            )
            Text(
                text = "${String.format("%3d", model.main?.temp?.roundToInt())}°C",
                style = Typography.bodyLarge,
                modifier = Modifier.fillMaxWidth(0.3f),
            )
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun Loading() {
    val animation by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading))
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
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
        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.3f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
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
    }
}
