package com.example.weatherapp.data.repository

import com.example.weatherapp.data.source.remote.WeatherRemoteDataSource
import com.example.weatherapp.data.model.CurrentWeatherModel
import com.example.weatherapp.data.model.ForecastResponse
import com.example.weatherapp.data.model.GeocodingResponseItem
import com.example.weatherapp.data.model.UnsplashResponse
import retrofit2.Response

class WeatherRepository(private val remoteDataSource: WeatherRemoteDataSource) {

    suspend fun getCurrentWeatherByCoords(lat: Double, lon: Double): Response<CurrentWeatherModel> {
        return remoteDataSource.getCurrentWeatherByCoords(lat, lon)
    }

    suspend fun getForecastByCoords(lat: Double, lon: Double): Response<ForecastResponse> {
        return remoteDataSource.getForecastByCoords(lat, lon)
    }

    suspend fun getWeatherByCity(city: String): Response<CurrentWeatherModel> {
        return remoteDataSource.getCurrentWeatherByCity(city)
    }

    suspend fun getForecastByCity(city: String): Response<ForecastResponse>{
        return remoteDataSource.getForecastByCity(city)
    }

    suspend fun getCityImage(city: String): Response<UnsplashResponse> {
        return remoteDataSource.getCityImage(city)
    }

    suspend fun searchCity(query: String): Response<List<GeocodingResponseItem>> {
        return remoteDataSource.searchCity(query)
    }
}
