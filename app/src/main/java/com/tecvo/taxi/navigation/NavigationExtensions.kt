// NavigationExtensions.kt
package com.tecvo.taxi.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import com.tecvo.taxi.Routes
import timber.log.Timber

/**
 * Safe navigation extensions to prevent navigation stack issues and warnings.
 * These extensions check navigation state before performing operations to avoid crashes.
 */

private const val TAG = "SafeNavigation"

/**
 * Safely navigates to a destination, checking if the navigation controller is still valid
 */
fun NavController.safeNavigate(
    route: String,
    builder: NavOptionsBuilder.() -> Unit = {}
) {
    try {
        navigate(route, builder)
        Timber.tag(TAG).d("Safe navigation to $route successful")
    } catch (e: Exception) {
        Timber.tag(TAG).e("Safe navigation to $route failed: ${e.message}")
    }
}

/**
 * Safely pops back stack to a specific route, checking if the route exists first
 */
fun NavController.safePopUpTo(
    route: String,
    inclusive: Boolean = false
): Boolean {
    return try {
        // Check if the route exists in current back stack
        val backStack = currentBackStack.value
        val hasRoute = backStack.any { it.destination.route == route }
        
        if (hasRoute) {
            popBackStack(route, inclusive)
            Timber.tag(TAG).d("Safe popUpTo $route successful")
            true
        } else {
            Timber.tag(TAG).w("Route $route not found in back stack, skipping popUpTo")
            false
        }
    } catch (e: Exception) {
        Timber.tag(TAG).e("Safe popUpTo $route failed: ${e.message}")
        false
    }
}

/**
 * Safely navigates with popUpTo, checking if the popUpTo route exists
 */
fun NavController.safeNavigateWithPopUpTo(
    destination: String,
    popUpToRoute: String,
    inclusive: Boolean = false
) {
    try {
        // Check if popUpTo route exists in back stack
        val backStack = currentBackStack.value
        val hasPopUpToRoute = backStack.any { it.destination.route == popUpToRoute }
        
        if (hasPopUpToRoute) {
            navigate(destination) {
                popUpTo(popUpToRoute) { this.inclusive = inclusive }
            }
            Timber.tag(TAG).d("Safe navigate to $destination with popUpTo $popUpToRoute successful")
        } else {
            // If popUpTo route doesn't exist, just navigate normally
            navigate(destination)
            Timber.tag(TAG).d("Safe navigate to $destination (popUpTo route not found, using simple navigation)")
        }
    } catch (e: Exception) {
        Timber.tag(TAG).e("Safe navigate with popUpTo failed: ${e.message}")
        // Fallback: try simple navigation
        try {
            navigate(destination)
            Timber.tag(TAG).d("Fallback navigation to $destination successful")
        } catch (fallbackError: Exception) {
            Timber.tag(TAG).e("Fallback navigation also failed: ${fallbackError.message}")
        }
    }
}

/**
 * Safely clears the back stack and navigates to a new destination
 */
fun NavController.safeNavigateAndClearStack(destination: String) {
    try {
        navigate(destination) {
            // Clear the entire back stack
            popUpTo(0) { inclusive = true }
        }
        Timber.tag(TAG).d("Safe navigate and clear stack to $destination successful")
    } catch (e: Exception) {
        Timber.tag(TAG).e("Safe navigate and clear stack failed: ${e.message}")
        // Fallback: try simple navigation
        try {
            navigate(destination)
            Timber.tag(TAG).d("Fallback navigation to $destination successful")
        } catch (fallbackError: Exception) {
            Timber.tag(TAG).e("Fallback navigation also failed: ${fallbackError.message}")
        }
    }
}

/**
 * Checks if a specific route exists in the current back stack
 */
fun NavController.hasRouteInBackStack(route: String): Boolean {
    return try {
        val backStack = currentBackStack.value
        backStack.any { it.destination.route == route }
    } catch (e: Exception) {
        Timber.tag(TAG).e("Error checking back stack for route $route: ${e.message}")
        false
    }
}

/**
 * Gets all routes currently in the back stack (for debugging)
 */
fun NavController.getBackStackRoutes(): List<String> {
    return try {
        currentBackStack.value.mapNotNull { it.destination.route }
    } catch (e: Exception) {
        Timber.tag(TAG).e("Error getting back stack routes: ${e.message}")
        emptyList()
    }
}

/**
 * Safe navigation for home with intelligent route management
 */
fun NavController.safeNavigateToHome() {
    try {
        val backStackRoutes = getBackStackRoutes()
        Timber.tag(TAG).d("Current back stack routes: $backStackRoutes")
        
        when {
            // If login route exists, pop up to it
            hasRouteInBackStack(Routes.LOGIN) -> {
                safeNavigateWithPopUpTo(Routes.HOME, Routes.LOGIN, inclusive = true)
            }
            // If we're already at home, don't navigate
            currentDestination?.route == Routes.HOME -> {
                Timber.tag(TAG).d("Already at home, skipping navigation")
            }
            // Otherwise, just navigate to home
            else -> {
                safeNavigate(Routes.HOME)
            }
        }
    } catch (e: Exception) {
        Timber.tag(TAG).e("Safe navigate to home failed: ${e.message}")
        // Last resort fallback
        try {
            navigate(Routes.HOME)
        } catch (fallbackError: Exception) {
            Timber.tag(TAG).e("Emergency fallback navigation to home failed: ${fallbackError.message}")
        }
    }
}