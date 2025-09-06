package com.tecvo.taxi.ui.base

import android.content.Context
import androidx.activity.compose.setContent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tecvo.taxi.MainActivity
import com.tecvo.taxi.navigation.AppNavigation
import com.tecvo.taxi.navigation.NavHostManager
import com.tecvo.taxi.services.AnalyticsManager
import com.tecvo.taxi.services.CrashReportingManager
import com.tecvo.taxi.ui.theme.TaxiTheme
import com.tecvo.taxi.repository.AuthRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.reset
import org.mockito.Mockito.mock
import javax.inject.Inject

/**
 * Simplified base class for UI tests with essential Hilt mocking.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
abstract class BaseUITest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    // Injected mocks from TestAuthModule
    @Inject lateinit var mockAuthRepository: AuthRepository
    @Inject lateinit var mockNavHostManager: NavHostManager
    
    // Local mocks for services not in DI
    lateinit var mockAnalyticsManager: AnalyticsManager
    lateinit var mockCrashReportingManager: CrashReportingManager

    protected val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    open fun setUp() {
        // Inject Hilt dependencies first
        hiltRule.inject()
        
        // Create local mocks
        mockAnalyticsManager = mock(AnalyticsManager::class.java)
        mockCrashReportingManager = mock(CrashReportingManager::class.java)
        
        // Reset all mocks to clean state
        resetAllMocks()
        
        // Configure default mock behaviors
        configureMockDefaults()
        
        // Allow derived classes to customize mocks
        configureMocksForTest()
        
        // Setup compose content with proper navigation
        setupComposeContent()
        
        // Wait for initial composition
        composeTestRule.waitForIdle()
    }
    
    /**
     * Reset all mocks to clean state
     */
    private fun resetAllMocks() {
        reset(mockAuthRepository)
        reset(mockNavHostManager)
    }
    
    /**
     * Configure default mock behaviors for all tests
     */
    private fun configureMockDefaults() {
        // Auth Repository - User is logged out by default
        runBlocking {
            `when`(mockAuthRepository.isUserLoggedIn()).thenReturn(false)
        }
        `when`(mockAuthRepository.getCurrentUserId()).thenReturn(null)
        
        // User Preferences will use real implementation from AppModule
    }
    
    /**
     * Override this to configure test-specific mock behaviors
     */
    protected open fun configureMocksForTest() {
        // Default: User is logged out for most tests
        // Override in specific test classes as needed
    }

    /**
     * Sets up the Compose content with proper navigation
     */
    protected open fun setupComposeContent() {
        composeTestRule.activity.setContent {
            TaxiTheme {
                val navController = rememberNavController()
                AppNavigation(
                    handlePostRegistrationPermissions = { _ -> },
                    analyticsManager = mockAnalyticsManager,
                    crashReportingManager = mockCrashReportingManager,
                    navHostManager = mockNavHostManager
                )
            }
        }
    }
    
    /**
     * Helper to simulate a logged-in user
     */
    protected fun simulateLoggedInUser(userId: String = "test_user_123") {
        runBlocking {
            `when`(mockAuthRepository.isUserLoggedIn()).thenReturn(true)
        }
        `when`(mockAuthRepository.getCurrentUserId()).thenReturn(userId)
    }
    
    /**
     * Helper to simulate a logged-out user
     */
    protected fun simulateLoggedOutUser() {
        runBlocking {
            `when`(mockAuthRepository.isUserLoggedIn()).thenReturn(false)
        }
        `when`(mockAuthRepository.getCurrentUserId()).thenReturn(null)
    }
    
    /**
     * Helper to set user role and destination
     * Note: UserPreferencesRepository is not mocked - uses real implementation
     */
    protected fun setUserPreferences(role: String? = null, destination: String? = null) {
        // This would need to be implemented differently since we're using real UserPreferencesRepository
        // For now, this is a placeholder
    }

    /**
     * Wait for compose to be idle and ready for testing
     */
    protected fun waitForCompose() {
        composeTestRule.waitForIdle()
    }

    /**
     * Simulate app going to background
     */
    protected fun simulateAppBackground() {
        composeTestRule.activityRule.scenario.moveToState(
            androidx.lifecycle.Lifecycle.State.STARTED
        )
    }

    /**
     * Simulate app returning to foreground
     */
    protected fun simulateAppForeground() {
        composeTestRule.activityRule.scenario.moveToState(
            androidx.lifecycle.Lifecycle.State.RESUMED
        )
    }

    /**
     * Helper to grant location permissions during test
     */
    protected fun grantLocationPermissions() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.uiAutomation.executeShellCommand(
            "pm grant ${context.packageName} android.permission.ACCESS_FINE_LOCATION"
        )
        instrumentation.uiAutomation.executeShellCommand(
            "pm grant ${context.packageName} android.permission.ACCESS_COARSE_LOCATION"
        )
    }

    /**
     * Helper to revoke location permissions during test
     */
    protected fun revokeLocationPermissions() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.uiAutomation.executeShellCommand(
            "pm revoke ${context.packageName} android.permission.ACCESS_FINE_LOCATION"
        )
        instrumentation.uiAutomation.executeShellCommand(
            "pm revoke ${context.packageName} android.permission.ACCESS_COARSE_LOCATION"
        )
    }
}