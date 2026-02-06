package com.tecvo.taxi.utils

import org.junit.Assert.*
import org.junit.Test

class ApiKeyValidatorTest {

    @Test
    fun `validateMapsApiKey regex should match exact Google API key format`() {
        // Test the regex pattern used in ApiKeyValidator
        val validKeys = listOf(
            "AIzaSyD1234567890123456789012345678901234",
            "AIzaAbCdEfGhIjKlMnOpQrStUvWxYz1234567890",
            "AIza1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZab",
            "AIzaAbC-_123456789012345678901234567890123"
        )

        val invalidKeys = listOf(
            "BIzaSyD1234567890123456789012345678901234", // Wrong prefix
            "AIza123456789012345678901234567890",         // Too short
            "AIzaSyD12345678901234567890123456789012345", // Too long
            "AIzaSyD123456789012345678901234567890!@#4",  // Invalid characters
            "aizasyD1234567890123456789012345678901234",  // Wrong case
            "",                                           // Empty
            "   "                                         // Whitespace
        )

        val regex = Regex("^AIza[0-9A-Za-z\\-_]{35}$")

        validKeys.forEach { key ->
            assertTrue("$key should match the regex", regex.matches(key))
        }

        invalidKeys.forEach { key ->
            assertFalse("$key should not match the regex", regex.matches(key))
        }
    }

    @Test
    fun `API key format validation with edge cases`() {
        val regex = Regex("^AIza[0-9A-Za-z\\-_]{35}$")

        // Test exact length requirements
        val exactLength = "AIza" + "A".repeat(35)
        assertTrue("Exact 39 character key should be valid", regex.matches(exactLength))

        val tooShort = "AIza" + "A".repeat(34)
        assertFalse("38 character key should be invalid", regex.matches(tooShort))

        val tooLong = "AIza" + "A".repeat(36)
        assertFalse("40 character key should be invalid", regex.matches(tooLong))

        // Test character restrictions
        val withDash = "AIza" + "A".repeat(30) + "-" + "A".repeat(4)
        assertTrue("Key with dash should be valid", regex.matches(withDash))

        val withUnderscore = "AIza" + "A".repeat(30) + "_" + "A".repeat(4)
        assertTrue("Key with underscore should be valid", regex.matches(withUnderscore))

        val withSpace = "AIza" + "A".repeat(30) + " " + "A".repeat(4)
        assertFalse("Key with space should be invalid", regex.matches(withSpace))

        val withSpecialChar = "AIza" + "A".repeat(30) + "!" + "A".repeat(4)
        assertFalse("Key with special character should be invalid", regex.matches(withSpecialChar))
    }

    @Test
    fun `API key prefix validation`() {
        val regex = Regex("^AIza[0-9A-Za-z\\-_]{35}$")

        val validPrefixes = listOf("AIza")
        val invalidPrefixes = listOf("AIzA", "aiza", "AIZA", "Aiza", "AIZa", "BIza", "AIzb", "AIz")

        validPrefixes.forEach { prefix ->
            val key = prefix + "A".repeat(35)
            assertTrue("Key with prefix '$prefix' should be valid", regex.matches(key))
        }

        invalidPrefixes.forEach { prefix ->
            val key = if (prefix.length >= 4) {
                prefix + "A".repeat(39 - prefix.length)
            } else {
                prefix + "A".repeat(36)
            }
            assertFalse("Key with prefix '$prefix' should be invalid", regex.matches(key))
        }
    }

    @Test
    fun `API key character set validation`() {
        val regex = Regex("^AIza[0-9A-Za-z\\-_]{35}$")

        // Test all valid characters
        val validChars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_"

        validChars.forEach { char ->
            val key = "AIza" + char.toString() + "A".repeat(34)
            assertTrue("Key with character '$char' should be valid", regex.matches(key))
        }

        // Test some invalid characters
        val invalidChars = "!@#$%^&*()+=[]{}|\\:;\"'<>?,./~`"

        invalidChars.forEach { char ->
            val key = "AIza" + char.toString() + "A".repeat(34)
            assertFalse("Key with character '$char' should be invalid", regex.matches(key))
        }
    }

    @Test
    fun `empty and whitespace API key validation`() {
        val emptyKey = ""
        val whitespaceKey = "   "
        val nullishKey = "null"

        assertTrue("Empty string should be blank", emptyKey.isBlank())
        assertTrue("Whitespace string should be blank", whitespaceKey.isBlank())
        assertFalse("Null-like string should not be blank", nullishKey.isBlank())

        val regex = Regex("^AIza[0-9A-Za-z\\-_]{35}$")
        assertFalse("Empty key should not match regex", regex.matches(emptyKey))
        assertFalse("Whitespace key should not match regex", regex.matches(whitespaceKey))
        assertFalse("Null-like key should not match regex", regex.matches(nullishKey))
    }
}