package com.example.weatherapp.data.source.remote

import com.example.weatherapp.data.model.UnsplashResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface UnsplashApiService {
    @GET("search/photos")
    suspend fun searchPhotos(
        @Query("query") query: String,
        @Query("client_id") clientId: String,
        @Query("page") page: Int = 1,
        @Query("orientation") orientation: String = "landscape"
    ): Response<UnsplashResponse>
}
