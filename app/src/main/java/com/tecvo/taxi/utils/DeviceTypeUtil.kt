package com.tecvo.taxi.utils

import android.content.Context
import android.content.res.Configuration
import timber.log.Timber

/**
 * Utility class for detecting device type to enforce phone-only restrictions.
 *
 * This utility helps maintain the app's focus on mobile phone users,
 * which aligns with the target market of SA taxi drivers and commuters
 * who primarily use smartphones rather than tablets.
 */
object DeviceTypeUtil {

    private const val TAG = "DeviceTypeUtil"

    /**
     * Checks if the current device is a tablet based on multiple criteria:
     * 1. Screen size category (large/xlarge)
     * 2. Smallest width configuration
     * 3. Screen density and dimensions
     *
     * @param context Application context
     * @return true if device is detected as a tablet, false for phones
     */
    fun isTablet(context: Context): Boolean {
        val configuration = context.resources.configuration
        val displayMetrics = context.resources.displayMetrics

        // Method 1: Check screen size category
        val screenLayout = configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
        val isLargeScreen = screenLayout == Configuration.SCREENLAYOUT_SIZE_LARGE ||
                           screenLayout == Configuration.SCREENLAYOUT_SIZE_XLARGE

        // Method 2: Check smallest width (tablets typically >= 600dp)
        val smallestWidthDp = configuration.smallestScreenWidthDp
        val isWideScreen = smallestWidthDp >= 600

        // Method 3: Calculate physical screen size
        val widthPixels = displayMetrics.widthPixels
        val heightPixels = displayMetrics.heightPixels
        val density = displayMetrics.density

        val widthDp = widthPixels / density
        val heightDp = heightPixels / density
        val smallestWidthCalculated = kotlin.math.min(widthDp, heightDp)

        // Method 4: Check diagonal size (tablets typically > 7 inches)
        val widthInches = widthPixels / displayMetrics.xdpi
        val heightInches = heightPixels / displayMetrics.ydpi
        val diagonalInches = kotlin.math.sqrt(widthInches * widthInches + heightInches * heightInches)
        val isLargeDiagonal = diagonalInches >= 7.0

        val isTablet = isLargeScreen || isWideScreen || smallestWidthCalculated >= 600 || isLargeDiagonal

        if (isTablet) {
            Timber.tag(TAG).w(
                "Tablet detected - screenLayout: $screenLayout, " +
                "smallestWidthDp: $smallestWidthDp, " +
                "calculatedWidthDp: $smallestWidthCalculated, " +
                "diagonal: ${String.format("%.1f", diagonalInches)}\"" +
                "density: $density"
            )
        } else {
            Timber.tag(TAG).d(
                "Phone detected - screenLayout: $screenLayout, " +
                "smallestWidthDp: $smallestWidthDp, " +
                "calculatedWidthDp: $smallestWidthCalculated, " +
                "diagonal: ${String.format("%.1f", diagonalInches)}\""
            )
        }

        return isTablet
    }

    /**
     * Gets a user-friendly device type description
     *
     * @param context Application context
     * @return "Tablet" or "Phone" based on device detection
     */
    fun getDeviceTypeDescription(context: Context): String {
        return if (isTablet(context)) "Tablet" else "Phone"
    }

    /**
     * Gets detailed device information for debugging
     *
     * @param context Application context
     * @return Formatted string with device specifications
     */
    fun getDeviceInfo(context: Context): String {
        val configuration = context.resources.configuration
        val displayMetrics = context.resources.displayMetrics

        val screenLayout = configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
        val smallestWidthDp = configuration.smallestScreenWidthDp
        val widthPixels = displayMetrics.widthPixels
        val heightPixels = displayMetrics.heightPixels
        val density = displayMetrics.density

        val widthDp = widthPixels / density
        val heightDp = heightPixels / density

        val widthInches = widthPixels / displayMetrics.xdpi
        val heightInches = heightPixels / displayMetrics.ydpi
        val diagonalInches = kotlin.math.sqrt(widthInches * widthInches + heightInches * heightInches)

        return buildString {
            appendLine("Device Type: ${getDeviceTypeDescription(context)}")
            appendLine("Screen Layout: $screenLayout")
            appendLine("Smallest Width: ${smallestWidthDp}dp")
            appendLine("Current Dimensions: ${widthDp.toInt()}dp x ${heightDp.toInt()}dp")
            appendLine("Pixel Dimensions: ${widthPixels}px x ${heightPixels}px")
            appendLine("Density: $density")
            appendLine("Diagonal Size: ${String.format("%.1f", diagonalInches)}\"")
        }
    }
}