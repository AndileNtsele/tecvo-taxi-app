package com.tecvo.taxi.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import timber.log.Timber

/**
 * Utility class for detecting device type to enforce phone-only restrictions.
 *
 * This utility helps maintain the app's focus on mobile phone users,
 * which aligns with the target market of SA taxi drivers and commuters
 * who primarily use smartphones rather than tablets.
 *
 * Special handling for foldable phones: These devices are considered phones
 * regardless of their unfolded screen size, and will use phone-optimized
 * layouts constrained to phone dimensions even when unfolded.
 */
object DeviceTypeUtil {

    private const val TAG = "DeviceTypeUtil"

    /**
     * Checks if the current device is a foldable phone.
     * Foldable phones are always treated as phones, never as tablets,
     * even when their unfolded screen size exceeds typical phone dimensions.
     *
     * @param context Application context
     * @return true if device is a known foldable phone model
     */
    fun isFoldablePhone(context: Context): Boolean {
        val model = Build.MODEL.lowercase()
        val device = Build.DEVICE.lowercase()
        val manufacturer = Build.MANUFACTURER.lowercase()

        // Check for Samsung foldables
        if (manufacturer.contains("samsung")) {
            if (model.contains("fold") || device.contains("fold")) return true
            if (model.contains("flip") || device.contains("flip")) return true
            if (model.contains("sm-f")) return true // Samsung foldable model prefix
        }

        // Check for Google Pixel Fold
        if (manufacturer.contains("google")) {
            if (model.contains("fold") || device.contains("felix")) return true
        }

        // Check for OnePlus foldables
        if (manufacturer.contains("oneplus")) {
            if (model.contains("open") || model.contains("fold")) return true
        }

        // Check for Oppo foldables
        if (manufacturer.contains("oppo")) {
            if (model.contains("find n") || model.contains("fold")) return true
        }

        // Check for Xiaomi foldables
        if (manufacturer.contains("xiaomi")) {
            if (model.contains("mix fold") || model.contains("fold")) return true
        }

        // Check for Motorola foldables
        if (manufacturer.contains("motorola")) {
            if (model.contains("razr") || model.contains("fold")) return true
        }

        // Check for Huawei foldables
        if (manufacturer.contains("huawei")) {
            if (model.contains("mate x") || model.contains("fold")) return true
        }

        // Check for Honor foldables
        if (manufacturer.contains("honor")) {
            if (model.contains("magic v") || model.contains("fold")) return true
        }

        // Check for Vivo foldables
        if (manufacturer.contains("vivo")) {
            if (model.contains("x fold") || model.contains("fold")) return true
        }

        // Generic foldable detection
        if (model.contains("fold") || model.contains("flip") ||
            model.contains("razr") || device.contains("fold")) {
            return true
        }

        Timber.tag(TAG).d(
            "Device check - Manufacturer: $manufacturer, Model: $model, Device: $device - Is Foldable: false"
        )

        return false
    }

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
        // CRITICAL: Foldable phones are NEVER tablets, regardless of screen size
        // They should always use phone layouts constrained to phone dimensions
        if (isFoldablePhone(context)) {
            Timber.tag(TAG).d("Foldable phone detected - treating as phone regardless of screen size")
            return false
        }

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