package com.example.weatherapp.ui.navigation

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.weatherapp.ui.home.HomeScreen
import com.example.weatherapp.ui.home.HomeViewModel
import com.example.weatherapp.ui.onboarding.OnboardingScreen
import com.example.weatherapp.ui.onboarding.WeatherSplashScreen
import com.example.weatherapp.ui.lovedcities.LovedCitiesScreen
import com.example.weatherapp.ui.alerts.AlertsScreen
import com.example.weatherapp.ui.alerts.AlarmTriggerScreen
import org.koin.androidx.compose.koinViewModel

private const val PREFS_NAME = "weather_prefs"
private const val KEY_ONBOARDING_DONE = "onboarding_done"

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val LOVED_CITIES = "loved_cities"
    const val ALERTS = "alerts"
    const val ALARM_TRIGGER = "alarm_trigger/{city}/{message}"
    
    const val DEEP_LINK_BASE = "weazy://"
    const val ALERTS_DEEP_LINK = "${DEEP_LINK_BASE}alerts"
    const val ALARM_DEEP_LINK = "${DEEP_LINK_BASE}alarm/{city}/{message}"
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = koinViewModel()

    NavHost(navController = navController, startDestination = Routes.SPLASH) {

        composable(Routes.SPLASH) {
            WeatherSplashScreen {
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val done = prefs.getBoolean(KEY_ONBOARDING_DONE, false)
                val dest = if (done) Routes.HOME else Routes.ONBOARDING
                navController.navigate(dest) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            }
        }

        composable(Routes.ONBOARDING) {
            OnboardingScreen {
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit().putBoolean(KEY_ONBOARDING_DONE, true).apply()
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                }
            }
        }

        composable(Routes.HOME) {
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToCities = { navController.navigate(Routes.LOVED_CITIES) },
                onNavigateToAlerts = { navController.navigate(Routes.ALERTS) }
            )
        }

        composable(Routes.LOVED_CITIES) {
            LovedCitiesScreen(
                onBack = { navController.popBackStack() },
                onCitySelected = { city ->
                    homeViewModel.fetchWeatherData(city)
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.ALERTS,
            deepLinks = listOf(navDeepLink { uriPattern = Routes.ALERTS_DEEP_LINK })
        ) {
            AlertsScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.ALARM_TRIGGER,
            arguments = listOf(
                navArgument("city") { type = NavType.StringType },
                navArgument("message") { type = NavType.StringType }
            ),
            deepLinks = listOf(navDeepLink { uriPattern = Routes.ALARM_DEEP_LINK })
        ) { backStackEntry ->
            val city = backStackEntry.arguments?.getString("city") ?: ""
            val message = backStackEntry.arguments?.getString("message") ?: ""
            AlarmTriggerScreen(
                cityName = city,
                alertMessage = message,
                onStopAlarm = {
                    navController.navigate(Routes.ALERTS) {
                        popUpTo(Routes.HOME) // Clean up stack
                    }
                }
            )
        }
    }
}
