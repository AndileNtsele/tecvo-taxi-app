package com.tecvo.taxi.ui.utils

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.isNotEnabled

/**
 * Comprehensive UI testing utilities for the Taxi application.
 * Provides reusable methods for common UI interactions and assertions.
 */
object UITestUtils {

    /**
     * Test Tags - Define all UI component test tags used throughout the app
     */
    object TestTags {
        // Login Screen
        const val LOGIN_BUTTON = "login_button"
        const val PHONE_INPUT = "phone_input"
        const val COUNTRY_SELECTOR = "country_selector"
        const val LOGIN_LOADING = "login_loading"
        const val LOGIN_ERROR = "login_error"
        
        // Role Selection Screen  
        const val PASSENGER_BUTTON = "passenger_button"
        const val DRIVER_BUTTON = "driver_button"
        const val TOWN_BUTTON = "town_button"
        const val LOCAL_BUTTON = "local_button"
        const val LOCATION_PERMISSION_DIALOG = "location_permission_dialog"
        
        // Map Screen
        const val MAP_VIEW = "map_view"
        const val CURRENT_LOCATION_BUTTON = "current_location_button"
        const val MAP_LOADING = "map_loading"
        const val USER_MARKER = "user_marker"
        const val OTHER_USERS_MARKER = "other_users_marker"
        const val MAP_ERROR = "map_error"
        const val DESTINATION_LABEL = "destination_label"
        
        // Home Screen
        const val HOME_TITLE = "home_title"
        const val PASSENGER_CARD = "passenger_card"
        const val DRIVER_CARD = "driver_card"
        const val SETTINGS_BUTTON = "settings_button"
        
        // Settings Screen
        const val SETTINGS_TITLE = "settings_title"
        const val LOGOUT_BUTTON = "logout_button"
        const val TERMS_BUTTON = "terms_button"
        const val PRIVACY_BUTTON = "privacy_button"
        
        // Navigation
        const val BACK_BUTTON = "back_button"
        const val HOME_BUTTON = "home_button"
        
        // Common
        const val LOADING_INDICATOR = "loading_indicator"
        const val ERROR_MESSAGE = "error_message"
        const val RETRY_BUTTON = "retry_button"
        const val PERMISSION_DIALOG = "permission_dialog"
        const val PERMISSION_ALLOW_BUTTON = "permission_allow_button"
        const val PERMISSION_DENY_BUTTON = "permission_deny_button"
    }

    /**
     * Common UI Interactions
     */
    
    fun ComposeContentTestRule.clickLoginButton() {
        onNodeWithTag(TestTags.LOGIN_BUTTON)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }
    
    fun ComposeContentTestRule.enterPhoneNumber(phoneNumber: String) {
        onNodeWithTag(TestTags.PHONE_INPUT)
            .assertIsDisplayed()
            .performTextInput(phoneNumber)
    }
    
    fun ComposeContentTestRule.selectPassengerRole() {
        onNodeWithTag(TestTags.PASSENGER_BUTTON)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }
    
    fun ComposeContentTestRule.selectDriverRole() {
        onNodeWithTag(TestTags.DRIVER_BUTTON)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }
    
    fun ComposeContentTestRule.selectTownDestination() {
        onNodeWithTag(TestTags.TOWN_BUTTON)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }
    
    fun ComposeContentTestRule.selectLocalDestination() {
        onNodeWithTag(TestTags.LOCAL_BUTTON)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }
    
    fun ComposeContentTestRule.allowLocationPermission() {
        onNodeWithTag(TestTags.PERMISSION_ALLOW_BUTTON)
            .assertIsDisplayed()
            .performClick()
    }
    
    fun ComposeContentTestRule.denyLocationPermission() {
        onNodeWithTag(TestTags.PERMISSION_DENY_BUTTON)
            .assertIsDisplayed()
            .performClick()
    }
    
    fun ComposeContentTestRule.clickCurrentLocationButton() {
        onNodeWithTag(TestTags.CURRENT_LOCATION_BUTTON)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }
    
    fun ComposeContentTestRule.navigateToSettings() {
        onNodeWithTag(TestTags.SETTINGS_BUTTON)
            .assertIsDisplayed()
            .performClick()
    }
    
    fun ComposeContentTestRule.logout() {
        onNodeWithTag(TestTags.LOGOUT_BUTTON)
            .assertIsDisplayed()
            .performClick()
    }
    
    fun ComposeContentTestRule.goBack() {
        onNodeWithTag(TestTags.BACK_BUTTON)
            .assertIsDisplayed()
            .performClick()
    }

    /**
     * Common UI Assertions
     */
    
    fun ComposeContentTestRule.assertLoginScreenVisible() {
        onNodeWithTag(TestTags.LOGIN_BUTTON).assertIsDisplayed()
        onNodeWithTag(TestTags.PHONE_INPUT).assertIsDisplayed()
    }
    
    fun ComposeContentTestRule.assertRoleSelectionScreenVisible() {
        onNodeWithTag(TestTags.PASSENGER_BUTTON).assertIsDisplayed()
        onNodeWithTag(TestTags.DRIVER_BUTTON).assertIsDisplayed()
    }
    
    fun ComposeContentTestRule.assertDestinationSelectionVisible() {
        onNodeWithTag(TestTags.TOWN_BUTTON).assertIsDisplayed()
        onNodeWithTag(TestTags.LOCAL_BUTTON).assertIsDisplayed()
    }
    
    fun ComposeContentTestRule.assertMapScreenVisible() {
        onNodeWithTag(TestTags.MAP_VIEW).assertIsDisplayed()
        onNodeWithTag(TestTags.CURRENT_LOCATION_BUTTON).assertIsDisplayed()
    }
    
    fun ComposeContentTestRule.assertHomeScreenVisible() {
        onNodeWithTag(TestTags.HOME_TITLE).assertIsDisplayed()
        onNodeWithTag(TestTags.PASSENGER_CARD).assertIsDisplayed()
        onNodeWithTag(TestTags.DRIVER_CARD).assertIsDisplayed()
    }
    
    fun ComposeContentTestRule.assertSettingsScreenVisible() {
        onNodeWithTag(TestTags.SETTINGS_TITLE).assertIsDisplayed()
        onNodeWithTag(TestTags.LOGOUT_BUTTON).assertIsDisplayed()
    }
    
    fun ComposeContentTestRule.assertLoadingVisible() {
        onNodeWithTag(TestTags.LOADING_INDICATOR).assertIsDisplayed()
    }
    
    fun ComposeContentTestRule.assertErrorVisible(errorMessage: String? = null) {
        val errorNode = onNodeWithTag(TestTags.ERROR_MESSAGE)
        errorNode.assertIsDisplayed()
        errorMessage?.let {
            errorNode.assertTextContains(it, ignoreCase = true)
        }
    }
    
    fun ComposeContentTestRule.assertLocationPermissionDialogVisible() {
        onNodeWithTag(TestTags.LOCATION_PERMISSION_DIALOG).assertIsDisplayed()
        onNodeWithTag(TestTags.PERMISSION_ALLOW_BUTTON).assertIsDisplayed()
        onNodeWithTag(TestTags.PERMISSION_DENY_BUTTON).assertIsDisplayed()
    }

    /**
     * Wait Utilities
     */
    
    fun ComposeContentTestRule.waitForElementToAppear(
        testTag: String,
        timeoutMs: Long = 5000L
    ) {
        waitUntil(timeoutMs) {
            try {
                onNodeWithTag(testTag).assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }
    
    fun ComposeContentTestRule.waitForElementToDisappear(
        testTag: String,
        timeoutMs: Long = 5000L
    ) {
        waitUntil(timeoutMs) {
            try {
                onNodeWithTag(testTag).assertDoesNotExist()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }
    
    fun ComposeContentTestRule.waitForTextToAppear(
        text: String,
        timeoutMs: Long = 5000L
    ) {
        waitUntil(timeoutMs) {
            try {
                onNodeWithText(text).assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }

    /**
     * Firebase Integration Test Helpers
     */
    
    fun ComposeContentTestRule.assertUserPresenceOnMap(role: String) {
        when (role) {
            "passenger" -> {
                onNodeWithTag(TestTags.USER_MARKER).assertIsDisplayed()
                // Assert passenger-specific UI elements
            }
            "driver" -> {
                onNodeWithTag(TestTags.USER_MARKER).assertIsDisplayed()
                // Assert driver-specific UI elements
            }
        }
    }
    
    fun ComposeContentTestRule.assertOtherUsersVisible(count: Int) {
        // This would need to be implemented based on how other users are displayed
        // For now, assert that the map shows other user markers
        repeat(count) {
            onNodeWithTag("${TestTags.OTHER_USERS_MARKER}_$it").assertIsDisplayed()
        }
    }

    /**
     * Navigation Test Helpers
     */
    
    fun ComposeContentTestRule.completeAuthenticationFlow(phoneNumber: String = "+27123456789") {
        assertLoginScreenVisible()
        enterPhoneNumber(phoneNumber)
        clickLoginButton()
        // Would need to handle OTP verification in real implementation
    }
    
    fun ComposeContentTestRule.completeRoleSelectionFlow(
        role: String,
        destination: String
    ) {
        assertRoleSelectionScreenVisible()
        
        when (role) {
            "passenger" -> selectPassengerRole()
            "driver" -> selectDriverRole()
        }
        
        assertDestinationSelectionVisible()
        
        when (destination) {
            "town" -> selectTownDestination()
            "local" -> selectLocalDestination()
        }
    }

    /**
     * Custom Semantic Matchers
     */
    
    fun hasTestTag(testTag: String): SemanticsMatcher = 
        SemanticsMatcher.expectValue(SemanticsProperties.TestTag, testTag)
    
    fun isButtonEnabled(): SemanticsMatcher = 
        SemanticsMatcher("is button enabled") { semanticsNode ->
            semanticsNode.config.getOrNull(SemanticsProperties.Disabled) == null
        }
    
    fun containsText(text: String): SemanticsMatcher =
        SemanticsMatcher("contains text '$text'") { semanticsNode ->
            val nodeText = semanticsNode.config.getOrNull(SemanticsProperties.Text)
            nodeText?.any { it.text.contains(text, ignoreCase = true) } == true
        }
}