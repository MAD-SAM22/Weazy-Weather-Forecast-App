package com.example.weatherapp.ui.lovedcities.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.weatherapp.ui.lovedcities.LovedCityUiModel
import com.example.weatherapp.ui.lovedcities.WeatherCardShape

@Composable
fun WeatherCityCard(city: LovedCityUiModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(184.dp)
    ) {
        // Background card with custom shape
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clip(WeatherCardShape)
        ) {
            if (city.imageUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(city.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Overlay to ensure text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF5936B4), Color(0xFF362A84)),
                                start = Offset(0f, 0f),
                                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                            )
                        )
                )
            }
        }

        // Weather Image
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("file:///android_asset/${city.iconAsset}")
                .build(),
            contentDescription = null,
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-10).dp, y = (-20).dp),
            contentScale = ContentScale.Fit
        )

        // Temperature and High/Low
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 20.dp, top = 20.dp)
        ) {
            Text(
                text = "${city.temp}°",
                color = Color.White,
                fontSize = 64.sp,
                fontWeight = FontWeight.Normal
            )
            
            Text(
                text = "H:${city.high}°  L:${city.low}°",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp
            )
        }

        // City and Condition
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "${city.name}, ${city.country}",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = city.condition,
                color = Color.White,
                fontSize = 13.sp
            )
        }
    }
}
