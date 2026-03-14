package com.example.weatherapp.ui.lovedcities

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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

// Custom shape for the weather card based on the UI design
val WeatherCardShape = GenericShape { size, _ ->
    val width = size.width
    val height = size.height
    
    // Starting at top left
    moveTo(0f, 40f) 
    
    // Top-left curve
    quadraticTo(0f, 0f, 40f, 0f)
    
    // Top line: starts at 0 height on left, ends a bit lower on right (around 10-20% height)
    lineTo(width * 0.7f, height * 0.15f)
    
    // Top-right curve
    quadraticTo(width, height * 0.25f, width, height * 0.5f)
    
    // Right side is half height
    lineTo(width, height - 40f)
    
    // Bottom-right curve
    quadraticTo(width, height, width - 40f, height)
    
    // Bottom line
    lineTo(40f, height)
    
    // Bottom-left curve
    quadraticTo(0f, height, 0f, height - 40f)
    
    close()
}

data class CityWeather(
    val id: String,
    val temp: Int,
    val high: Int,
    val low: Int,
    val name: String,
    val country: String,
    val condition: String,
    val iconAsset: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LovedCitiesScreen(onBack: () -> Unit) {
    var cities by remember {
        mutableStateOf(
            listOf(
                CityWeather("1", 19, 24, 18, "Montreal", "Canada", "Mid Rain", "icons/wind.png"),
                CityWeather("2", 20, 21, -19, "Toronto", "Canada", "Fast Wind", "icons/cloudy_sun.png"),
                CityWeather("3", 13, 16, 8, "Tokyo", "Japan", "Showers", "icons/bar2.png")
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF2E335A), Color(0xFF1C1B33))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Text(
                        text = "Weather",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
                
                IconButton(onClick = { }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Search Bar
            TextField(
                value = "",
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp)),
                placeholder = { Text("Search for a city or airport", color = Color.White.copy(alpha = 0.5f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.5f)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                    unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            // Cities List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(cities, key = { it.id }) { city ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            if (it == SwipeToDismissBoxValue.EndToStart) {
                                cities = cities.filter { it.id != city.id }
                                true
                            } else {
                                false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            val backgroundColor by animateColorAsState(
                                when (dismissState.targetValue) {
                                    SwipeToDismissBoxValue.EndToStart -> Color.Red.copy(alpha = 0.8f)
                                    else -> Color.Transparent
                                }, label = "color"
                            )
                            val scale by animateFloatAsState(
                                if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) 0.75f else 1f, label = "scale"
                            )

                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .clip(WeatherCardShape)
                                    .background(backgroundColor)
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    modifier = Modifier.scale(scale),
                                    tint = Color.White
                                )
                            }
                        },
                        enableDismissFromStartToEnd = false
                    ) {
                        WeatherCityCard(city)
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherCityCard(city: CityWeather) {
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
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF5936B4), Color(0xFF362A84)),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        )

        // Weather Image - Positioning it to overflow slightly like in the UI
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
