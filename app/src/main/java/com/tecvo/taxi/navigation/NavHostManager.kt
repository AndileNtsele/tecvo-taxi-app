// NavHostManager.kt
package com.tecvo.taxi.navigation

import com.tecvo.taxi.Routes
import androidx.navigation.NavController
import kotlinx.coroutines.runBlocking
import com.tecvo.taxi.repository.AuthRepository
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages navigation operations throughout the app with safe navigation patterns.
 * This class centralizes navigation logic for common flows like
 * login redirect, session handling, and screen transitions.
 * Uses safe navigation methods to prevent navigation stack warnings and crashes.
 */
@Singleton
open class NavHostManager @Inject constructor(
    private val authRepository: AuthRepository
) {
    private val tag = "NavHostManager"

    /**
     * Checks if a user is already logged in and navigates accordingly
     * Uses safe navigation to prevent navigation stack warnings
     */
    open suspend fun checkUserLoginStatus(navController: NavController) {
        try {
            val isLoggedIn = authRepository.isUserLoggedIn()
            if (isLoggedIn) {
                val userId = authRepository.getCurrentUserId()
                Timber.tag(tag)
                    .i("User Flow: Existing user $userId detected, skipping login")
                
                // Use safe navigation to prevent popUpTo warnings
                navController.safeNavigateToHome()
            } else {
                Timber.tag(tag).i("User Flow: No existing user, showing login screen")
                // User will stay on login screen (startDestination)
            }
        } catch (e: Exception) {
            Timber.tag(tag).e("Error checking user login status: ${e.message}")
            // If there's an error, let user stay on login screen
        }
    }

    /**
     * Safe navigation helper for common navigation patterns
     */
    fun navigateToHomeAfterLogin(navController: NavController) {
        try {
            Timber.tag(tag).i("Navigating to home after successful login")
            navController.safeNavigateToHome()
        } catch (e: Exception) {
            Timber.tag(tag).e("Error navigating to home after login: ${e.message}")
            // Fallback navigation
            try {
                navController.navigate(Routes.HOME)
            } catch (fallbackError: Exception) {
                Timber.tag(tag).e("Fallback navigation also failed: ${fallbackError.message}")
            }
        }
    }

    /**
     * Safely handles logout navigation
     */
    fun handleLogout(navController: NavController) {
        try {
            Timber.tag(tag).i("Handling user logout")
            // Clear back stack and navigate to login
            navController.safeNavigateAndClearStack(Routes.LOGIN)
        } catch (e: Exception) {
            Timber.tag(tag).e("Error handling logout: ${e.message}")
        }
    }
}