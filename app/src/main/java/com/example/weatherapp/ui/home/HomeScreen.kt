package com.example.weatherapp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.weatherapp.ui.home.components.AddCityDialog
import com.example.weatherapp.ui.home.components.ForecastCard
import com.example.weatherapp.ui.home.components.MapSelectionDialog
import com.example.weatherapp.ui.utils.WeatherMapper
import org.osmdroid.util.GeoPoint

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToCities: () -> Unit,
    onNavigateToAlerts: () -> Unit
) {
    val context = LocalContext.current
    val weather = viewModel.weatherState
    val isHourly = viewModel.isHourlySelected
    val forecastData = if (isHourly) viewModel.hourlyForecast else viewModel.weeklyForecast
    
    var showAddCityDialog by remember { mutableStateOf(false) }
    var showMapDialog by remember { mutableStateOf(false) }

    // Map weather state to theme
    val theme = WeatherMapper.getTheme(
        condition = weather?.weather?.firstOrNull()?.main,
        iconCode = weather?.weather?.firstOrNull()?.icon
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image mapped from Weather state
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data("file:///android_asset/${theme.bgImage}")
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            Text(
                text = weather?.name ?: "Loading...",
                fontSize = 34.sp,
                color = Color.White,
                fontWeight = FontWeight.Normal
            )

            Text(
                text = if (weather != null) "${weather.main.temp.toInt()}°" else "--°",
                fontSize = 96.sp,
                color = Color.White,
                fontWeight = FontWeight.Thin
            )

            Text(
                text = weather?.weather?.firstOrNull()?.main ?: "",
                fontSize = 20.sp,
                color = Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.SemiBold
            )

            if (weather != null) {
                Text(
                    text = "H:${weather.main.temp_max.toInt()}°  L:${weather.main.temp_min.toInt()}°",
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // House Image mapped from Weather state
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data("file:///android_asset/${theme.houseImage}")
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .height(320.dp)
                    .padding(top = 15.dp),
                contentScale = ContentScale.FillBounds
            )
            
            Spacer(modifier = Modifier.weight(1f))
        }

        // Forecast Section
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(340.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF3E3C6E).copy(alpha = 0.8f), Color(0xFF2E335A))
                    ),
                    shape = RoundedCornerShape(topStart = 44.dp, topEnd = 44.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .width(48.dp)
                    .height(5.dp)
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    .align(Alignment.TopCenter)
            )

            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 25.dp, start = 32.dp, end = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Hourly Forecast",
                        color = if (isHourly) Color.White else Color.White.copy(alpha = 0.6f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { viewModel.toggleForecastType(true) }
                    )
                    Text(
                        text = "Weekly Forecast",
                        color = if (!isHourly) Color.White else Color.White.copy(alpha = 0.6f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { viewModel.toggleForecastType(false) }
                    )
                }
                
                HorizontalDivider(
                    modifier = Modifier.padding(top = 12.dp),
                    thickness = 0.5.dp,
                    color = Color.White.copy(alpha = 0.2f)
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(forecastData) { item ->
                        ForecastCard(item)
                    }
                }
            }
        }

        // Bottom Navigation Bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(100.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateToAlerts) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
                
                Box(
                    modifier = Modifier
                        .offset(y = (-20).dp)
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(2.dp)
                        .background(
                            brush = Brush.linearGradient(listOf(Color.White, Color(0xFFE0E0E0))),
                            shape = CircleShape
                        )
                        .clickable { showAddCityDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                       modifier = Modifier
                           .size(56.dp)
                           .clip(CircleShape)
                           .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF48319D), modifier = Modifier.size(32.dp))
                    }
                }

                IconButton(onClick = onNavigateToCities) {
                    Icon(Icons.Default.List, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
        }

        if (showAddCityDialog) {
            AddCityDialog(
                onDismiss = { showAddCityDialog = false },
                onSearch = { viewModel.searchCities(it) },
                searchResults = viewModel.searchResults,
                onCitySelected = { 
                    viewModel.addCityToFavorites(it)
                    showAddCityDialog = false
                },
                onSelectOnMap = {
                    showAddCityDialog = false
                    showMapDialog = true
                }
            )
        }

        if (showMapDialog) {
            MapSelectionDialog(
                initialLocation = GeoPoint(30.0444, 31.2357), // Using GeoPoint
                onDismiss = { showMapDialog = false },
                onLocationConfirmed = { geoPoint ->
                    viewModel.addLocationToFavorites(geoPoint.latitude, geoPoint.longitude)
                    showMapDialog = false
                }
            )
        }
    }
}
