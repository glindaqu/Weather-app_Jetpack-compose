package ru.glindaquint.weatherapp.viewModels.implementation

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.glindaquint.weatherapp.R
import ru.glindaquint.weatherapp.screens.home.UIState
import ru.glindaquint.weatherapp.services.openWeatherMap.OpenWeatherMapService
import ru.glindaquint.weatherapp.services.openWeatherMap.api.OWMApiAnswer
import ru.glindaquint.weatherapp.services.openWeatherMap.api.OWMForecastApiAnswer
import ru.glindaquint.weatherapp.viewModels.api.IWeatherViewModel

class WeatherViewModel(
    application: Application,
) : AndroidViewModel(application),
    IWeatherViewModel {
    private class WeatherCallBack<T>(
        val onSuccess: ((response: Response<T>) -> Unit)?,
        val onFailure: (() -> Unit)?,
    ) : Callback<T> {
        override fun onResponse(
            call: Call<T>,
            response: Response<T>,
        ) {
            if (response.body() != null) {
                onSuccess?.invoke(response)
            } else {
                Log.e("SERVER ERROR", "Server has sent null")
                onFailure?.invoke()
            }
        }

        override fun onFailure(
            call: Call<T>,
            t: Throwable,
        ) {
            Log.e("NETWORK ERROR", "${t.message}")
            onFailure?.invoke()
        }
    }

    private val client = OkHttpClient()
    private val retrofit =
        Retrofit
            .Builder()
            .baseUrl("https://ru.api.openweathermap.org/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    private val service = retrofit.create(OpenWeatherMapService::class.java)

    private val sharedPrefs =
        application.applicationContext.getSharedPreferences(
            application.packageName,
            Context.MODE_PRIVATE,
        )

    private val apiKey = application.resources.getString(R.string.open_weather_key)

    val uiState = MutableStateFlow(UIState.WeatherLoading)
    val currentWeather = MutableLiveData<OWMApiAnswer?>()
    val currentWeatherForecast = MutableLiveData<OWMForecastApiAnswer?>()

    @Suppress("ktlint:standard:backing-property-naming")
    private var _shouldShowPermissionsRequire =
        sharedPrefs.getBoolean("SHOULD_SHOW_LOCALE_PERMISSION", true)

    var shouldShowPermissionsRequire: Boolean
        get() = _shouldShowPermissionsRequire

        @SuppressLint("CommitPrefEdits")
        set(value) {
            if (_shouldShowPermissionsRequire != value) {
                sharedPrefs.edit().putBoolean("SHOULD_SHOW_LOCALE_PERMISSION", value)
            }
            _shouldShowPermissionsRequire = value
        }

    override fun getWeatherByCity(cityName: String) {
        uiState.value = UIState.WeatherLoading
        val weatherCall =
            service.getWeatherByCity(
                cityName = cityName,
                apiKey = apiKey,
            )
        val forecastCall =
            service.getForecastByCityName(
                name = cityName,
                apiKey = apiKey,
            )
        weatherCall.enqueue(
            WeatherCallBack<OWMApiAnswer>(onFailure = {
                client.dispatcher.cancelAll()
                uiState.value = UIState.WeatherLoadingError
            }, onSuccess = { weather ->
                forecastCall.enqueue(
                    WeatherCallBack<OWMForecastApiAnswer>(onSuccess = { forecast ->
                        currentWeatherForecast.value = forecast.body()
                        currentWeather.value = weather.body()
                        uiState.value = UIState.WeatherLoaded
                    }, onFailure = {
                        client.dispatcher.cancelAll()
                        uiState.value = UIState.WeatherLoadingError
                    }),
                )
            }),
        )
    }

    override fun getWeatherByLocation(location: Location) {
        uiState.value = UIState.WeatherLoading
        val weatherCall =
            service.getWeatherByLocation(
                lat = location.latitude,
                lon = location.longitude,
                apiKey = apiKey,
            )
        val forecastCall =
            service.getForecastByLocation(
                lat = location.latitude,
                lon = location.longitude,
                apiKey = apiKey,
            )
        forecastCall.enqueue(
            WeatherCallBack<OWMForecastApiAnswer>(onSuccess = { forecast ->
                weatherCall.enqueue(
                    WeatherCallBack<OWMApiAnswer>(onFailure = {
                        client.dispatcher.cancelAll()
                        uiState.value = UIState.WeatherLoadingError
                    }, onSuccess = { weather ->
                        currentWeatherForecast.value = forecast.body()
                        currentWeather.value = weather.body()
                        uiState.value = UIState.WeatherLoaded
                    }),
                )
            }, onFailure = {
                client.dispatcher.cancelAll()
                uiState.value = UIState.WeatherLoadingError
            }),
        )
    }

    companion object {
        const val DEFAULT_CITY = "St. Petersburg"
    }
}
