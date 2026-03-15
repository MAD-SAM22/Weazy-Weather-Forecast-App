package com.example.weatherapp.data.source.remote

import com.example.weatherapp.data.model.CurrentWeatherModel
import com.example.weatherapp.data.model.ForecastResponse
import com.example.weatherapp.data.model.UnsplashResponse
import retrofit2.Response

interface WeatherRemoteDataSource {
    suspend fun getCurrentWeatherByCoords(lat: Double, lon: Double): Response<CurrentWeatherModel>
    suspend fun getCurrentWeatherByCity(city: String): Response<CurrentWeatherModel>
    suspend fun getForecastByCoords(lat: Double, lon: Double): Response<ForecastResponse>
    suspend fun getForecastByCity(city: String): Response<ForecastResponse>
    suspend fun getCityImage(city: String): Response<UnsplashResponse>
}

class WeatherRemoteDataSourceImpl(
    private val apiService: WeatherApiService,
    private val unsplashApiService: UnsplashApiService
) : WeatherRemoteDataSource {

    private val API_KEY = "037a373011c520cc888756c98e9d9260"
    private val UNSPLASH_CLIENT_ID = "t365ol8lMra4ralBTzFhll1mTkpwNnH4WHa_TGE0Rxk"

    override suspend fun getCurrentWeatherByCoords(lat: Double, lon: Double): Response<CurrentWeatherModel> {
        return apiService.getCurrentWeather(lat = lat, lon = lon, apiKey = API_KEY)
    }

    override suspend fun getCurrentWeatherByCity(city: String): Response<CurrentWeatherModel> {
        return apiService.getCurrentWeather(cityName = city, apiKey = API_KEY)
    }

    override suspend fun getForecastByCoords(lat: Double, lon: Double): Response<ForecastResponse> {
        return apiService.getFiveDayForecast(lat = lat, lon = lon, apiKey = API_KEY)
    }

    override suspend fun getForecastByCity(city: String): Response<ForecastResponse> {
        return apiService.getFiveDayForecast(cityName = city, apiKey = API_KEY)
    }

    override suspend fun getCityImage(city: String): Response<UnsplashResponse> {
        return unsplashApiService.searchPhotos(query = "$city cityscape", clientId = UNSPLASH_CLIENT_ID)
    }
}
