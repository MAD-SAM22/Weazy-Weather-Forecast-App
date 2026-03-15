package com.example.weatherapp.ui.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.data.source.local.WeatherDao
import com.example.weatherapp.data.source.local.entity.WeatherAlertEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

class AlertsViewModel(private val weatherDao: WeatherDao) : ViewModel() {

    val alerts: StateFlow<List<WeatherAlertEntity>> = weatherDao.getAllAlerts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addAlert(cityName: String, alertType: String, alertDate: Date, alertTime: String) {
        viewModelScope.launch {
            weatherDao.insertAlert(
                WeatherAlertEntity(
                    cityName = cityName,
                    alertType = alertType,
                    alertDate = alertDate.time, // Convert Date to Long
                    alertTime = alertTime
                )
            )
        }
    }

    fun deleteAlert(alert: WeatherAlertEntity) {
        viewModelScope.launch {
            weatherDao.deleteAlert(alert)
        }
    }
}
