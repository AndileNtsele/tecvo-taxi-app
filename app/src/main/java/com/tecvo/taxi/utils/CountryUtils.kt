package com.tecvo.taxi.utils
import com.tecvo.taxi.models.Country
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.Locale
object CountryUtils {
    private val phoneNumberUtil: PhoneNumberUtil by lazy {
        PhoneNumberUtil.getInstance()
    }
    /**
     * A list of all regions that libphonenumber supports,
     * mapped to your Country data class with name, dialCode, and flagEmoji.
     */
    val allCountries: List<Country> by lazy {
        val regionCodes = phoneNumberUtil.supportedRegions
        regionCodes.map { regionCode ->
            val callingCode = phoneNumberUtil.getCountryCodeForRegion(regionCode)
            val locale = Locale("", regionCode)
            val displayName = locale.displayCountry
            val flag = regionCodeToFlagEmoji(regionCode)
            Country(
                name = displayName,
                code = regionCode,
                dialCode = "+$callingCode",
                flagEmoji = flag
            )
        }.sortedBy { it.name }
    }
    // Add this at the top of your CountryUtils object, after the allCountries list
    val DEFAULT_COUNTRY = allCountries.find { it.name == "South Africa" } ?: allCountries.first()
    /**
     * Validates if a phone number is valid for a specific region code.
     */
    fun isValidPhoneNumber(
        phoneNumber: String,
        regionCode: String
    ): Boolean {
        return try {
            val proto = phoneNumberUtil.parse(phoneNumber, regionCode)
            phoneNumberUtil.isValidNumberForRegion(proto, regionCode)
        } catch (_: NumberParseException) {
            false
        }
    }
    /**
     * Overloaded version: Validates a phone number for a Country object.
     */
    fun isValidPhoneNumber(phoneNumber: String, country: Country): Boolean {
        return isValidPhoneNumber(phoneNumber, country.code)
    }
    /**
     * Formats a phone number into a specified format.
     * Supported formats: INTERNATIONAL, NATIONAL, E164, RFC3966.
     */
    fun formatPhoneNumber(
        phoneNumber: String,
        regionCode: String,
        format: PhoneNumberUtil.PhoneNumberFormat = PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL
    ): String {
        return try {
            val proto = phoneNumberUtil.parse(phoneNumber, regionCode)
            phoneNumberUtil.format(proto, format)
        } catch (_: NumberParseException) {
            phoneNumber
        }
    }
    /**
     * Overloaded version: Formats a phone number using a Country object.
     */
    fun formatPhoneNumber(
        phoneNumber: String,
        country: Country,
        format: PhoneNumberUtil.PhoneNumberFormat = PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL
    ): String {
        return formatPhoneNumber(phoneNumber, country.code, format)
    }
    /**
     * Formats a phone number in real-time as the user types.
     */
    fun formatAsYouType(phoneNumber: String, regionCode: String): String {
        val formatter = phoneNumberUtil.getAsYouTypeFormatter(regionCode)
        phoneNumber.forEach { formatter.inputDigit(it) }
        return formatter.toString()
    }
    /**
     * Converts a 2-character ISO region code into a flag emoji.
     */
    private fun regionCodeToFlagEmoji(regionCode: String): String {
        if (regionCode.length != 2) return "🌐" // Default fallback
        val uppercase = regionCode.uppercase(Locale.US)
        val firstLetter = uppercase[0].code - 0x41 + 0x1F1E6
        val secondLetter = uppercase[1].code - 0x41 + 0x1F1E6
        return String(Character.toChars(firstLetter)) +
                String(Character.toChars(secondLetter))
    }
    /**
     * Generates a local phone number example for a given country code.
     * Uses libphonenumber's example number functionality to get nationally formatted numbers.
     */
    fun getLocalNumberExample(countryCode: String): String {
        return try {
// Try to get an example mobile number for the country
            val exampleNumber = phoneNumberUtil.getExampleNumberForType(
                countryCode,
                PhoneNumberUtil.PhoneNumberType.MOBILE
            )
            if (exampleNumber != null) {
// Format the example number in national format (without country code)
                val nationalFormat = phoneNumberUtil.format(
                    exampleNumber,
                    PhoneNumberUtil.PhoneNumberFormat.NATIONAL
                )
// Return the nationally formatted number
                nationalFormat
            } else {
// Fallback to fixed line if mobile example isn't available
                val fixedLineExample = phoneNumberUtil.getExampleNumberForType(
                    countryCode,
                    PhoneNumberUtil.PhoneNumberType.FIXED_LINE
                )
                if (fixedLineExample != null) {
                    phoneNumberUtil.format(
                        fixedLineExample,
                        PhoneNumberUtil.PhoneNumberFormat.NATIONAL
                    )
                } else {
// Generic fallback if no examples are available
                    "Local phone number"
                }
            }
        } catch (_: Exception) {
// Provide a safe fallback
            "Local phone number"
        }
    }
    /**
     * Validates a phone number entered in local format (without country code)
     */
    fun isValidLocalPhoneNumber(phoneNumber: String, country: Country): Boolean {
        // Clean the input by removing spaces, hyphens, etc.
        val cleanNumber = phoneNumber.replace(Regex("[\\s-()]"), "")
        
        // Basic length check - allow reasonable range instead of strict validation
        if (cleanNumber.length < 7 || cleanNumber.length > 15) {
            return false
        }
        
        // Check if it contains only digits (after removing leading +)
        if (!cleanNumber.matches(Regex("^\\d+$"))) {
            return false
        }
        
        // Format for authentication - handle leading zero
        val formattedNumber = if (cleanNumber.startsWith("0")) {
            "${country.dialCode}${cleanNumber.substring(1)}"
        } else {
            "${country.dialCode}$cleanNumber"
        }
        
        // Use the existing validation logic with our properly formatted number
        return try {
            val proto = phoneNumberUtil.parse(formattedNumber, country.code)
            
            // Be more lenient - check if it's possible or valid
            phoneNumberUtil.isValidNumber(proto) || phoneNumberUtil.isPossibleNumber(proto)
        } catch (e: NumberParseException) {
            // Log the specific error for debugging but don't crash the app
            timber.log.Timber.w("Phone validation failed for $cleanNumber with country ${country.code}: ${e.message}")
            
            // Fallback: Allow if it looks like a reasonable phone number format
            when (country.code) {
                "ZA" -> cleanNumber.length >= 9 && cleanNumber.length <= 10 // South Africa mobile: 9-10 digits
                "US" -> cleanNumber.length == 10 // US: exactly 10 digits
                "GB" -> cleanNumber.length >= 10 && cleanNumber.length <= 11 // UK: 10-11 digits
                else -> cleanNumber.length >= 7 && cleanNumber.length <= 15 // General fallback
            }
        }
    }
}