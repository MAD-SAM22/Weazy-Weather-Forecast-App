package com.example.weatherapp.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.weatherapp.ui.theme.PrimaryPurple
import com.example.weatherapp.ui.theme.ForecastCardTop
import com.example.weatherapp.ui.theme.ForecastCardBottom
import com.example.weatherapp.ui.theme.OnboardingCardBg
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.collections.listOf
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
// ─── Data ───────────────────────────────────────────────────────────────────

data class OnboardingData(
    val title: String,
    val description: String,
    val tag: String,
    val assetPath: String,
    val accentColor: Color = PrimaryPurple
)

private val pages = listOf(
    OnboardingData(
        title = "Know the Weather\nAnywhere, Anytime",
        description = "Real-time weather for any city on Earth. Rain, sun or snow — stay one step ahead wherever you go.",
        tag = "Weather",
        assetPath = "onboarding/multi_weather.png"
    ),
    OnboardingData(
        title = "Set Alerts &\nMorning Notifications",
        description = "Custom weather alerts and a daily morning forecast delivered right to you. Never be caught off guard again.",
        tag = "Notifications",
        assetPath = "onboarding/allert.png"
    ),
    OnboardingData(
        title = "Add Cities to\nYour Favourites",
        description = "Save the cities you love. Switch between them instantly and plan ahead with beautiful per-city weather cards.",
        tag = "Favourites",
        assetPath = "onboarding/location_onboarding.png"
    )
)

// ─── Main Screen ─────────────────────────────────────────────────────────────

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val currentPage = pagerState.currentPage

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Background consistent with Home theme ───────────────────
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data("file:///android_asset/bg/star.jpg")
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Depth overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF2E335A).copy(alpha = 0.4f))
        )

        // ── Pages ───────────────────────────────────────────────────
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { index ->
            OnboardingPageContent(
                data = pages[index],
                pageIndex = index
            )
        }

        // ── Bottom controls ─────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 52.dp, start = 32.dp, end = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dot indicators matching premium style
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                pages.indices.forEach { i ->
                    val selected = i == currentPage
                    val width by animateDpAsState(
                        targetValue = if (selected) 24.dp else 8.dp,
                        animationSpec = spring(Spring.DampingRatioMediumBouncy),
                        label = "dot"
                    )
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(
                                if (selected) Color.White
                                else Color.White.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Primary action button — Circular "Add" style from Home
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(2.dp)
                    .background(
                        brush = Brush.linearGradient(listOf(Color.White, Color(0xFFE0E0E0))),
                        shape = CircleShape
                    )
                    .clickable {
                        if (currentPage < pages.size - 1) {
                            scope.launch { pagerState.animateScrollToPage(currentPage + 1) }
                        } else {
                            onFinished()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (currentPage < pages.size - 1) "→" else "✓",
                        color = Color(0xFF48319D),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Skip
            if (currentPage < pages.size - 1) {
                Text(
                    text = "Skip",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onFinished() }
                )
            }
        }
    }
}

// ─── Single page content ───────────────────────────────────────────────────

@Composable
fun OnboardingPageContent(
    data: OnboardingData,
    pageIndex: Int
) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.fillMaxHeight(0.12f))

        // ── High Quality Illustration ──────────────────────────────
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data("file:///android_asset/${data.assetPath}")
                .build(),
            contentDescription = null,
            modifier = Modifier
                .height(280.dp)
                .fillMaxWidth(),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(40.dp))

        // ── Glass Text Card - Matching Forecast Section Gradient ────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(44.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(ForecastCardTop.copy(alpha = 0.8f), ForecastCardBottom)
                    )
                )
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Accent tag pill
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = data.tag.uppercase(),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = data.title,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = data.description,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        }
    }
}
