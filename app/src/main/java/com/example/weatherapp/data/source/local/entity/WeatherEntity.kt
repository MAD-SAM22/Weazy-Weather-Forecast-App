package com.example.weatherapp.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "current_weather")
data class CurrentWeatherEntity(
    @PrimaryKey val id: Int = 1,
    val cityName: String,
    val temp: Double,
    val tempMax: Double,
    val tempMin: Double,
    val condition: String,
    val icon: String,
    val timestamp: Long
)

@Entity(tableName = "favorite_cities")
data class FavoriteCityEntity(
    @PrimaryKey val name: String,
    val country: String,
    val temp: Double,
    val condition: String,
    val icon: String
)

@Entity(tableName = "weather_alerts")
data class WeatherAlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cityName: String,
    val alertType: String, // e.g., "Alarm", "Notification"
    val alertDate: Long,   // Storing as timestamp
    val alertTime: String, // e.g., "08:00"
    val isEnabled: Boolean = true
)
