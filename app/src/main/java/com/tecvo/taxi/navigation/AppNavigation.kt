// AppNavigation.kt
package com.tecvo.taxi.navigation

import com.tecvo.taxi.Routes
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tecvo.taxi.PrivacyPolicyScreen
import com.tecvo.taxi.TermsAndConditionsScreen
import com.tecvo.taxi.screens.homescreens.HomeScreen
import com.tecvo.taxi.screens.loginscreens.LoginScreen
import com.tecvo.taxi.screens.mapscreens.DriverMapScreenLocal
import com.tecvo.taxi.screens.mapscreens.DriverMapScreenTown
import com.tecvo.taxi.screens.mapscreens.PassengerMapScreenLocal
import com.tecvo.taxi.screens.mapscreens.PassengerMapScreenTown
import com.tecvo.taxi.screens.rolescreen.DriverScreen
import com.tecvo.taxi.screens.rolescreen.PassengerScreen
import com.tecvo.taxi.screens.settingsscreen.SettingsScreen
import com.tecvo.taxi.services.AnalyticsManager
import com.tecvo.taxi.services.CrashReportingManager
import timber.log.Timber

/**
 * Custom function to set up the window UI appearance without using deprecated APIs
 */
private fun setupWindowUI(window: Window) {
    // Make the app edge-to-edge (content draws under system bars)
    WindowCompat.setDecorFitsSystemWindows(window, false)

    // Use modern approach through WindowCompat
    // This makes system bars transparent but keeps them visible
    WindowCompat.getInsetsController(window, window.decorView).apply {
        isAppearanceLightStatusBars = false
        isAppearanceLightNavigationBars = false
    }

    // Add flags to make navigation bar transparent and status bar dark
    // These are applied through WindowCompat to avoid deprecated methods
    // Set window flags for transparent navigation bar
    window.statusBarColor = android.graphics.Color.BLACK
    window.navigationBarColor = android.graphics.Color.TRANSPARENT
}

/**
 * AppNavigation sets up the navigation graph for the Taxi application.
 *
 * @param handlePostRegistrationPermissions Callback function to handle post-registration permission flow
 */
@Composable
fun AppNavigation(
    handlePostRegistrationPermissions: (NavController) -> Unit,
    analyticsManager: AnalyticsManager,
    crashReportingManager: CrashReportingManager,
    navHostManager: NavHostManager
) {
    val navController = rememberNavController()

    // For system UI configuration
    val view = LocalView.current
    val context = view.context as? android.app.Activity
    val window = context?.window

    // Track screen changes
    val currentBackStackEntry = navController.currentBackStackEntryAsState()

    // Keep basic screen tracking for crashlytics
    DisposableEffect(currentBackStackEntry.value) {
        val route = currentBackStackEntry.value?.destination?.route
        if (route != null) {
            val screenName = when (route) {
                Routes.LOGIN -> "Login Screen"
                Routes.HOME -> "Home Screen"
                Routes.DRIVER -> "Driver Role Screen"
                Routes.PASSENGER -> "Passenger Role Screen"
                Routes.DRIVER_MAP_TOWN -> "Driver Town Map Screen"
                Routes.DRIVER_MAP_LOCAL -> "Driver Local Map Screen"
                Routes.PASSENGER_MAP_TOWN -> "Passenger Town Map Screen"
                Routes.PASSENGER_MAP_LOCAL -> "Passenger Local Map Screen"
                Routes.SETTINGS -> "Settings Screen"
                Routes.TERMS_AND_CONDITIONS -> "Terms and Conditions Screen"
                Routes.PRIVACY_POLICY -> "Privacy Policy Screen"
                else -> "Unknown Screen"
            }

            // Keep only standard analytics
            analyticsManager.logScreenView(screenName, route)
            crashReportingManager.logScreen(screenName)
            Timber.tag("AppNavigation").i("Navigation: User at $screenName")
        }
        onDispose { }
    }

    // Set system UI colors without using deprecated APIs
    if (window != null) {
        SideEffect {
            setupWindowUI(window)
        }
    }

    // Check user login status when app launches
    LaunchedEffect(key1 = Unit) {
        Timber.tag("AppNavigation").i("Navigation: Checking user login status")
        navHostManager.checkUserLoginStatus(navController)
    }

    // Navigation structure
    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(navController, handlePostRegistrationPermissions)
        }
        composable(Routes.HOME) {
            HomeScreen(navController)
        }
        composable(Routes.PASSENGER) {
            PassengerScreen(navController)
        }
        composable(Routes.DRIVER) {
            DriverScreen(navController)
        }
        composable(Routes.TERMS_AND_CONDITIONS) {
            TermsAndConditionsScreen(navController)
        }
        composable(Routes.PRIVACY_POLICY) {
            PrivacyPolicyScreen(navController)
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(navController)
        }
        composable(Routes.PASSENGER_MAP_TOWN) {
            PassengerMapScreenTown(navController)
        }
        composable(Routes.PASSENGER_MAP_LOCAL) {
            PassengerMapScreenLocal(navController)
        }
        composable(Routes.DRIVER_MAP_TOWN) {
            DriverMapScreenTown(navController)
        }
        composable(Routes.DRIVER_MAP_LOCAL) {
            DriverMapScreenLocal(navController)
        }

    }
}