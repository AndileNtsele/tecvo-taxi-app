package com.tecvo.taxi.ui.screens

import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tecvo.taxi.ui.base.BaseUITest
import com.tecvo.taxi.ui.utils.UITestUtils.TestTags
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import org.junit.runner.RunWith

/**
 * OPTIMIZED Login Screen UI Tests using improved BaseUITest
 *
 * Improvements Applied:
 * - No runBlocking in Firebase mocking
 * - Optimized test rule ordering
 * - Minimal cleanup operations
 * - Expected execution time: 30-60 seconds per test (vs 5-10 minutes before)
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class OptimizedLoginScreenUITest : BaseUITest() {

    override fun configureMocksForTest() {
        // Ensure user is logged out for login screen tests
        simulateLoggedOutUser()
    }

    // Optional: Skip heavy compose setup for simple tests
    override fun shouldSetupComposeContent(): Boolean = true

    @Test
    fun loginScreen_initialState_displaysCorrectElements_optimized() {
        with(composeTestRule) {
            // Wait with timeout
            waitForCompose(timeoutMs = 5000)

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
    fun loginScreen_phoneNumberInput_saNumbers_optimized() {
        with(composeTestRule) {
            // Test SA taxi driver numbers specifically
            val saPhoneNumbers = listOf(
                "0728588857", // Test credential from CLAUDE.md
                "0821234567",
                "0731234567"
            )

            saPhoneNumbers.forEach { phoneNumber ->
                // Clear previous input
                onNodeWithTag(TestTags.PHONE_INPUT).performTextClearance()

                // Enter SA phone number
                onNodeWithTag(TestTags.PHONE_INPUT)
                    .assertIsDisplayed()
                    .performTextInput(phoneNumber)

                waitForCompose(timeoutMs = 2000)

                // Assert phone number is entered
                onNodeWithTag(TestTags.PHONE_INPUT)
                    .assertTextContains(phoneNumber)

                // Login button should be enabled for valid SA numbers
                onNodeWithTag(TestTags.LOGIN_BUTTON)
                    .assertIsEnabled()
            }
        }
    }

    @Test
    fun loginScreen_countrySelection_southAfrica_optimized() {
        with(composeTestRule) {
            // Click country selector
            onNodeWithTag(TestTags.COUNTRY_SELECTOR)
                .assertIsDisplayed()
                .performClick()

            waitForCompose(timeoutMs = 3000)

            // Focus on South Africa (primary market for TAXI app)
            onNodeWithText("South Africa").assertExists()
            onNodeWithText("South Africa").performClick()

            waitForCompose(timeoutMs = 2000)

            // Verify SA country code is selected
            onNodeWithText("+27").assertIsDisplayed()
        }
    }

    @Test
    fun loginScreen_criticalPath_saDriver_optimized() {
        // Test the critical path for SA taxi drivers (core user journey)
        with(composeTestRule) {
            val testPhoneNumber = "0728588857" // From CLAUDE.md test credentials

            // Step 1: Select South Africa
            onNodeWithTag(TestTags.COUNTRY_SELECTOR).performClick()
            waitForCompose(timeoutMs = 2000)
            onNodeWithText("South Africa").performClick()
            waitForCompose(timeoutMs = 1000)

            // Step 2: Enter SA phone number
            onNodeWithTag(TestTags.PHONE_INPUT)
                .performTextInput(testPhoneNumber)
            waitForCompose(timeoutMs = 1000)

            // Step 3: Click login
            onNodeWithTag(TestTags.LOGIN_BUTTON)
                .assertIsEnabled()
                .performClick()

            waitForCompose(timeoutMs = 3000)

            // Should show loading (mocked to fail, but loading should appear)
            onNodeWithTag(TestTags.LOGIN_LOADING)
                .assertIsDisplayed()
        }
    }

    @Test
    fun loginScreen_performance_renderUnder5Seconds() {
        // Performance test - login screen should render quickly
        val startTime = System.currentTimeMillis()

        with(composeTestRule) {
            waitForCompose(timeoutMs = 5000)

            // Check that essential elements are present
            onNodeWithTag(TestTags.PHONE_INPUT).assertExists()
            onNodeWithTag(TestTags.LOGIN_BUTTON).assertExists()
        }

        val renderTime = System.currentTimeMillis() - startTime

        // Login screen should render quickly (under 5 seconds with optimizations)
        assert(renderTime < 5000) {
            "Login screen took ${renderTime}ms to render - too slow! Expected < 5000ms"
        }
    }

    @Test
    fun loginScreen_errorHandling_graceful() {
        // Test that error states don't crash the app
        with(composeTestRule) {
            val invalidNumber = "123" // Too short

            onNodeWithTag(TestTags.PHONE_INPUT)
                .performTextInput(invalidNumber)

            waitForCompose(timeoutMs = 1000)

            // Button should be disabled for invalid input
            onNodeWithTag(TestTags.LOGIN_BUTTON)
                .assertIsNotEnabled()

            // No error messages should crash the UI
            // (This tests graceful handling of edge cases)
        }
    }

    @Test
    fun loginScreen_fuelSavingContext_driverUse() {
        // Test in context of fuel-saving taxi driver use case
        with(composeTestRule) {
            // Simulate driver opening app during off-peak hours (9am-3pm)
            // when passengers are scarce - core value proposition

            onNodeWithTag(TestTags.PHONE_INPUT)
                .assertIsDisplayed()

            // Should be quick to use (drivers don't want to waste time/fuel)
            val startTime = System.currentTimeMillis()

            onNodeWithTag(TestTags.PHONE_INPUT)
                .performTextInput("0728588857")

            onNodeWithTag(TestTags.LOGIN_BUTTON)
                .performClick()

            waitForCompose(timeoutMs = 2000)

            val interactionTime = System.currentTimeMillis() - startTime

            // Interaction should be fast for fuel-conscious drivers
            assert(interactionTime < 3000) {
                "Login interaction took ${interactionTime}ms - drivers will waste fuel waiting!"
            }
        }
    }
}