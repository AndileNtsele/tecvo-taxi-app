package com.tecvo.taxi.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.junit.Rule
import org.junit.Test

class CountBadgeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun countBadge_displaysCorrectCount() {
        val testCount = 5

        composeTestRule.setContent {
            CountBadge(
                count = testCount,
                backgroundColor = Color.Red
            )
        }

        composeTestRule
            .onNodeWithText("5")
            .assertIsDisplayed()
    }

    @Test
    fun countBadge_displaysZeroCount() {
        composeTestRule.setContent {
            CountBadge(
                count = 0,
                backgroundColor = Color.Blue
            )
        }

        composeTestRule
            .onNodeWithText("0")
            .assertIsDisplayed()
    }

    @Test
    fun countBadge_displaysLargeCount() {
        val testCount = 999

        composeTestRule.setContent {
            CountBadge(
                count = testCount,
                backgroundColor = Color.Green
            )
        }

        composeTestRule
            .onNodeWithText("999")
            .assertIsDisplayed()
    }

    @Test
    fun countBadge_displaysNegativeCount() {
        val testCount = -5

        composeTestRule.setContent {
            CountBadge(
                count = testCount,
                backgroundColor = Color.Yellow
            )
        }

        composeTestRule
            .onNodeWithText("-5")
            .assertIsDisplayed()
    }

    @Test
    fun countBadge_handlesVeryLargeNumbers() {
        val testCount = 1000000

        composeTestRule.setContent {
            CountBadge(
                count = testCount,
                backgroundColor = Color.Magenta
            )
        }

        composeTestRule
            .onNodeWithText("1000000")
            .assertIsDisplayed()
    }

    @Test
    fun countBadge_withCustomSizeAndTextSize() {
        composeTestRule.setContent {
            CountBadge(
                count = 42,
                backgroundColor = Color.Cyan,
                badgeSize = 48.dp,
                textSize = 20.sp
            )
        }

        composeTestRule
            .onNodeWithText("42")
            .assertIsDisplayed()
    }

    @Test
    fun countBadge_withDifferentBackgroundColors() {
        // Test various background colors to ensure they don't affect text display
        val colors = listOf(
            Color.Red, Color.Blue, Color.Green, Color.Yellow,
            Color.Black, Color.White, Color.Gray, Color.Transparent
        )

        colors.forEachIndexed { index, color ->
            composeTestRule.setContent {
                CountBadge(
                    count = index,
                    backgroundColor = color
                )
            }

            composeTestRule
                .onNodeWithText(index.toString())
                .assertIsDisplayed()
        }
    }

    @Test
    fun countBadge_withMinimalSize() {
        composeTestRule.setContent {
            CountBadge(
                count = 1,
                backgroundColor = Color.Red,
                badgeSize = 1.dp,
                textSize = 1.sp
            )
        }

        composeTestRule
            .onNodeWithText("1")
            .assertIsDisplayed()
    }

    @Test
    fun countBadge_edgeCaseDoubleDigits() {
        listOf(10, 11, 99, 100).forEach { count ->
            composeTestRule.setContent {
                CountBadge(
                    count = count,
                    backgroundColor = Color.Blue
                )
            }

            composeTestRule
                .onNodeWithText(count.toString())
                .assertIsDisplayed()
        }
    }

    @Test
    fun countBadge_withDefaultParameters() {
        composeTestRule.setContent {
            CountBadge(
                count = 7,
                backgroundColor = Color.Red
            )
        }

        // Should work with default badgeSize (24.dp) and textSize (12.sp)
        composeTestRule
            .onNodeWithText("7")
            .assertIsDisplayed()
    }
}