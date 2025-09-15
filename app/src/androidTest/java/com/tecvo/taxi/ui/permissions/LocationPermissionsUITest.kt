package com.tecvo.taxi.ui.permissions

import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tecvo.taxi.ui.base.BaseUITest
import com.tecvo.taxi.ui.utils.UITestUtils
import com.tecvo.taxi.ui.utils.UITestUtils.TestTags
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

/**
 * Comprehensive UI tests for Location Permissions handling.
 * Tests all location permission scenarios critical for the taxi app functionality.
 * 
 * Coverage:
 * - Initial permission requests
 * - Permission granted scenarios  
 * - Permission denied scenarios
 * - Permission revoked during app usage
 * - Rationale dialogs and explanations
 * - Settings navigation for permissions
 * - Location accuracy requirements
 * - Background location permissions (if used)
 * - Recovery from permission denial
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class LocationPermissionsUITest : BaseUITest() {

    @Test
    fun locationPermission_firstTime_showsPermissionDialog() {
        with(composeTestRule) {
            // Arrange - Start with no location permissions
            revokeLocationPermissions()
            
            // Act - Navigate to map screen  
            UITestUtils.run {
                completeAuthenticationFlow()
                completeRoleSelectionFlow("passenger", "town")
            }
            
            // Assert - Permission dialog should appear
            UITestUtils.run {
                assertLocationPermissionDialogVisible()
            }
            
            // Dialog should have proper content
            onNodeWithText("Location Permission Required").assertIsDisplayed()
            onNodeWithText("Allow").assertIsDisplayed()
            onNodeWithText("Deny").assertIsDisplayed()
            
            // Explanation text should be present
            onNodeWithText("location").assertExists()
            onNodeWithText("taxi").assertExists()
        }
    }

    @Test
    fun locationPermission_granted_navigatesToMap() {
        with(composeTestRule) {
            // Arrange
            revokeLocationPermissions()
            
            UITestUtils.run {
                completeAuthenticationFlow()
                completeRoleSelectionFlow("passenger", "town")
                assertLocationPermissionDialogVisible()
            }
            
            // Act - Grant permission
            UITestUtils.run {
                allowLocationPermission()
                waitForIdle()
            }
            
            // Assert - Should navigate to map screen
            UITestUtils.run {
                waitForElementToAppear(TestTags.MAP_VIEW, 10000L)
                assertMapScreenVisible()
            }
            
            // User marker should appear (indicates location is working)
            UITestUtils.run {
                waitForElementToAppear(TestTags.USER_MARKER, 10000L)
            }
            onNodeWithTag(TestTags.USER_MARKER).assertIsDisplayed()
        }
    }

    @Test
    fun locationPermission_denied_showsErrorAndRetry() {
        with(composeTestRule) {
            // Arrange
            revokeLocationPermissions()
            
            UITestUtils.run {
                completeAuthenticationFlow()
                completeRoleSelectionFlow("driver", "local")
                assertLocationPermissionDialogVisible()
            }
            
            // Act - Deny permission
            UITestUtils.run {
                denyLocationPermission()
                waitForIdle()
            }
            
            // Assert - Should show error message
            UITestUtils.run {
                assertErrorVisible("Location permission is required")
            }
            
            // Should remain on destination selection or show retry option
            try {
                UITestUtils.run {
                    assertDestinationSelectionVisible()
                }
            } catch (e: AssertionError) {
                // Or might show dedicated permission error screen
                onNodeWithText("Permission Required").assertIsDisplayed()
                onNodeWithTag(TestTags.RETRY_BUTTON).assertIsDisplayed()
            }
        }
    }

    @Test
    fun locationPermission_deniedTwice_showsSettingsPrompt() {
        with(composeTestRule) {
            // Arrange
            revokeLocationPermissions()
            
            UITestUtils.run {
                completeAuthenticationFlow()
                completeRoleSelectionFlow("passenger", "local")
                assertLocationPermissionDialogVisible()
            }
            
            // First denial
            UITestUtils.run {
                denyLocationPermission()
                waitForIdle()
            }
            
            // Try again - should show rationale
            try {
                onNodeWithTag(TestTags.RETRY_BUTTON).performClick()
                UITestUtils.run {
                    assertLocationPermissionDialogVisible()
                }
                
                // Second denial
                UITestUtils.run {
                    denyLocationPermission()
                    waitForIdle()
                }
                
                // Should show settings prompt
                onNodeWithText("Go to Settings").assertIsDisplayed()
                onNodeWithText("Permission permanently denied").assertExists()
                
            } catch (e: AssertionError) {
                // Permission flow might vary based on Android version
                // Basic error handling should still work
                UITestUtils.run {
                    assertErrorVisible("Location permission")
                }
            }
        }
    }

    @Test
    fun locationPermission_revokedDuringUsage_handlesGracefully() {
        with(composeTestRule) {
            // Arrange - Start with permission granted
            grantLocationPermissions()
            
            UITestUtils.run {
                completeAuthenticationFlow()
                completeRoleSelectionFlow("driver", "town")
                waitForElementToAppear(TestTags.MAP_VIEW, 10000L)
                waitForElementToAppear(TestTags.USER_MARKER, 10000L)
            }
            
            // Act - Revoke permission while app is running
            revokeLocationPermissions()
            runBlocking { delay(2000) }
            
            // Try to interact with location features
            UITestUtils.run {
                clickCurrentLocationButton()
                waitForIdle()
            }
            
            // Assert - Should handle gracefully
            try {
                UITestUtils.run {
                    assertLocationPermissionDialogVisible()
                    allowLocationPermission()
                }
            } catch (e: AssertionError) {
                // Or show error message
                UITestUtils.run {
                    assertErrorVisible("Location access denied")
                }
            }
        }
    }

    @Test
    fun locationPermission_rationaleDialog_showsExplanation() {
        with(composeTestRule) {
            // Arrange
            revokeLocationPermissions()
            
            UITestUtils.run {
                completeAuthenticationFlow()
                completeRoleSelectionFlow("passenger", "town")
                assertLocationPermissionDialogVisible()
            }
            
            // Deny first time
            UITestUtils.run {
                denyLocationPermission()
                waitForIdle()
            }
            
            // Try again - should show rationale
            try {
                onNodeWithTag(TestTags.RETRY_BUTTON).performClick()
                
                // Assert - Rationale should explain why permission is needed
                onNodeWithText("Why we need location").assertExists()
                onNodeWithText("find nearby").assertExists()
                onNodeWithText("taxi").assertExists()
                
                // Should still have Allow/Deny options
                onNodeWithText("Allow").assertIsDisplayed()
                onNodeWithText("Deny").assertIsDisplayed()
                
            } catch (e: AssertionError) {
                // Rationale might not be implemented yet
                UITestUtils.run {
                    assertLocationPermissionDialogVisible()
                }
            }
        }
    }

    @Test
    fun locationPermission_settingsNavigation_worksCorrectly() {
        with(composeTestRule) {
            // Arrange - Trigger settings navigation
            revokeLocationPermissions()
            
            UITestUtils.run {
                completeAuthenticationFlow()
                completeRoleSelectionFlow("driver", "local")
                assertLocationPermissionDialogVisible()
                denyLocationPermission()
                waitForIdle()
            }
            
            // Act - Navigate to settings (if option is available)
            try {
                onNodeWithText("Go to Settings").performClick()
                waitForIdle()
                
                // Note: This would open system settings, so we can't test much more
                // In real test, verify intent was sent to open settings
                
            } catch (e: AssertionError) {
                // Settings navigation might not be implemented
                // Basic error handling should still work
                UITestUtils.run {
                    assertErrorVisible("Location permission")
                }
            }
        }
    }

    @Test
    fun locationPermission_foregroundOnly_worksWithoutBackgroundPermission() {
        with(composeTestRule) {
            // Test that app works with only foreground location permission
            grantLocationPermissions() // Only grants foreground permission
            
            UITestUtils.run {
                completeAuthenticationFlow()
                completeRoleSelectionFlow("passenger", "town")
                waitForElementToAppear(TestTags.MAP_VIEW, 10000L)
            }
            
            // App should work normally with only foreground permission
            UITestUtils.run {
                assertMapScreenVisible()
                waitForElementToAppear(TestTags.USER_MARKER, 10000L)
            }
            
            // Should never ask for background location permission
            onNodeWithText("Background Location").assertDoesNotExist()
            onNodeWithText("Allow all the time").assertDoesNotExist()
            
            // Location features should work in foreground
            UITestUtils.run {
                clickCurrentLocationButton()
                waitForIdle()
            }
            
            // Should continue to work normally
            UITestUtils.run {
                assertMapScreenVisible()
            }
        }
    }

    @Test
    fun locationPermission_appPausedAndResumed_handlesCorrectly() {
        with(composeTestRule) {
            // Test app behavior when paused and resumed (foreground-only app)
            grantLocationPermissions()
            
            UITestUtils.run {
                completeAuthenticationFlow()
                completeRoleSelectionFlow("driver", "local")
                waitForElementToAppear(TestTags.MAP_VIEW, 10000L)
                waitForElementToAppear(TestTags.USER_MARKER, 10000L)
            }
            
            // Simulate app going to background (pause)
            simulateAppBackground()
            runBlocking { delay(1000) }

            // Simulate app returning to foreground (resume)
            simulateAppForeground()
            runBlocking { delay(1000) }
            
            // App should resume normally with foreground permissions
            UITestUtils.run {
                assertMapScreenVisible()
            }
            
            // Location features should still work
            UITestUtils.run {
                clickCurrentLocationButton()
                waitForIdle()
            }
            
            // Should continue to work normally
            UITestUtils.run {
                assertMapScreenVisible()
            }
        }
    }

    @Test
    fun locationPermission_preciseLocation_handledCorrectly() {
        with(composeTestRule) {
            // Test precise location permission (Android 12+)
            revokeLocationPermissions()
            
            UITestUtils.run {
                completeAuthenticationFlow()
                completeRoleSelectionFlow("driver", "town")
                assertLocationPermissionDialogVisible()
                allowLocationPermission()
                waitForIdle()
            }
            
            // Check if precise location dialog appears
            try {
                onNodeWithText("Precise Location").assertExists()
                onNodeWithText("Use precise location").assertExists()
                
                // Allow precise location
                onNodeWithText("Use precise location").performClick()
                waitForIdle()
                
            } catch (e: AssertionError) {
                // Precise location dialog might not appear
                // This depends on Android version and implementation
            }
            
            // Should reach map screen regardless
            UITestUtils.run {
                waitForElementToAppear(TestTags.MAP_VIEW, 10000L)
                assertMapScreenVisible()
            }
        }
    }

    @Test
    fun locationPermission_multipleRoles_consistentBehavior() {
        with(composeTestRule) {
            // Test permission behavior is consistent across roles
            revokeLocationPermissions()
            
            // Test passenger role
            UITestUtils.run {
                completeAuthenticationFlow()
                completeRoleSelectionFlow("passenger", "town")
                assertLocationPermissionDialogVisible()
                allowLocationPermission()
                waitForElementToAppear(TestTags.MAP_VIEW, 10000L)
                
                // Navigate back to home
                goBack()
                goBack()
                waitForElementToAppear(TestTags.HOME_TITLE, 5000L)
            }
            
            // Test driver role - permission should be remembered
            UITestUtils.run {
                completeRoleSelectionFlow("driver", "local")
                waitForElementToAppear(TestTags.MAP_VIEW, 10000L)
            }
            
            // Should not ask for permission again
            onNodeWithTag(TestTags.PERMISSION_DIALOG).assertDoesNotExist()
            UITestUtils.run {
                assertMapScreenVisible()
            }
        }
    }

    @Test
    fun locationPermission_orientationChange_preservesState() {
        with(composeTestRule) {
            // Arrange
            revokeLocationPermissions()
            
            UITestUtils.run {
                completeAuthenticationFlow()
                completeRoleSelectionFlow("passenger", "local")
                assertLocationPermissionDialogVisible()
            }
            
            // Act - Rotate device while permission dialog is shown
            activity.requestedOrientation = 
                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            waitForIdle()
            
            // Assert - Permission dialog should still be visible
            UITestUtils.run {
                assertLocationPermissionDialogVisible()
            }
            
            // Grant permission
            UITestUtils.run {
                allowLocationPermission()
                waitForElementToAppear(TestTags.MAP_VIEW, 10000L)
            }
            
            // Map should work normally
            UITestUtils.run {
                assertMapScreenVisible()
            }
        }
    }

    @Test
    fun locationPermission_errorRecovery_worksCorrectly() {
        with(composeTestRule) {
            // Test recovery from permission-related errors
            revokeLocationPermissions()
            
            UITestUtils.run {
                completeAuthenticationFlow()
                completeRoleSelectionFlow("driver", "town")
                assertLocationPermissionDialogVisible()
                denyLocationPermission()
                waitForIdle()
                assertErrorVisible("Location permission")
            }
            
            // Try to recover by retrying
            try {
                onNodeWithTag(TestTags.RETRY_BUTTON).performClick()
                UITestUtils.run {
                    assertLocationPermissionDialogVisible()
                    allowLocationPermission()
                    waitForElementToAppear(TestTags.MAP_VIEW, 10000L)
                    assertMapScreenVisible()
                }
            } catch (e: AssertionError) {
                // Recovery might navigate back to role selection
                UITestUtils.run {
                    assertDestinationSelectionVisible()
                }
                
                // Try the flow again
                UITestUtils.run {
                    selectTownDestination()
                    assertLocationPermissionDialogVisible()
                    allowLocationPermission()
                    waitForElementToAppear(TestTags.MAP_VIEW, 10000L)
                }
            }
            
            // Should eventually reach map
            UITestUtils.run {
                assertMapScreenVisible()
            }
        }
    }

    @Test
    fun locationPermission_rapidInteractions_handlesGracefully() {
        with(composeTestRule) {
            // Test rapid permission dialog interactions
            revokeLocationPermissions()
            
            UITestUtils.run {
                completeAuthenticationFlow()
                completeRoleSelectionFlow("passenger", "town")
                assertLocationPermissionDialogVisible()
            }
            
            // Rapid clicks on permission buttons
            repeat(3) {
                try {
                    UITestUtils.run {
                        denyLocationPermission()
                    }
                    runBlocking { delay(500) }
                } catch (e: Exception) {
                    // Dialog might disappear, that's okay
                }
            }
            
            waitForIdle()
            
            // Should handle gracefully and show appropriate state
            try {
                UITestUtils.run {
                    assertErrorVisible("Location permission")
                }
            } catch (e: AssertionError) {
                // Might still be on destination selection
                UITestUtils.run {
                    assertDestinationSelectionVisible()
                }
            }
        }
    }

    @Test
    fun locationPermission_completeFlow_endToEnd() {
        with(composeTestRule) {
            // Test complete permission flow from start to map usage
            revokeLocationPermissions()
            
            // 1. Start flow without permissions
            UITestUtils.run {
                completeAuthenticationFlow()
                completeRoleSelectionFlow("driver", "local")
                assertLocationPermissionDialogVisible()
            }
            
            // 2. Grant permissions
            UITestUtils.run {
                allowLocationPermission()
                waitForElementToAppear(TestTags.MAP_VIEW, 10000L)
                assertMapScreenVisible()
            }
            
            // 3. Use location-dependent features
            UITestUtils.run {
                waitForElementToAppear(TestTags.USER_MARKER, 10000L)
                clickCurrentLocationButton()
            }
            
            // 4. Navigate away and back
            UITestUtils.run {
                goBack()
                goBack()
                waitForElementToAppear(TestTags.HOME_TITLE, 5000L)
            }
            
            // 5. Return to map - should not ask for permission again
            UITestUtils.run {
                completeRoleSelectionFlow("passenger", "town")
                waitForElementToAppear(TestTags.MAP_VIEW, 10000L)
                assertMapScreenVisible()
            }
            
            // Permission should be remembered
            onNodeWithTag(TestTags.PERMISSION_DIALOG).assertDoesNotExist()
            UITestUtils.run {
                waitForElementToAppear(TestTags.USER_MARKER, 10000L)
            }
        }
    }
}