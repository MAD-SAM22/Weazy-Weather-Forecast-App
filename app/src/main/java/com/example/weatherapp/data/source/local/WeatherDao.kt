package com.example.weatherapp.data.source.local

import androidx.room.*
import com.example.weatherapp.data.source.local.entity.CurrentWeatherEntity
import com.example.weatherapp.data.source.local.entity.FavoriteCityEntity
import com.example.weatherapp.data.source.local.entity.WeatherAlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    // Current Weather
    @Query("SELECT * FROM current_weather WHERE id = 1")
    fun getCurrentWeather(): Flow<CurrentWeatherEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrentWeather(weather: CurrentWeatherEntity)

    // Favorite Cities
    @Query("SELECT * FROM favorite_cities")
    fun getFavoriteCities(): Flow<List<FavoriteCityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteCity(city: FavoriteCityEntity)

    @Delete
    suspend fun deleteFavoriteCity(city: FavoriteCityEntity)

    // Weather Alerts
    @Query("SELECT * FROM weather_alerts")
    fun getAllAlerts(): Flow<List<WeatherAlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: WeatherAlertEntity): Long

    @Delete
    suspend fun deleteAlert(alert: WeatherAlertEntity)

    @Query("DELETE FROM weather_alerts WHERE cityName = :cityName")
    suspend fun deleteAlertByCity(cityName: String)

    @Query("UPDATE weather_alerts SET isEnabled = :enabled WHERE id = :alertId")
    suspend fun updateAlertStatus(alertId: Int, enabled: Boolean)
}
