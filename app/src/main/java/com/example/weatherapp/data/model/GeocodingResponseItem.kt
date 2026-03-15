package com.example.weatherapp.data.model

data class GeocodingResponseItem(
    val name: String,
    val local_names: Map<String, String>?,
    val lat: Double,
    val lon: Double,
    val country: String,
    val state: String?
)
