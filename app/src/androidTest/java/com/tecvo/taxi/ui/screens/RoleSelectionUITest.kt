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
 * Comprehensive UI tests for Role Selection (Passenger/Driver) screens.
 * Tests role selection, destination selection, and location permissions flow.
 * 
 * Coverage:
 * - Passenger role selection
 * - Driver role selection
 * - Town vs Local destination selection
 * - Location permission requests
 * - Permission granted/denied scenarios
 * - Navigation between role screens
 * - Error handling during role setup
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RoleSelectionUITest : BaseUITest() {

    override fun configureMocksForTest() {
        // Role selection tests need a logged-in user but no pre-set role
        simulateLoggedInUser("test_user_789")
        setUserPreferences(role = null, destination = null)
    }

    @Before
    override fun setUp() {
        super.setUp()
        // Grant location permissions by default, override in specific tests
        grantLocationPermissions()
    }

    @Test
    fun roleSelectionScreen_initialState_displaysCorrectElements() {
        with(composeTestRule) {
            // Arrange - Navigate to role selection screen
            navigateToRoleSelectionFromLogin()
            
            // Assert
            UITestUtils.run {
                assertRoleSelectionScreenVisible()
            }
            
            // Verify role selection buttons
            onNodeWithTag(TestTags.PASSENGER_BUTTON).assertIsDisplayed()
            onNodeWithTag(TestTags.DRIVER_BUTTON).assertIsDisplayed()
            
            // Both should be clickable
            onNodeWithTag(TestTags.PASSENGER_BUTTON).assertHasClickAction()
            onNodeWithTag(TestTags.DRIVER_BUTTON).assertHasClickAction()
            
            // Should have descriptive text
            onNodeWithText("I need a ride").assertIsDisplayed()
            onNodeWithText("I am a taxi driver").assertIsDisplayed()
        }
    }

    @Test
    fun passengerRole_selection_navigatesToDestination() {
        with(composeTestRule) {
            // Arrange
            navigateToRoleSelectionFromLogin()
            
            // Act
            UITestUtils.run {
                selectPassengerRole()
                waitForIdle()
            }
            
            // Assert - Should navigate to destination selection
            UITestUtils.run {
                assertDestinationSelectionVisible()
            }
            
            // Verify passenger-specific UI elements
            onNodeWithText("Where are you going?").assertIsDisplayed()
            onNodeWithTag(TestTags.TOWN_BUTTON).assertIsDisplayed()
            onNodeWithTag(TestTags.LOCAL_BUTTON).assertIsDisplayed()
        }
    }

    @Test
    fun driverRole_selection_navigatesToDestination() {
        with(composeTestRule) {
            // Arrange
            navigateToRoleSelectionFromLogin()
            
            // Act
            UITestUtils.run {
                selectDriverRole()
                waitForIdle()
            }
            
            // Assert - Should navigate to destination selection
            UITestUtils.run {
                assertDestinationSelectionVisible()
            }
            
            // Verify driver-specific UI elements
            onNodeWithText("Which route are you driving?").assertIsDisplayed()
            onNodeWithTag(TestTags.TOWN_BUTTON).assertIsDisplayed()
            onNodeWithTag(TestTags.LOCAL_BUTTON).assertIsDisplayed()
        }
    }

    @Test
    fun destinationSelection_townButton_navigatesToMap() {
        with(composeTestRule) {
            // Arrange
            navigateToDestinationSelection("passenger")
            
            // Act
            UITestUtils.run {
                selectTownDestination()
                waitForIdle()
            }
            
            // Assert - Should navigate to map screen
            UITestUtils.run {
                assertMapScreenVisible()
            }
            
            // Verify town destination is set
            onNodeWithTag(TestTags.DESTINATION_LABEL)
                .assertTextContains("Town", ignoreCase = true)
        }
    }

    @Test
    fun destinationSelection_localButton_navigatesToMap() {
        with(composeTestRule) {
            // Arrange
            navigateToDestinationSelection("driver")
            
            // Act
            UITestUtils.run {
                selectLocalDestination()
                waitForIdle()
            }
            
            // Assert - Should navigate to map screen
            UITestUtils.run {
                assertMapScreenVisible()
            }
            
            // Verify local destination is set
            onNodeWithTag(TestTags.DESTINATION_LABEL)
                .assertTextContains("Local", ignoreCase = true)
        }
    }

    @Test
    fun roleSelection_withoutLocationPermission_showsPermissionDialog() {
        with(composeTestRule) {
            // Arrange - Revoke location permissions
            revokeLocationPermissions()
            navigateToRoleSelectionFromLogin()
            
            // Act
            UITestUtils.run {
                selectPassengerRole()
                selectTownDestination()
                waitForIdle()
            }
            
            // Assert - Permission dialog should appear
            UITestUtils.run {
                assertLocationPermissionDialogVisible()
            }
            
            // Verify dialog content
            onNodeWithText("Location Permission Required")
                .assertIsDisplayed()
            onNodeWithText("Allow")
                .assertIsDisplayed()
            onNodeWithText("Deny")
                .assertIsDisplayed()
        }
    }

    @Test
    fun locationPermission_granted_navigatesToMap() {
        with(composeTestRule) {
            // Arrange
            revokeLocationPermissions()
            navigateToDestinationSelection("passenger")
            
            UITestUtils.run {
                selectTownDestination()
                assertLocationPermissionDialogVisible()
            }
            
            // Act
            UITestUtils.run {
                allowLocationPermission()
                waitForIdle()
            }
            
            // Assert - Should navigate to map
            UITestUtils.run {
                assertMapScreenVisible()
            }
        }
    }

    @Test
    fun locationPermission_denied_showsError() {
        with(composeTestRule) {
            // Arrange
            revokeLocationPermissions()
            navigateToDestinationSelection("passenger")
            
            UITestUtils.run {
                selectTownDestination()
                assertLocationPermissionDialogVisible()
            }
            
            // Act
            UITestUtils.run {
                denyLocationPermission()
                waitForIdle()
            }
            
            // Assert - Should show error message
            UITestUtils.run {
                assertErrorVisible("Location permission is required")
            }
            
            // Should still be on destination selection screen
            UITestUtils.run {
                assertDestinationSelectionVisible()
            }
        }
    }

    @Test
    fun roleSelection_backNavigation_returnsToLogin() {
        with(composeTestRule) {
            // Arrange
            navigateToRoleSelectionFromLogin()
            
            // Act
            UITestUtils.run {
                goBack()
                waitForIdle()
            }
            
            // Assert - Should return to login screen
            UITestUtils.run {
                assertLoginScreenVisible()
            }
        }
    }

    @Test
    fun destinationSelection_backNavigation_returnsToRoleSelection() {
        with(composeTestRule) {
            // Arrange
            navigateToDestinationSelection("passenger")
            
            // Act
            UITestUtils.run {
                goBack()
                waitForIdle()
            }
            
            // Assert - Should return to role selection
            UITestUtils.run {
                assertRoleSelectionScreenVisible()
            }
        }
    }

    @Test
    fun passengerFlow_endToEnd_completesSuccessfully() {
        with(composeTestRule) {
            // Arrange
            navigateToRoleSelectionFromLogin()
            
            // Act - Complete full passenger flow
            UITestUtils.run {
                completeRoleSelectionFlow("passenger", "town")
                waitForIdle()
            }
            
            // Assert - Should be on passenger map screen
            UITestUtils.run {
                assertMapScreenVisible()
            }
            
            // Verify passenger-specific elements
            onNodeWithContentDescription("Passenger view").assertExists()
            onNodeWithTag(TestTags.USER_MARKER).assertIsDisplayed()
        }
    }

    @Test
    fun driverFlow_endToEnd_completesSuccessfully() {
        with(composeTestRule) {
            // Arrange
            navigateToRoleSelectionFromLogin()
            
            // Act - Complete full driver flow
            UITestUtils.run {
                completeRoleSelectionFlow("driver", "local")
                waitForIdle()
            }
            
            // Assert - Should be on driver map screen
            UITestUtils.run {
                assertMapScreenVisible()
            }
            
            // Verify driver-specific elements
            onNodeWithContentDescription("Driver view").assertExists()
            onNodeWithTag(TestTags.USER_MARKER).assertIsDisplayed()
        }
    }

    @Test
    fun roleSelection_orientationChange_preservesSelection() {
        with(composeTestRule) {
            // Arrange
            navigateToRoleSelectionFromLogin()
            
            UITestUtils.run {
                selectPassengerRole()
            }
            
            // Act - Rotate device
            activity.requestedOrientation = 
                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            waitForIdle()
            
            // Assert - Should still be on destination selection
            UITestUtils.run {
                assertDestinationSelectionVisible()
            }
            
            // Passenger-specific text should still be visible
            onNodeWithText("Where are you going?").assertIsDisplayed()
        }
    }

    @Test
    fun roleSelection_rapidClicks_handledGracefully() {
        with(composeTestRule) {
            // Arrange
            navigateToRoleSelectionFromLogin()
            
            // Act - Rapid clicks on passenger button
            repeat(5) {
                UITestUtils.run {
                    selectPassengerRole()
                }
            }
            waitForIdle()
            
            // Assert - Should only navigate once
            UITestUtils.run {
                assertDestinationSelectionVisible()
            }
            
            // Should not have duplicate screens or crashes
            onNodeWithText("Where are you going?").assertExists()
        }
    }

    @Test
    fun roleSelection_bothRoles_switchingBetween() {
        with(composeTestRule) {
            // Test switching from passenger to driver
            navigateToRoleSelectionFromLogin()
            
            // Select passenger first
            UITestUtils.run {
                selectPassengerRole()
                assertDestinationSelectionVisible()
                goBack()
                assertRoleSelectionScreenVisible()
            }
            
            // Then select driver
            UITestUtils.run {
                selectDriverRole()
                assertDestinationSelectionVisible()
            }
            
            // Verify driver-specific UI
            onNodeWithText("Which route are you driving?").assertIsDisplayed()
        }
    }

    @Test
    fun destinationSelection_bothDestinations_switchingBetween() {
        with(composeTestRule) {
            navigateToDestinationSelection("passenger")
            
            // Try town destination first
            UITestUtils.run {
                selectTownDestination()
                waitForElementToAppear(TestTags.MAP_VIEW, 10000L)
                goBack() // Go back to destination selection
                assertDestinationSelectionVisible()
            }
            
            // Then try local destination
            UITestUtils.run {
                selectLocalDestination()
                waitForElementToAppear(TestTags.MAP_VIEW, 10000L)
            }
            
            // Verify local destination is set
            onNodeWithTag(TestTags.DESTINATION_LABEL)
                .assertTextContains("Local", ignoreCase = true)
        }
    }

    // Helper functions
    private fun ComposeContentTestRule.navigateToRoleSelectionFromLogin() {
        // Simulate successful login flow
        UITestUtils.run {
            completeAuthenticationFlow()
            waitForElementToAppear(TestTags.PASSENGER_BUTTON, 10000L)
        }
    }
    
    private fun ComposeContentTestRule.navigateToDestinationSelection(role: String) {
        navigateToRoleSelectionFromLogin()
        when (role) {
            "passenger" -> UITestUtils.run { selectPassengerRole() }
            "driver" -> UITestUtils.run { selectDriverRole() }
        }
        UITestUtils.run {
            waitForElementToAppear(TestTags.TOWN_BUTTON, 5000L)
        }
    }
}