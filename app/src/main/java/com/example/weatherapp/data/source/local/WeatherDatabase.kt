package com.example.weatherapp.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.weatherapp.data.source.local.entity.CurrentWeatherEntity
import com.example.weatherapp.data.source.local.entity.FavoriteCityEntity
import com.example.weatherapp.data.source.local.entity.WeatherAlertEntity

@Database(
    entities = [
        CurrentWeatherEntity::class,
        FavoriteCityEntity::class,
        WeatherAlertEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(WeatherTypeConverters::class)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao
}
