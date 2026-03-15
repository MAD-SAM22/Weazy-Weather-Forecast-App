package com.example.weatherapp.ui.home

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.data.model.CurrentWeatherModel
import com.example.weatherapp.data.model.ForecastDisplayItem
import com.example.weatherapp.data.model.ForecastResponse
import com.example.weatherapp.data.model.GeocodingResponseItem
import com.example.weatherapp.data.repository.WeatherRepository
import com.example.weatherapp.data.source.local.WeatherDao
import com.example.weatherapp.data.source.local.entity.FavoriteCityEntity
import com.example.weatherapp.data.source.remote.LocationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel(
    private val repository: WeatherRepository,
    private val locationHelper: LocationHelper,
    private val weatherDao: WeatherDao
) : ViewModel() {

    var weatherState by mutableStateOf<CurrentWeatherModel?>(null)
        private set

    var hourlyForecast by mutableStateOf<List<ForecastDisplayItem>>(emptyList())
        private set

    var weeklyForecast by mutableStateOf<List<ForecastDisplayItem>>(emptyList())
        private set

    var isHourlySelected by mutableStateOf(true)
        private set

    var searchResults by mutableStateOf<List<GeocodingResponseItem>>(emptyList())
        private set

    private var searchJob: Job? = null

    init {
        loadWeatherForCurrentLocation()
    }

    fun loadWeatherForCurrentLocation() {
        Log.d("HomeViewModel", "Loading weather for current location...")
        locationHelper.getDeviceLocation { lat, lon ->
            Log.d("HomeViewModel", "Received location: $lat, $lon")
            fetchWeatherData(lat, lon)
        }
        
        viewModelScope.launch {
            delay(5000)
            if (weatherState == null) {
                Log.d("HomeViewModel", "Location timeout or fetch pending, trying default city")
                fetchWeatherData("London")
            }
        }
    }

    fun searchCities(query: String) {
        searchJob?.cancel()
        if (query.length < 3) {
            searchResults = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            delay(500)
            try {
                val response = repository.searchCity(query)
                if (response.isSuccessful) {
                    searchResults = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error searching cities", e)
            }
        }
    }

    fun addCityToFavorites(city: GeocodingResponseItem) {
        viewModelScope.launch {
            try {
                val weatherResponse = repository.getWeatherByCity(city.name)
                if (weatherResponse.isSuccessful) {
                    val weather = weatherResponse.body()
                    if (weather != null) {
                        weatherDao.insertFavoriteCity(
                            FavoriteCityEntity(
                                name = city.name,
                                country = city.country,
                                temp = weather.main.temp,
                                condition = weather.weather.firstOrNull()?.description ?: "",
                                icon = mapIconToAsset(weather.weather.firstOrNull()?.icon)
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error adding city to favorites", e)
            }
        }
    }

    private fun fetchWeatherData(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val weatherResponse = repository.getCurrentWeatherByCoords(lat, lon)
                if (weatherResponse.isSuccessful) {
                    weatherState = weatherResponse.body()
                    Log.d("HomeViewModel", "Weather fetch successful for $lat, $lon")
                } else {
                    val errorMsg = weatherResponse.errorBody()?.string()
                    Log.e("HomeViewModel", "Weather fetch failed (HTTP ${weatherResponse.code()}): $errorMsg")
                }

                val forecastResponse = repository.getForecastByCoords(lat, lon)
                if (forecastResponse.isSuccessful) {
                    processForecast(forecastResponse.body())
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Exception fetching weather by coords: ${e.message}", e)
            }
        }
    }

    fun fetchWeatherData(city: String) {
        viewModelScope.launch {
            try {
                val weatherResponse = repository.getWeatherByCity(city)
                if (weatherResponse.isSuccessful) {
                    weatherState = weatherResponse.body()
                    Log.d("HomeViewModel", "Weather fetch successful for $city")
                } else {
                    val errorMsg = weatherResponse.errorBody()?.string()
                    Log.e("HomeViewModel", "Weather fetch failed for $city (HTTP ${weatherResponse.code()}): $errorMsg")
                }

                val forecastResponse = repository.getForecastByCity(city)
                if (forecastResponse.isSuccessful) {
                    processForecast(forecastResponse.body())
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Exception fetching weather by city: ${e.message}", e)
            }
        }
    }

    private fun processForecast(forecast: ForecastResponse?) {
        forecast?.let {
            val allItems = it.list
            
            // Hourly Forecast (next 8 items, 24 hours)
            hourlyForecast = allItems.take(8).map { item ->
                ForecastDisplayItem(
                    time = formatTime(item.dt),
                    iconAssetPath = mapIconToAsset(item.weather.firstOrNull()?.icon),
                    temp = "${item.main.temp.toInt()}°"
                )
            }

            // Weekly Forecast (one item per day)
            weeklyForecast = allItems.filterIndexed { index, _ -> index % 8 == 0 }.take(5).map { item ->
                ForecastDisplayItem(
                    time = formatDate(item.dt),
                    iconAssetPath = mapIconToAsset(item.weather.firstOrNull()?.icon),
                    temp = "${item.main.temp.toInt()}°"
                )
            }
        }
    }

    fun toggleForecastType(isHourly: Boolean) {
        isHourlySelected = isHourly
    }

    private fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("h a", Locale.getDefault())
        return sdf.format(Date(timestamp * 1000))
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEE", Locale.getDefault())
        return sdf.format(Date(timestamp * 1000))
    }

    private fun mapIconToAsset(icon: String?): String {
        return when (icon) {
            "01d", "01n" -> "icons/sun.png"
            "02d", "02n", "03d", "03n", "04d", "04n" -> "icons/scoudy_night.png"
            "09d", "09n", "10d", "10n" -> "icons/rain.png"
            else -> "icons/water_drops_night.png"
        }
    }
}
