package com.tecvo.taxi.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class LocationButtonTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun locationButton_displaysLocationName() {
        composeTestRule.setContent {
            LocationButton(
                locationName = "Johannesburg",
                driverCount = 5,
                passengerCount = 3,
                onClick = {}
            )
        }

        composeTestRule
            .onNodeWithText("Johannesburg")
            .assertIsDisplayed()
    }

    @Test
    fun locationButton_displaysLocationAndCityName() {
        composeTestRule.setContent {
            LocationButton(
                locationName = "Sandton",
                cityName = "Johannesburg",
                driverCount = 10,
                passengerCount = 7,
                onClick = {}
            )
        }

        composeTestRule
            .onNodeWithText("Sandton (Johannesburg)")
            .assertIsDisplayed()
    }

    @Test
    fun locationButton_displaysSameLocationAndCityName() {
        composeTestRule.setContent {
            LocationButton(
                locationName = "Johannesburg",
                cityName = "Johannesburg",
                driverCount = 8,
                passengerCount = 4,
                onClick = {}
            )
        }

        // Should only show location name once when it's the same as city name
        composeTestRule
            .onNodeWithText("Johannesburg")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Johannesburg (Johannesburg)")
            .assertDoesNotExist()
    }

    @Test
    fun locationButton_displaysDriverCount() {
        composeTestRule.setContent {
            LocationButton(
                locationName = "Cape Town",
                driverCount = 15,
                passengerCount = 8,
                onClick = {}
            )
        }

        composeTestRule
            .onNodeWithText("15")
            .assertIsDisplayed()
    }

    @Test
    fun locationButton_displaysPassengerCount() {
        composeTestRule.setContent {
            LocationButton(
                locationName = "Durban",
                driverCount = 12,
                passengerCount = 6,
                onClick = {}
            )
        }

        composeTestRule
            .onNodeWithText("6")
            .assertIsDisplayed()
    }

    @Test
    fun locationButton_displaysZeroCounts() {
        composeTestRule.setContent {
            LocationButton(
                locationName = "Port Elizabeth",
                driverCount = 0,
                passengerCount = 0,
                onClick = {}
            )
        }

        composeTestRule
            .onNodeWithText("0")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun locationButton_showsUnfilledStarWhenNotFavorite() {
        composeTestRule.setContent {
            LocationButton(
                locationName = "Bloemfontein",
                driverCount = 3,
                passengerCount = 2,
                isFavorite = false,
                onClick = {},
                onFavoriteToggle = {}
            )
        }

        composeTestRule
            .onNodeWithContentDescription("Add to favorites")
            .assertIsDisplayed()
    }

    @Test
    fun locationButton_showsFilledStarWhenFavorite() {
        composeTestRule.setContent {
            LocationButton(
                locationName = "Pretoria",
                driverCount = 7,
                passengerCount = 4,
                isFavorite = true,
                onClick = {},
                onFavoriteToggle = {}
            )
        }

        composeTestRule
            .onNodeWithContentDescription("Remove from favorites")
            .assertIsDisplayed()
    }

    @Test
    fun locationButton_showsExpandIcon_whenExpandableAndNotExpanded() {
        composeTestRule.setContent {
            LocationButton(
                locationName = "East London",
                driverCount = 4,
                passengerCount = 1,
                isExpandable = true,
                isExpanded = false,
                onClick = {}
            )
        }

        composeTestRule
            .onNodeWithContentDescription("Expand East London")
            .assertIsDisplayed()
    }

    @Test
    fun locationButton_showsCollapseIcon_whenExpandableAndExpanded() {
        composeTestRule.setContent {
            LocationButton(
                locationName = "Polokwane",
                driverCount = 6,
                passengerCount = 3,
                isExpandable = true,
                isExpanded = true,
                onClick = {}
            )
        }

        composeTestRule
            .onNodeWithContentDescription("Collapse Polokwane")
            .assertIsDisplayed()
    }

    @Test
    fun locationButton_showsChevronIcon_whenNotExpandable() {
        composeTestRule.setContent {
            LocationButton(
                locationName = "Kimberley",
                driverCount = 2,
                passengerCount = 1,
                isExpandable = false,
                onClick = {}
            )
        }

        composeTestRule
            .onNodeWithContentDescription("Navigate to Kimberley")
            .assertIsDisplayed()
    }

    @Test
    fun locationButton_triggersOnClick_whenPressed() {
        var clicked = false

        composeTestRule.setContent {
            LocationButton(
                locationName = "Nelspruit",
                driverCount = 5,
                passengerCount = 2,
                onClick = { clicked = true }
            )
        }

        composeTestRule
            .onNodeWithText("Nelspruit")
            .performClick()

        assert(clicked) { "onClick should have been triggered" }
    }

    @Test
    fun locationButton_triggersFavoriteToggle_whenStarPressed() {
        var favoriteToggled = false

        composeTestRule.setContent {
            LocationButton(
                locationName = "Rustenburg",
                driverCount = 3,
                passengerCount = 1,
                isFavorite = false,
                onClick = {},
                onFavoriteToggle = { favoriteToggled = true }
            )
        }

        composeTestRule
            .onNodeWithContentDescription("Add to favorites")
            .performClick()

        assert(favoriteToggled) { "onFavoriteToggle should have been triggered" }
    }

    @Test
    fun locationButton_handlesLargeCounts() {
        composeTestRule.setContent {
            LocationButton(
                locationName = "Mafikeng",
                driverCount = 999,
                passengerCount = 888,
                onClick = {}
            )
        }

        composeTestRule
            .onNodeWithText("999")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("888")
            .assertIsDisplayed()
    }

    @Test
    fun locationButton_handlesLongLocationNames() {
        val longLocationName = "Very Long Location Name That Might Overflow The Available Space"

        composeTestRule.setContent {
            LocationButton(
                locationName = longLocationName,
                driverCount = 5,
                passengerCount = 3,
                onClick = {}
            )
        }

        composeTestRule
            .onNodeWithText(longLocationName)
            .assertIsDisplayed()
    }

    @Test
    fun locationButton_handlesEmptyCityName() {
        composeTestRule.setContent {
            LocationButton(
                locationName = "Upington",
                cityName = "",
                driverCount = 2,
                passengerCount = 1,
                onClick = {}
            )
        }

        // Should only show location name when city name is empty
        composeTestRule
            .onNodeWithText("Upington")
            .assertIsDisplayed()
    }

    @Test
    fun locationButton_hasCorrectContentDescriptions() {
        composeTestRule.setContent {
            LocationButton(
                locationName = "Witbank",
                driverCount = 4,
                passengerCount = 2,
                onClick = {}
            )
        }

        composeTestRule
            .onNodeWithContentDescription("Passengers")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Drivers")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Navigate to Witbank")
            .assertIsDisplayed()
    }
}