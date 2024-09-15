package ru.glindaquint.weatherapp.viewModels.implementation

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.glindaquint.weatherapp.R
import ru.glindaquint.weatherapp.services.openWeatherMap.OpenWeatherMapService
import ru.glindaquint.weatherapp.services.openWeatherMap.api.OWMGeoApiAnswer
import ru.glindaquint.weatherapp.viewModels.api.ICityPickViewModel
import java.lang.ref.WeakReference

class CityPickViewModel(
    application: Application,
) : AndroidViewModel(application),
    ICityPickViewModel {
    private val retrofit =
        Retrofit
            .Builder()
            .baseUrl("https://ru.api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    private val service = retrofit.create(OpenWeatherMapService::class.java)
    private val apiKey = application.resources.getString(R.string.open_weather_key)

    private val sharedPrefs =
        application.applicationContext.getSharedPreferences(
            application.packageName,
            Context.MODE_PRIVATE,
        )

    val cities = MutableLiveData<List<OWMGeoApiAnswer>?>(null)

    val context = WeakReference(application.applicationContext)

    init {
        viewModelScope.launch {
            this@CityPickViewModel.findCitiesByName(
                apiKey = apiKey,
                cityName =
                    sharedPrefs.getString("LAST_LOCATION", WeatherViewModel.DEFAULT_CITY)
                        ?: WeatherViewModel.DEFAULT_CITY,
            )
        }
    }

    override fun findCitiesByName(
        cityName: String,
        apiKey: String,
    ): MutableLiveData<List<OWMGeoApiAnswer>?> {
        val call = service.getCitiesByName(name = cityName, apiKey = apiKey)
        call.enqueue(
            object : Callback<List<OWMGeoApiAnswer>> {
                override fun onResponse(
                    call: Call<List<OWMGeoApiAnswer>>,
                    response: Response<List<OWMGeoApiAnswer>>,
                ) {
                    cities.value = response.body()
                }

                override fun onFailure(
                    call: Call<List<OWMGeoApiAnswer>>,
                    t: Throwable,
                ) {
                    if (context.get() != null) {
                        Toast
                            .makeText(
                                context.get()!!,
                                "Что-то пошло не так, попробуйте позже",
                                Toast.LENGTH_LONG,
                            ).show()
                    }
                }
            },
        )
        return cities
    }

    override fun refreshCities(name: String) {
        viewModelScope.launch {
            this@CityPickViewModel.findCitiesByName(cityName = name, apiKey = apiKey)
        }
    }
}
