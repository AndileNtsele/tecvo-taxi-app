package com.tecvo.taxi.utils

import com.tecvo.taxi.models.Country
import com.google.i18n.phonenumbers.PhoneNumberUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

class CountryUtilsTest {

    private lateinit var testCountry: Country

    @Before
    fun setup() {
        testCountry = Country(
            name = "South Africa",
            code = "ZA",
            dialCode = "+27",
            flagEmoji = "🇿🇦"
        )
    }

    @Test
    fun allCountries_isPopulated() {
        val countries = CountryUtils.allCountries
        assertFalse("Country list should not be empty", countries.isEmpty())
        assertTrue("Country list should contain more than 100 countries", countries.size > 100)
    }

    @Test
    fun defaultCountry_isSouthAfrica() {
        val defaultCountry = CountryUtils.DEFAULT_COUNTRY
        assertEquals("Default country name should be South Africa", "South Africa", defaultCountry.name)
        assertEquals("Default country dial code should be +27", "+27", defaultCountry.dialCode)
    }

    @Test
    fun isValidPhoneNumber_withValidNumber_returnsTrue() {
        val validNumber = "+27123456789"
        val result = CountryUtils.isValidPhoneNumber(validNumber, testCountry.code)
        assertTrue("Valid number should be validated as true", result)
    }

    @Test
    fun isValidPhoneNumber_withInvalidNumber_returnsFalse() {
        val invalidNumber = "+271234"
        val result = CountryUtils.isValidPhoneNumber(invalidNumber, testCountry.code)
        assertFalse("Invalid number should be validated as false", result)
    }

    @Test
    fun isValidPhoneNumber_withCountryObject_validatesCorrectly() {
        val validNumber = "+27123456789"
        val result = CountryUtils.isValidPhoneNumber(validNumber, testCountry)
        assertTrue("Valid number should be validated as true when using Country object", result)
    }

    @Test
    fun formatPhoneNumber_international_hasCorrectFormat() {
        val phoneNumber = "+27123456789"
        val result = CountryUtils.formatPhoneNumber(phoneNumber, testCountry.code)

        // Test the general format rather than specific substrings
        assertTrue("International format should include country code", result.contains("+27"))
        assertTrue("Formatted result should be different from input", result != phoneNumber)
        assertTrue("International format should include spaces or formatting", result.contains(" "))
    }

    @Test
    fun formatPhoneNumber_withCountryObject_formatsCorrectly() {
        val phoneNumber = "+27123456789"
        val result = CountryUtils.formatPhoneNumber(phoneNumber, testCountry)

        assertTrue("Formatted result should include country code", result.contains("+27"))
        assertTrue("Formatted result should be different from input", result != phoneNumber)
    }

    @Test
    fun formatPhoneNumber_national_hasCorrectFormat() {
        val phoneNumber = "+27123456789"
        val format = PhoneNumberUtil.PhoneNumberFormat.NATIONAL
        val result = CountryUtils.formatPhoneNumber(phoneNumber, testCountry.code, format)

        assertFalse("National format should not start with +", result.startsWith("+"))
        assertTrue("National format should include spaces or formatting", result.contains(" "))
    }

    @Test
    fun formatAsYouType_changesFormat() {
        val phoneNumber = "123456789"
        val result = CountryUtils.formatAsYouType(phoneNumber, testCountry.code)

        assertNotEquals("AsYouType formatting should change the input", phoneNumber, result)
    }

    @Test
    fun getLocalNumberExample_returnsValidExample() {
        val result = CountryUtils.getLocalNumberExample(testCountry.code)

        assertTrue("Example number should not be empty", result.isNotEmpty())
        assertNotEquals("Example should be a real number, not the fallback", "Local phone number", result)
        assertTrue("Example should contain digits", result.any { it.isDigit() })
    }

    @Test
    fun isValidLocalPhoneNumber_withValidNumber_returnsTrue() {
        val localNumber = "0123456789" // Local ZA number with leading 0
        val result = CountryUtils.isValidLocalPhoneNumber(localNumber, testCountry)

        assertTrue("Valid local number should be validated as true", result)
    }

    @Test
    fun isValidLocalPhoneNumber_withInvalidNumber_returnsFalse() {
        val invalidNumber = "12345" // Too short
        val result = CountryUtils.isValidLocalPhoneNumber(invalidNumber, testCountry)

        assertFalse("Invalid local number should be validated as false", result)
    }
}

/**
 * Parameterized tests for testing multiple country scenarios
 */
@RunWith(Parameterized::class)
class CountryUtilsParameterizedTest(
    private val countryCode: String,
    private val validNumber: String
) {

    @Test
    fun testFormatPhoneNumber_withDifferentCountries() {
        // Test international format
        val resultInternational = CountryUtils.formatPhoneNumber(
            validNumber,
            countryCode,
            PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL
        )
        assertTrue("International format should start with +", resultInternational.startsWith("+"))

        // Test national format
        val resultNational = CountryUtils.formatPhoneNumber(
            validNumber,
            countryCode,
            PhoneNumberUtil.PhoneNumberFormat.NATIONAL
        )
        assertFalse("National format should not start with +", resultNational.startsWith("+"))
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf("ZA", "+27123456789"),
                arrayOf("US", "+12125551234"),
                arrayOf("GB", "+447911123456"),
                arrayOf("AU", "+61412345678")
            )
        }
    }
}