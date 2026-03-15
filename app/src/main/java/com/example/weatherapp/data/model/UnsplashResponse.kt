package com.example.weatherapp.data.model

import com.google.gson.annotations.SerializedName

data class UnsplashResponse(
    val results: List<UnsplashPhoto>
)

data class UnsplashPhoto(
    val urls: PhotoUrls
)

data class PhotoUrls(
    val regular: String,
    val small: String
)
