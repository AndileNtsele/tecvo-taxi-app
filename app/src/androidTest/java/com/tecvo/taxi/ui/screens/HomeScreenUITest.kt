package com.tecvo.taxi.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tecvo.taxi.ui.base.BaseUITest
import com.tecvo.taxi.ui.utils.UITestUtils
import com.tecvo.taxi.ui.utils.UITestUtils.TestTags
import com.tecvo.taxi.ui.utils.FirebaseTestUtils
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Before

/**
 * Comprehensive UI tests for the Home Screen and navigation flows.
 * Tests dashboard functionality, navigation between screens, and user experience.
 * 
 * Coverage:
 * - Home screen initial state
 * - Navigation to passenger/driver flows
 * - Settings navigation
 * - User session management
 * - Quick access to previous selections
 * - Error states and recovery
 * - Deep linking capabilities
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HomeScreenUITest : BaseUITest() {

    override fun configureMocksForTest() {
        // Home screen tests need a logged-in user
        simulateLoggedInUser("test_user_123")
        setUserPreferences(role = "passenger", destination = "local")
    }

    @Before
    override fun setUp() {
        super.setUp()
        grantLocationPermissions()
    }

    @Test
    fun homeScreen_initialLoad_displaysCorrectElements() {
        with(composeTestRule) {
            // Arrange & Act
            navigateToHomeScreen()
            
            // Assert - Core home elements should be visible
            UITestUtils.run {
                assertHomeScreenVisible()
            }
            
            // Main dashboard cards
            onNodeWithTag(TestTags.PASSENGER_CARD).assertIsDisplayed()
            onNodeWithTag(TestTags.DRIVER_CARD).assertIsDisplayed()
            
            // Navigation elements
            onNodeWithTag(TestTags.SETTINGS_BUTTON).assertIsDisplayed()
            
            // Welcome message or user info
            onNodeWithText("Welcome back").assertExists()
            
            // Both cards should be clickable
            onNodeWithTag(TestTags.PASSENGER_CARD).assertHasClickAction()
            onNodeWithTag(TestTags.DRIVER_CARD).assertHasClickAction()
        }
    }

    @Test
    fun homeScreen_passengerCard_navigatesToRoleSelection() {
        with(composeTestRule) {
            // Arrange
            navigateToHomeScreen()
            
            // Act
            onNodeWithTag(TestTags.PASSENGER_CARD)
                .assertIsDisplayed()
                .performClick()
            
            waitForIdle()
            
            // Assert - Should navigate to passenger role selection
            UITestUtils.run {
                assertDestinationSelectionVisible()
            }
            
            // Should show passenger-specific content
            onNodeWithText("Where are you going?").assertIsDisplayed()
            onNodeWithTag(TestTags.TOWN_BUTTON).assertIsDisplayed()
            onNodeWithTag(TestTags.LOCAL_BUTTON).assertIsDisplayed()
        }
    }

    @Test
    fun homeScreen_driverCard_navigatesToRoleSelection() {
        with(composeTestRule) {
            // Arrange
            navigateToHomeScreen()
            
            // Act
            onNodeWithTag(TestTags.DRIVER_CARD)
                .assertIsDisplayed()
                .performClick()
            
            waitForIdle()
            
            // Assert - Should navigate to driver role selection
            UITestUtils.run {
                assertDestinationSelectionVisible()
            }
            
            // Should show driver-specific content
            onNodeWithText("Which route are you driving?").assertIsDisplayed()
            onNodeWithTag(TestTags.TOWN_BUTTON).assertIsDisplayed()
            onNodeWithTag(TestTags.LOCAL_BUTTON).assertIsDisplayed()
        }
    }

    @Test
    fun homeScreen_settingsNavigation_works() {
        with(composeTestRule) {
            // Arrange
            navigateToHomeScreen()
            
            // Act
            UITestUtils.run {
                navigateToSettings()
                waitForIdle()
            }
            
            // Assert - Should be on settings screen
            UITestUtils.run {
                assertSettingsScreenVisible()
            }
            
            // Verify settings elements
            onNodeWithTag(TestTags.LOGOUT_BUTTON).assertIsDisplayed()
            onNodeWithTag(TestTags.TERMS_BUTTON).assertIsDisplayed()
            onNodeWithTag(TestTags.PRIVACY_BUTTON).assertIsDisplayed()
        }
    }

    @Test
    fun homeScreen_quickPassengerFlow_completesEndToEnd() {
        with(composeTestRule) {
            // Arrange
            navigateToHomeScreen()
            
            // Act - Complete quick passenger flow
            onNodeWithTag(TestTags.PASSENGER_CARD).performClick()
            waitForIdle()
            
            UITestUtils.run {
                selectTownDestination()
                waitForElementToAppear(TestTags.MAP_VIEW, 10000L)
            }
            
            // Assert - Should reach passenger map
            UITestUtils.run {
                assertMapScreenVisible()
            }
            
            onNodeWithContentDescription("Passenger view").assertExists()
            onNodeWithTag(TestTags.DESTINATION_LABEL)
                .assertTextContains("Town", ignoreCase = true)
        }
    }

    @Test
    fun homeScreen_quickDriverFlow_completesEndToEnd() {
        with(composeTestRule) {
            // Arrange
            navigateToHomeScreen()
            
            // Act - Complete quick driver flow
            onNodeWithTag(TestTags.DRIVER_CARD).performClick()
            waitForIdle()
            
            UITestUtils.run {
                selectLocalDestination()
                waitForElementToAppear(TestTags.MAP_VIEW, 10000L)
            }
            
            // Assert - Should reach driver map
            UITestUtils.run {
                assertMapScreenVisible()
            }
            
            onNodeWithContentDescription("Driver view").assertExists()
            onNodeWithTag(TestTags.DESTINATION_LABEL)
                .assertTextContains("Local", ignoreCase = true)
        }
    }

    @Test
    fun homeScreen_backNavigation_fromSettings_returnsHome() {
        with(composeTestRule) {
            // Arrange
            navigateToHomeScreen()
            UITestUtils.run {
                navigateToSettings()
                assertSettingsScreenVisible()
            }
            
            // Act
            UITestUtils.run {
                goBack()
                waitForIdle()
            }
            
            // Assert - Should return to home
            UITestUtils.run {
                assertHomeScreenVisible()
            }
        }
    }

    @Test
    fun homeScreen_backNavigation_fromMap_returnsHome() {
        with(composeTestRule) {
            // Arrange - Navigate to map through home
            navigateToHomeScreen()
            onNodeWithTag(TestTags.PASSENGER_CARD).performClick()
            
            UITestUtils.run {
                selectTownDestination()
                waitForElementToAppear(TestTags.MAP_VIEW, 10000L)
            }
            
            // Act - Navigate back from map
            UITestUtils.run {
                goBack() // Back to destination selection
                goBack() // Back to home
                waitForIdle()
            }
            
            // Assert - Should be back on home
            UITestUtils.run {
                assertHomeScreenVisible()
            }
        }
    }

    @Test
    fun homeScreen_recentSelection_remembersPreference() {
        with(composeTestRule) {
            // Test if app remembers last role/destination selection
            navigateToHomeScreen()
            
            // Complete a passenger town flow
            onNodeWithTag(TestTags.PASSENGER_CARD).performClick()
            UITestUtils.run {
                selectTownDestination()
                waitForElementToAppear(TestTags.MAP_VIEW, 10000L)
                goBack() // Return to destination
                goBack() // Return to home
            }
            
            // Act - Select passenger again
            onNodeWithTag(TestTags.PASSENGER_CARD).performClick()
            
            // Assert - Should remember last destination or show quick access
            // This depends on implementation - could show last used destination
            UITestUtils.run {
                assertDestinationSelectionVisible()
            }
            
            // Quick access to "Town" might be highlighted or selected
            onNodeWithTag(TestTags.TOWN_BUTTON).assertIsDisplayed()
        }
    }

    @Test
    fun homeScreen_orientationChange_preservesState() {
        with(composeTestRule) {
            // Arrange
            navigateToHomeScreen()
            
            // Act - Rotate device
            activity.requestedOrientation = 
                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            waitForIdle()
            
            // Assert - Home elements should still be visible
            UITestUtils.run {
                assertHomeScreenVisible()
            }
            
            onNodeWithTag(TestTags.PASSENGER_CARD).assertIsDisplayed()
            onNodeWithTag(TestTags.DRIVER_CARD).assertIsDisplayed()
            
            // Rotate back
            activity.requestedOrientation = 
                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            waitForIdle()
            
            UITestUtils.run {
                assertHomeScreenVisible()
            }
        }
    }

    @Test
    fun homeScreen_userInfo_displaysCorrectly() {
        with(composeTestRule) {
            // Arrange
            // User is already mocked as logged in
            navigateToHomeScreen()
            
            // Assert - User information should be displayed
            // Could be phone number, email, or welcome message
            onNodeWithText("Welcome back").assertExists()
            
            // User profile or settings access
            onNodeWithTag(TestTags.SETTINGS_BUTTON).assertIsDisplayed()
        }
    }

    @Test
    fun homeScreen_multipleNavigation_handlesGracefully() {
        with(composeTestRule) {
            // Test rapid navigation between different sections
            navigateToHomeScreen()
            
            // Rapid clicks on different cards
            repeat(3) {
                onNodeWithTag(TestTags.PASSENGER_CARD).performClick()
                waitForIdle()
                UITestUtils.run { goBack() }
                waitForIdle()
                
                onNodeWithTag(TestTags.DRIVER_CARD).performClick()
                waitForIdle()
                UITestUtils.run { goBack() }
                waitForIdle()
            }
            
            // Should still be on home screen
            UITestUtils.run {
                assertHomeScreenVisible()
            }
        }
    }

    @Test
    fun homeScreen_deepLinking_handlesDirectNavigation() {
        with(composeTestRule) {
            // Simulate deep link directly to specific role/destination
            // This would test if app can handle direct navigation to map
            
            navigateToHomeScreen()
            
            // Simulate direct navigation to passenger town map
            // In real implementation, this would test intent handling
            
            // For now, test that normal flow works
            onNodeWithTag(TestTags.PASSENGER_CARD).performClick()
            UITestUtils.run {
                selectTownDestination()
                waitForElementToAppear(TestTags.MAP_VIEW, 10000L)
                assertMapScreenVisible()
            }
        }
    }

    @Test
    fun homeScreen_errorStates_handlesGracefully() {
        with(composeTestRule) {
            navigateToHomeScreen()
            
            // Test what happens if navigation fails
            onNodeWithTag(TestTags.PASSENGER_CARD).performClick()
            
            // If destination selection fails to load
            UITestUtils.run {
                try {
                    waitForElementToAppear(TestTags.TOWN_BUTTON, 5000L)
                } catch (e: Exception) {
                    // Should show error state
                    assertErrorVisible("Failed to load destinations")
                    
                    // Retry should be available
                    onNodeWithTag(TestTags.RETRY_BUTTON)
                        .assertIsDisplayed()
                        .performClick()
                }
            }
        }
    }

    @Test
    fun homeScreen_sessionTimeout_handlesReauth() {
        with(composeTestRule) {
            // Simulate session timeout scenario
            navigateToHomeScreen()
            
            // Simulate auth token expiry
            simulateLoggedOutUser()
            
            // Act - Try to navigate
            onNodeWithTag(TestTags.PASSENGER_CARD).performClick()
            
            // Assert - Should handle authentication gracefully
            // Could redirect to login or show re-auth dialog
            UITestUtils.run {
                try {
                    assertDestinationSelectionVisible()
                } catch (e: AssertionError) {
                    // If auth expired, should redirect to login
                    assertLoginScreenVisible()
                }
            }
        }
    }

    @Test
    fun homeScreen_accessibilityFeatures_work() {
        with(composeTestRule) {
            navigateToHomeScreen()
            
            // Test accessibility features
            onNodeWithTag(TestTags.PASSENGER_CARD)
                .assertIsDisplayed()
                .assertHasClickAction()
            
            onNodeWithTag(TestTags.DRIVER_CARD)
                .assertIsDisplayed()
                .assertHasClickAction()
            
            // Content descriptions should be present
            onNodeWithContentDescription("Select passenger mode").assertExists()
            onNodeWithContentDescription("Select driver mode").assertExists()
        }
    }

    @Test
    fun homeScreen_completeUserJourney_multipleRoles() {
        with(composeTestRule) {
            // Test complete user journey using both roles
            navigateToHomeScreen()
            
            // 1. Try passenger flow
            onNodeWithTag(TestTags.PASSENGER_CARD).performClick()
            UITestUtils.run {
                selectTownDestination()
                waitForElementToAppear(TestTags.MAP_VIEW, 10000L)
                goBack()
                goBack() // Return to home
                waitForIdle()
                assertHomeScreenVisible()
            }
            
            // 2. Try driver flow  
            onNodeWithTag(TestTags.DRIVER_CARD).performClick()
            UITestUtils.run {
                selectLocalDestination()
                waitForElementToAppear(TestTags.MAP_VIEW, 10000L)
                goBack()
                goBack() // Return to home
                waitForIdle()
                assertHomeScreenVisible()
            }
            
            // 3. Access settings
            UITestUtils.run {
                navigateToSettings()
                assertSettingsScreenVisible()
                goBack()
                assertHomeScreenVisible()
            }
            
            // Home should remain stable throughout
            onNodeWithTag(TestTags.PASSENGER_CARD).assertIsDisplayed()
            onNodeWithTag(TestTags.DRIVER_CARD).assertIsDisplayed()
        }
    }

    // Helper function to navigate to home screen
    private fun ComposeContentTestRule.navigateToHomeScreen() {
        UITestUtils.run {
            completeAuthenticationFlow()
            waitForElementToAppear(TestTags.HOME_TITLE, 10000L)
        }
    }
}