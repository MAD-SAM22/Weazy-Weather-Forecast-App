package com.example.weatherapp.ui.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.data.source.local.WeatherDao
import kotlinx.coroutines.launch

class AlarmTriggerViewModel(private val weatherDao: WeatherDao) : ViewModel() {
    fun stopAlarm(cityName: String) {
        viewModelScope.launch {
            weatherDao.deleteAlertByCity(cityName)
        }
    }
}
