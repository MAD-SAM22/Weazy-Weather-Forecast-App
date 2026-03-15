package com.example.weatherapp.ui.utils

import java.util.Calendar

data class WeatherTheme(
    val bgImage: String,
    val houseImage: String,
    val iconImage: String
)

object WeatherMapper {
    fun getTheme(condition: String?, iconCode: String?, timestamp: Long? = null): WeatherTheme {
        val calendar = Calendar.getInstance()
        if (timestamp != null) {
            calendar.timeInMillis = timestamp * 1000
        }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val isNight = iconCode?.endsWith("n") == true || hour !in 6..18

        return when {
            // Thunderstorm
            iconCode?.startsWith("11") == true || condition?.contains("thunderstorm", ignoreCase = true) == true -> {
                WeatherTheme(
                    bgImage = "bg/storm.jpg",
                    houseImage = "houses/rain.png",
                    iconImage = "icons/storm.png"
                )
            }
            // Rain / Drizzle
            iconCode?.startsWith("09") == true || iconCode?.startsWith("10") == true || 
            condition?.contains("rain", ignoreCase = true) == true || condition?.contains("drizzle", ignoreCase = true) == true -> {
                WeatherTheme(
                    bgImage = if (isNight) "bg/rain2.jpg" else "bg/rain1.jpg",
                    houseImage = "houses/rain.png",
                    iconImage = "icons/wind_rain.png"
                )
            }
            // Snow
            iconCode?.startsWith("13") == true || condition?.contains("snow", ignoreCase = true) == true -> {
                WeatherTheme(
                    bgImage = if (isNight) "bg/star2.jpg" else "bg/star.jpg",
                    houseImage = "houses/snow.png",
                    iconImage = "icons/snaw.png"
                )
            }
            // Atmosphere (Mist, Smoke, etc.)
            iconCode?.startsWith("50") == true -> {
                WeatherTheme(
                    bgImage = "bg/storm_bar2.jpg",
                    houseImage = if (isNight) "houses/morning_or_night.png" else "houses/morning2.png",
                    iconImage = "icons/wind.png"
                )
            }
            // Clear sky or Clouds (General visibility based on time)
            else -> {
                when {
                    hour in 6..10 -> { // Morning
                        WeatherTheme(
                            bgImage = "bg/morning.jpg",
                            houseImage = "houses/morning.png",
                            iconImage = if (iconCode?.startsWith("01") == true) "icons/sun.png" else "icons/cloudy_sun.png"
                        )
                    }
                    hour in 11..16 -> { // Midday
                        WeatherTheme(
                            bgImage = "bg/midday.jpg",
                            houseImage = "houses/morning2.png",
                            iconImage = if (iconCode?.startsWith("01") == true) "icons/sun.png" else "icons/cloudy_sun.png"
                        )
                    }
                    hour in 17..18 -> { // Late afternoon / Sunset
                        WeatherTheme(
                            bgImage = "bg/morning2.jpg",
                            houseImage = "houses/morning3.png",
                            iconImage = if (iconCode?.startsWith("01") == true) "icons/sun.png" else "icons/cloudy_sun.png"
                        )
                    }
                    else -> { // Night
                        WeatherTheme(
                            bgImage = if (hour in 19..22) "bg/night.jpg" else "bg/night2.jpg",
                            houseImage = "houses/night.png",
                            iconImage = if (iconCode?.startsWith("01") == true) "icons/full_moon.png" else "icons/scoudy_night.png"
                        )
                    }
                }
            }
        }
    }
}
