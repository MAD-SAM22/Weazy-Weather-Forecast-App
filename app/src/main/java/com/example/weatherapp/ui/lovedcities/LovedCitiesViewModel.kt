package com.example.weatherapp.ui.lovedcities

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.data.repository.WeatherRepository
import com.example.weatherapp.data.source.local.WeatherDao
import com.example.weatherapp.data.source.local.entity.FavoriteCityEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LovedCityUiModel(
    val id: String,
    val temp: Int,
    val high: Int,
    val low: Int,
    val name: String,
    val country: String,
    val condition: String,
    val iconAsset: String,
    val imageUrl: String? = null
)

class LovedCitiesViewModel(
    private val weatherDao: WeatherDao,
    private val repository: WeatherRepository
) : ViewModel() {

    private val _cityImages = MutableStateFlow<Map<String, String>>(emptyMap())

    init {
        // Monitor database changes and fetch images for new cities
        viewModelScope.launch {
            weatherDao.getFavoriteCities().collectLatest { entities ->
                entities.forEach { entity ->
                    if (!_cityImages.value.containsKey(entity.name)) {
                        fetchCityImage(entity.name)
                    }
                }
            }
        }
    }

    val lovedCities: StateFlow<List<LovedCityUiModel>> = combine(
        weatherDao.getFavoriteCities(),
        _cityImages
    ) { entities, images ->
        entities.map { entity ->
            LovedCityUiModel(
                id = entity.name,
                temp = entity.temp.toInt(),
                high = entity.temp.toInt() + 5,
                low = entity.temp.toInt() - 5,
                name = entity.name,
                country = entity.country,
                condition = entity.condition,
                iconAsset = entity.icon,
                imageUrl = images[entity.name]
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private fun fetchCityImage(cityName: String) {
        viewModelScope.launch {
            try {
                Log.d("LovedCitiesViewModel", "Fetching image for: $cityName")
                val response = repository.getCityImage(cityName)
                if (response.isSuccessful) {
                    val imageUrl = response.body()?.results?.firstOrNull()?.urls?.regular
                    Log.d("LovedCitiesViewModel", "Image URL for $cityName: $imageUrl")
                    if (imageUrl != null) {
                        _cityImages.update { it + (cityName to imageUrl) }
                    }
                } else {
                    Log.e("LovedCitiesViewModel", "Unsplash Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("LovedCitiesViewModel", "Exception fetching city image", e)
            }
        }
    }

    fun deleteCity(city: LovedCityUiModel) {
        viewModelScope.launch {
            val entity = FavoriteCityEntity(
                name = city.name,
                country = city.country,
                temp = city.temp.toDouble(),
                condition = city.condition,
                icon = city.iconAsset
            )
            weatherDao.deleteFavoriteCity(entity)
        }
    }
}
