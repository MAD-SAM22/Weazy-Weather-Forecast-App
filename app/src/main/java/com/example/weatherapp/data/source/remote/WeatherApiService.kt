package com.example.weatherapp.data.source.remote

import com.example.weatherapp.data.model.CurrentWeatherModel
import com.example.weatherapp.data.model.ForecastResponse
import com.example.weatherapp.data.model.GeocodingResponseItem
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

// unites of messure : standard, metric, or imperial
//units=metric&lang=ar


//1. Current Weather by Coordinates
//https://api.openweathermap.org/data/2.5/weather?lat=51.5085&lon=-0.1257&appid=037a373011c520cc888756c98e9d9260&units=metric


//2. 5-Day Forecast by Coordinates
//https://api.openweathermap.org/data/2.5/forecast?lat=51.5085&lon=-0.1257&appid=037a373011c520cc888756c98e9d9260&units=metric

//3. weather by city
//https://api.openweathermap.org/data/2.5/weather?q=London&appid=037a373011c520cc888756c98e9d9260&units=metric

//4. 5-Day Forecast by city
//https://api.openweathermap.org/data/2.5/forecast?q=London&appid=037a373011c520cc888756c98e9d9260&units=metric

interface WeatherApiService {

    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double? = null,
        @Query("lon") lon: Double? = null,
        @Query("q") cityName: String? = null,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "en"
    ): Response<CurrentWeatherModel>

    @GET("forecast")
    suspend fun getFiveDayForecast(
        @Query("lat") lat: Double? = null,
        @Query("lon") lon: Double? = null,
        @Query("q") cityName: String? = null,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "en"
    ): Response<ForecastResponse>

    @GET("https://api.openweathermap.org/geo/1.0/direct")
    suspend fun searchCity(
        @Query("q") cityName: String,
        @Query("limit") limit: Int = 5,
        @Query("appid") apiKey: String
    ): Response<List<GeocodingResponseItem>>
}
