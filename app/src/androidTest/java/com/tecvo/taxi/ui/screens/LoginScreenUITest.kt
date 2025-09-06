package com.tecvo.taxi.ui.screens

import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tecvo.taxi.ui.base.BaseUITest
import com.tecvo.taxi.ui.utils.UITestUtils
import com.tecvo.taxi.ui.utils.UITestUtils.TestTags
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*

/**
 * Fixed UI tests for the Login Screen with proper mock configuration
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class LoginScreenUITest : BaseUITest() {
    
    override fun configureMocksForTest() {
        // Ensure user is logged out for login screen tests
        simulateLoggedOutUser()
    }

    @Test
    fun loginScreen_initialState_displaysCorrectElements() {
        with(composeTestRule) {
            // Wait for screen to load
            waitForIdle()
            
            // Assert login screen elements are visible
            onNodeWithTag(TestTags.PHONE_INPUT).assertIsDisplayed()
            onNodeWithTag(TestTags.COUNTRY_SELECTOR).assertIsDisplayed()
            onNodeWithTag(TestTags.LOGIN_BUTTON).assertIsDisplayed()
            
            // Check initial states
            onNodeWithTag(TestTags.LOGIN_BUTTON).assertIsEnabled()
            onNodeWithTag(TestTags.LOGIN_LOADING).assertDoesNotExist()
            onNodeWithTag(TestTags.LOGIN_ERROR).assertDoesNotExist()
        }
    }

    @Test
    fun loginScreen_phoneNumberInput_acceptsValidInput() {
        with(composeTestRule) {
            val validPhoneNumber = "123456789"
            
            // Enter phone number
            onNodeWithTag(TestTags.PHONE_INPUT)
                .assertIsDisplayed()
                .performTextInput(validPhoneNumber)
            
            waitForIdle()
            
            // Assert phone number is entered
            onNodeWithTag(TestTags.PHONE_INPUT)
                .assertTextContains(validPhoneNumber)
            
            // Login button should be enabled
            onNodeWithTag(TestTags.LOGIN_BUTTON)
                .assertIsEnabled()
        }
    }

    @Test
    fun loginScreen_phoneNumberInput_handlesInvalidInput() {
        with(composeTestRule) {
            val invalidInputs = listOf(
                "",           // Empty
                "12",         // Too short
                "abc123",     // Contains letters
            )
            
            invalidInputs.forEach { invalidInput ->
                // Clear previous input
                onNodeWithTag(TestTags.PHONE_INPUT).performTextClearance()
                
                // Enter invalid input
                if (invalidInput.isNotEmpty()) {
                    onNodeWithTag(TestTags.PHONE_INPUT)
                        .performTextInput(invalidInput)
                }
                
                waitForIdle()
                
                // For empty or very short input, button should be disabled
                if (invalidInput.isEmpty() || invalidInput.length < 3) {
                    onNodeWithTag(TestTags.LOGIN_BUTTON)
                        .assertIsNotEnabled()
                }
            }
        }
    }

    @Test
    fun loginScreen_countrySelection_showsDropdown() {
        with(composeTestRule) {
            // Click country selector
            onNodeWithTag(TestTags.COUNTRY_SELECTOR)
                .assertIsDisplayed()
                .performClick()
            
            waitForIdle()
            
            // Assert country options are visible
            onNodeWithText("South Africa").assertExists()
            onNodeWithText("United States").assertExists()
            onNodeWithText("United Kingdom").assertExists()
        }
    }

    @Test
    fun loginScreen_loginButton_triggersAuthentication() {
        with(composeTestRule) {
            val phoneNumber = "123456789"
            
            // Enter phone number
            onNodeWithTag(TestTags.PHONE_INPUT)
                .performTextInput(phoneNumber)
            
            waitForIdle()
            
            // Click login button
            onNodeWithTag(TestTags.LOGIN_BUTTON)
                .assertIsDisplayed()
                .performClick()
            
            waitForIdle()
            
            // Should show loading
            onNodeWithTag(TestTags.LOGIN_LOADING)
                .assertIsDisplayed()
            
            // Login button should be disabled during loading
            onNodeWithTag(TestTags.LOGIN_BUTTON)
                .assertIsNotEnabled()
        }
    }

    @Test
    fun loginScreen_googleSignIn_buttonExists() {
        with(composeTestRule) {
            // Google sign in button should be visible
            onNodeWithContentDescription("Sign in with Google")
                .assertIsDisplayed()
                .assertHasClickAction()
        }
    }

    @Test
    fun loginScreen_termsAndConditions_navigation() {
        with(composeTestRule) {
            // Terms link should be visible
            onNodeWithText("Terms and Conditions")
                .assertIsDisplayed()
                .performClick()
            
            waitForIdle()
            
            // Should navigate to Terms screen
            onNodeWithText("Terms and Conditions", useUnmergedTree = true)
                .assertIsDisplayed()
        }
    }

    @Test
    fun loginScreen_privacyPolicy_navigation() {
        with(composeTestRule) {
            // Privacy link should be visible
            onNodeWithText("Privacy Policy")
                .assertIsDisplayed()
                .performClick()
            
            waitForIdle()
            
            // Should navigate to Privacy Policy screen
            onNodeWithText("Privacy Policy", useUnmergedTree = true)
                .assertIsDisplayed()
        }
    }

    @Test
    fun loginScreen_orientationChange_preservesState() {
        with(composeTestRule) {
            val phoneNumber = "123456789"
            
            // Enter phone number
            onNodeWithTag(TestTags.PHONE_INPUT)
                .performTextInput(phoneNumber)
            
            waitForIdle()
            
            // Rotate device
            activity.requestedOrientation = 
                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            waitForIdle()
            
            // Phone number should be preserved
            onNodeWithTag(TestTags.PHONE_INPUT)
                .assertTextContains(phoneNumber)
            
            // Rotate back
            activity.requestedOrientation = 
                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            waitForIdle()
            
            // Phone number still preserved
            onNodeWithTag(TestTags.PHONE_INPUT)
                .assertTextContains(phoneNumber)
        }
    }

    @Test
    fun loginScreen_multipleCountries_selectionWorks() {
        with(composeTestRule) {
            val countries = listOf(
                "South Africa" to "+27",
                "United States" to "+1",
                "United Kingdom" to "+44"
            )
            
            countries.forEach { (countryName, countryCode) ->
                // Open country selector
                onNodeWithTag(TestTags.COUNTRY_SELECTOR)
                    .performClick()
                
                waitForIdle()
                
                // Select country
                onNodeWithText(countryName)
                    .performClick()
                
                waitForIdle()
                
                // Country code should be displayed
                onNodeWithText(countryCode)
                    .assertIsDisplayed()
            }
        }
    }
}