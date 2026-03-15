package com.example.weatherapp.di

import androidx.room.Room
import com.example.weatherapp.data.repository.WeatherRepository
import com.example.weatherapp.data.source.local.WeatherDatabase
import com.example.weatherapp.ui.home.HomeViewModel
import com.example.weatherapp.ui.lovedcities.LovedCitiesViewModel
import com.example.weatherapp.ui.alerts.AlertsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.weatherapp.data.source.remote.WeatherApiService
import com.example.weatherapp.data.source.remote.UnsplashApiService
import com.example.weatherapp.data.source.remote.WeatherRemoteDataSource
import com.example.weatherapp.data.source.remote.WeatherRemoteDataSourceImpl
import com.example.weatherapp.data.source.remote.LocationHelper
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named

val networkModule = module {
    single(named("WeatherRetrofit")) {
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    single(named("UnsplashRetrofit")) {
        Retrofit.Builder()
            .baseUrl("https://api.unsplash.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single { get<Retrofit>(named("WeatherRetrofit")).create(WeatherApiService::class.java) }
    single { get<Retrofit>(named("UnsplashRetrofit")).create(UnsplashApiService::class.java) }
}

val locationModule = module {
    single { LocationHelper(androidContext()) }
}

val repositoryModule = module {
    // WeatherRemoteDataSourceImpl now needs UnsplashApiService as well
    single<WeatherRemoteDataSource> { WeatherRemoteDataSourceImpl(get(), get()) }
    single { WeatherRepository(get()) }
}

val viewModelModule = module {
    viewModel { HomeViewModel(get(), get()) }
    viewModel { LovedCitiesViewModel(get(), get()) }
    viewModel { AlertsViewModel(get()) }
}

val databaseModule = module {
    single {
        Room.databaseBuilder(androidContext(), WeatherDatabase::class.java, "weazy_db")
            .fallbackToDestructiveMigration()
            .build()
    }
    single { get<WeatherDatabase>().weatherDao() }
}

val appModule = listOf(networkModule, locationModule, repositoryModule, viewModelModule, databaseModule)
