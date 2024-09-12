package ru.glindaquint.weatherapp.viewModels.implementation

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.glindaquint.weatherapp.R
import ru.glindaquint.weatherapp.screens.home.UIState
import ru.glindaquint.weatherapp.services.openWeatherMap.OpenWeatherMapService
import ru.glindaquint.weatherapp.services.openWeatherMap.api.OWMApiAnswer
import ru.glindaquint.weatherapp.viewModels.api.IWeatherViewModel

class WeatherViewModel(
    application: Application,
) : AndroidViewModel(application),
    IWeatherViewModel {
    private val retrofit =
        Retrofit
            .Builder()
            .baseUrl("https://ru.api.openweathermap.org/")
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

    override fun getWeatherByCity(cityName: String): MutableLiveData<OWMApiAnswer?> {
        val call = service.getWeatherByCity(cityName, apiKey)
        call.enqueue(
            object : Callback<OWMApiAnswer> {
                override fun onResponse(
                    call: Call<OWMApiAnswer>,
                    response: Response<OWMApiAnswer>,
                ) {
                    uiState.value = UIState.WeatherLoaded
                    currentWeather.value = response.body()
                }

                override fun onFailure(
                    call: Call<OWMApiAnswer>,
                    t: Throwable,
                ) {
                    uiState.value = UIState.WeatherLoadingError
                }
            },
        )
        return currentWeather
    }

    override fun getWeatherByLocation(location: Location): MutableLiveData<OWMApiAnswer?> {
        val call =
            service.getWeatherByLocation(
                lat = location.latitude,
                lon = location.longitude,
                apiKey = apiKey,
            )
        call.enqueue(
            object : Callback<OWMApiAnswer> {
                override fun onResponse(
                    call: Call<OWMApiAnswer>,
                    response: Response<OWMApiAnswer>,
                ) {
                    uiState.value = UIState.WeatherLoaded
                    currentWeather.value = response.body()
                }

                override fun onFailure(
                    call: Call<OWMApiAnswer>,
                    t: Throwable,
                ) {
                    uiState.value = UIState.WeatherLoadingError
                }
            },
        )
        return currentWeather
    }

    companion object {
        const val DEFAULT_CITY = "St. Petersburg"
    }
}
