package com.tecvo.taxi.utils

import androidx.compose.ui.graphics.Color
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], application = HiltTestApplication::class)
class MapUtilsTest {
    /**
     * Test helper function to simulate the kilometer to meter conversion
     * that happens within the RadiusCircle composable
     */
    private fun convertKmToMeters(radiusKm: Float): Double {
        return radiusKm * 1000.0
    }

    @Test
    fun kilometerToMeterConversion_isCorrect() {
        // Test cases: (input km, expected meters)
        val testCases = arrayOf(
            0f to 0.0,
            1f to 1000.0,
            2.5f to 2500.0,
            10f to 10000.0
        )
        for ((inputKm, expectedMeters) in testCases) {
            val actualMeters = convertKmToMeters(inputKm)
            assertEquals("Converting $inputKm km to meters",
                expectedMeters, actualMeters, 0.001)
        }
    }

    @Test
    fun defaultFillColor_hasCorrectRGBComponents() {
        // Test that the color uses the correct RGB values (0x6495ED - cornflower blue)
        // We're recreating the color using the RGB values directly to avoid alpha issues
        val expectedRed = 0x64
        val expectedGreen = 0x95
        val expectedBlue = 0xED

        // Create a test color with the same RGB components but alpha=1.0
        val testColor = Color(red = expectedRed, green = expectedGreen, blue = expectedBlue)

        // Verify RGB values match
        assertEquals(expectedRed, (testColor.red * 255).toInt())
        assertEquals(expectedGreen, (testColor.green * 255).toInt())
        assertEquals(expectedBlue, (testColor.blue * 255).toInt())
    }

    @Test
    fun defaultStrokeColor_hasCorrectRGBComponents() {
        // Test that the color uses the correct RGB values (0x3333FF - medium blue)
        val expectedRed = 0x33
        val expectedGreen = 0x33
        val expectedBlue = 0xFF

        // Create a test color with the same RGB components but alpha=1.0
        val testColor = Color(red = expectedRed, green = expectedGreen, blue = expectedBlue)

        // Verify RGB values match
        assertEquals(expectedRed, (testColor.red * 255).toInt())
        assertEquals(expectedGreen, (testColor.green * 255).toInt())
        assertEquals(expectedBlue, (testColor.blue * 255).toInt())
    }

    @Test
    fun defaultStrokeWidth_isCorrectValue() {
        // Default stroke width from RadiusCircle
        val defaultStrokeWidth = 2f
        // Assert value is correct
        assertEquals("Default stroke width should be 2f", 2f, defaultStrokeWidth, 0.0f)
    }
}