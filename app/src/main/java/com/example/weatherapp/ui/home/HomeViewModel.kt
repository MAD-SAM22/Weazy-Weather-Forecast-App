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
import com.example.weatherapp.data.source.local.SettingsManager
import com.example.weatherapp.data.source.remote.LocationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel(
    private val repository: WeatherRepository,
    private val locationHelper: LocationHelper,
    private val weatherDao: WeatherDao,
    private val settingsManager: SettingsManager
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

    var isLoading by mutableStateOf(false)
        private set

    private var searchJob: Job? = null
    
    private var currentLat: Double? = null
    private var currentLon: Double? = null
    private var currentCity: String? = null
    
    private var isFirstLoad = true

    init {
        // Observe settings changes to refresh data
        viewModelScope.launch {
            combine(
                settingsManager.tempUnits,
                settingsManager.windSpeedUnits,
                settingsManager.language
            ) { _, _, _ -> }.collectLatest {
                if (!isFirstLoad) {
                    refreshData()
                }
            }
        }
        
        loadWeatherForCurrentLocation()
    }

    private fun refreshData() {
        val lat = currentLat
        val lon = currentLon
        if (lat != null && lon != null) {
            fetchWeatherData(lat, lon)
        } else if (currentCity != null) {
            fetchWeatherData(currentCity!!)
        }
    }

    fun loadWeatherForCurrentLocation() {
        Log.d("HomeViewModel", "Loading weather for current location...")
        isFirstLoad = false
        isLoading = true
        locationHelper.getDeviceLocation { lat, lon ->
            // Only update if we haven't manually selected a city from favorites in the meantime
            if (currentCity == null) {
                currentLat = lat
                currentLon = lon
                Log.d("HomeViewModel", "Received location: $lat, $lon")
                fetchWeatherData(lat, lon)
            } else {
                isLoading = false
            }
        }
        
        viewModelScope.launch {
            delay(5000)
            if (weatherState == null && currentLat == null && currentCity == null) {
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
                val weatherResponse = repository.getCurrentWeatherByCoords(city.lat, city.lon)
                if (weatherResponse.isSuccessful) {
                    val weather = weatherResponse.body()
                    if (weather != null) {
                        weatherDao.insertFavoriteCity(
                            FavoriteCityEntity(
                                name = city.name,
                                country = city.country,
                                temp = weather.main.temp,
                                condition = weather.weather.firstOrNull()?.description ?: "",
                                icon = mapIconToAsset(weather.weather.firstOrNull()?.icon),
                                lat = city.lat,
                                lon = city.lon
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error adding city to favorites", e)
            }
        }
    }

    fun addLocationToFavorites(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val geoResponse = repository.reverseGeocode(lat, lon)
                if (geoResponse.isSuccessful && geoResponse.body()?.isNotEmpty() == true) {
                    val cityInfo = geoResponse.body()!![0]
                    
                    val weatherResponse = repository.getCurrentWeatherByCoords(lat, lon)
                    if (weatherResponse.isSuccessful) {
                        val weather = weatherResponse.body()
                        if (weather != null) {
                            weatherDao.insertFavoriteCity(
                                FavoriteCityEntity(
                                    name = cityInfo.name,
                                    country = cityInfo.country,
                                    temp = weather.main.temp,
                                    condition = weather.weather.firstOrNull()?.description ?: "",
                                    icon = mapIconToAsset(weather.weather.firstOrNull()?.icon),
                                    lat = lat,
                                    lon = lon
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error adding location to favorites", e)
            }
        }
    }

    fun fetchWeatherData(lat: Double, lon: Double) {
        currentLat = lat
        currentLon = lon
        currentCity = null
        isLoading = true
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
                if (e !is kotlinx.coroutines.CancellationException) {
                    Log.e("HomeViewModel", "Exception fetching weather by coords: ${e.message}", e)
                }
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchWeatherData(city: String) {
        currentCity = city
        currentLat = null
        currentLon = null
        isLoading = true
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
                if (e !is kotlinx.coroutines.CancellationException) {
                    Log.e("HomeViewModel", "Exception fetching weather by city: ${e.message}", e)
                }
            } finally {
                isLoading = false
            }
        }
    }

    private fun processForecast(forecast: ForecastResponse?) {
        forecast?.let {
            val allItems = it.list
            
            hourlyForecast = allItems.take(8).map { item ->
                ForecastDisplayItem(
                    time = formatTime(item.dt),
                    iconAssetPath = mapIconToAsset(item.weather.firstOrNull()?.icon),
                    temp = "${item.main.temp.toInt()}°"
                )
            }

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
        val lang = settingsManager.language.value
        val locale = Locale.forLanguageTag(lang)
        val sdf = SimpleDateFormat("h a", locale)
        return sdf.format(Date(timestamp * 1000))
    }

    private fun formatDate(timestamp: Long): String {
        val lang = settingsManager.language.value
        val locale = Locale.forLanguageTag(lang)
        val sdf = SimpleDateFormat("EEE", locale)
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
