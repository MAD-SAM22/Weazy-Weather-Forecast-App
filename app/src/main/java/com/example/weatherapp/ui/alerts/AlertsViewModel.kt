package com.example.weatherapp.ui.alerts

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.weatherapp.data.model.GeocodingResponseItem
import com.example.weatherapp.data.repository.WeatherRepository
import com.example.weatherapp.data.source.local.WeatherDao
import com.example.weatherapp.data.source.local.entity.WeatherAlertEntity
import com.example.weatherapp.data.worker.WeatherNotificationWorker
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

class AlertsViewModel(
    application: Application,
    private val weatherDao: WeatherDao,
    private val repository: WeatherRepository
) : AndroidViewModel(application) {

    private val workManager = WorkManager.getInstance(application)

    val alerts: StateFlow<List<WeatherAlertEntity>> = weatherDao.getAllAlerts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    var searchResults by mutableStateOf<List<GeocodingResponseItem>>(emptyList())
        private set

    private var searchJob: Job? = null

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
                Log.e("AlertsViewModel", "Error searching cities", e)
            }
        }
    }

    fun addAlert(cityName: String, alertType: String, alertDate: Date, alertTime: String) {
        viewModelScope.launch {
            // Get coordinates for the city to avoid "City Not Found" errors in the worker
            var lat = 0.0
            var lon = 0.0
            try {
                val geoResponse = repository.searchCity(cityName)
                if (geoResponse.isSuccessful && geoResponse.body()?.isNotEmpty() == true) {
                    lat = geoResponse.body()!![0].lat
                    lon = geoResponse.body()!![0].lon
                }
            } catch (e: Exception) {
                Log.e("AlertsViewModel", "Error resolving city coordinates", e)
            }

            val entity = WeatherAlertEntity(
                cityName = cityName,
                alertType = alertType,
                alertDate = alertDate.time,
                alertTime = alertTime,
                lat = lat,
                lon = lon
            )
            val id = weatherDao.insertAlert(entity)
            
            // Schedule the worker
            scheduleNotification(cityName, alertType, alertDate, alertTime, id.toInt(), lat, lon)
        }
    }

    private fun scheduleNotification(cityName: String, alertType: String, alertDate: Date, alertTime: String, alertId: Int, lat: Double, lon: Double) {
        val calendar = Calendar.getInstance()
        val dateCalendar = Calendar.getInstance().apply { time = alertDate }
        
        val timeParts = alertTime.split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()

        calendar.set(Calendar.YEAR, dateCalendar.get(Calendar.YEAR))
        calendar.set(Calendar.MONTH, dateCalendar.get(Calendar.MONTH))
        calendar.set(Calendar.DAY_OF_MONTH, dateCalendar.get(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)

        val delay = calendar.timeInMillis - System.currentTimeMillis()

        if (delay > 0) {
            val data = workDataOf(
                "city_name" to cityName,
                "alert_type" to alertType,
                "lat" to lat,
                "lon" to lon
            )

            val workRequest = OneTimeWorkRequestBuilder<WeatherNotificationWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("alert_$alertId")
                .build()

            workManager.enqueueUniqueWork(
                "alert_$alertId",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
            Log.d("AlertsViewModel", "Scheduled alert for $cityName at $alertTime with delay $delay ms")
        } else {
            Log.w("AlertsViewModel", "Selected time is in the past. Not scheduling.")
        }
    }

    fun deleteAlert(alert: WeatherAlertEntity) {
        viewModelScope.launch {
            weatherDao.deleteAlert(alert)
            workManager.cancelUniqueWork("alert_${alert.id}")
        }
    }
}
