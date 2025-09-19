package com.tecvo.taxi.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Unified dimensions for Driver and Passenger screens.
 *
 * This data class consolidates dimension values for both role screens to improve
 * maintainability and ensure consistent responsive behavior across the application.
 */
data class RoleScreenDimens(
    // ===== LAYOUT & SPACING =====
    /** Overall screen padding around the main content */
    val screenPadding: Dp,
    /** Small spacer height for minimal gaps between elements */
    val smallSpacerHeight: Dp,

    // ===== APP LOGO SECTION =====
    /** Size of the app logo image (width and height) */
    val logoImageSize: Dp,
    /** Padding below the app logo */
    val appLogoPadding: Dp,
    /** Spacer height after the app logo */
    val appLogoSpacerHeight: Dp,

    // ===== ROLE IMAGE SECTION =====
    /** Size for the role-specific image (driver/passenger) */
    val roleImageSize: Dp,
    /** Padding around the role image */
    val imagePadding: Dp,
    /** Spacer height after the role image */
    val imageSpacerHeight: Dp,

    // ===== BUTTON SECTION =====
    /** Width of all navigation buttons (Town, Local, Home) */
    val buttonWidth: Dp,
    /** Height of all navigation buttons */
    val buttonHeight: Dp,
    /** Corner radius for the buttons */
    val cornerRadius: Dp,
    /** Spacer height between buttons */
    val buttonSpacerHeight: Dp,

    // ===== TEXT =====
    /** Font size for button labels (Town, Local, Home) */
    val labelTextSize: TextUnit,

    // ===== VISUAL EFFECTS =====
    /** Background image opacity (0.0f to 1.0f) */
    val backgroundAlpha: Float
)

/**
 * Compact Small Dimensions:
 * For screens with width < 400dp (very small devices).
 */
val RoleScreenCompactSmallDimens = RoleScreenDimens(
    // Layout & Spacing
    screenPadding = 16.dp,                // Overall padding around screen content
    smallSpacerHeight = 20.dp,            // Small spacer between elements (e.g., at bottom of screen)

    // App Logo Section
    logoImageSize = 180.dp,               // Size of app logo image
    appLogoPadding = 10.dp,               // Padding below the app logo
    appLogoSpacerHeight = 70.dp,          // Space between logo and other elements

    // Role Image Section
    roleImageSize = 100.dp,               // Size of role-specific image
    imagePadding = 10.dp,                 // Padding around role image
    imageSpacerHeight = 18.dp,            // Space after role image

    // Button Section
    buttonWidth = 310.dp,                 // Width for navigation buttons
    buttonHeight = 50.dp,                 // Height for navigation buttons
    cornerRadius = 8.dp,                 // Corner radius for buttons
    buttonSpacerHeight = 30.dp,           // Space between buttons

    // Text
    labelTextSize = 20.sp,                // Font size for button labels

    // Visual Effects
    backgroundAlpha = 0.8f                // Background image opacity
)

/**
 * Compact Medium Dimensions:
 * For screens with width between 400dp and 500dp (small to medium devices).
 */
val RoleScreenCompactMediumDimens = RoleScreenDimens(
    // Layout & Spacing
    screenPadding = 16.dp,                // Overall padding around screen content
    smallSpacerHeight = 20.dp,            // Small spacer between elements (e.g., at bottom of screen)

    // App Logo Section
    logoImageSize = 200.dp,               // Size of app logo image
    appLogoPadding = 10.dp,               // Padding below the app logo
    appLogoSpacerHeight = 80.dp,          // Space between logo and other elements

    // Role Image Section
    roleImageSize = 100.dp,               // Size of role-specific image
    imagePadding = 10.dp,                 // Padding around role image
    imageSpacerHeight = 18.dp,            // Space after role image

    // Button Section
    buttonWidth = 340.dp,                 // Width for navigation buttons
    buttonHeight = 65.dp,                 // Height for navigation buttons
    cornerRadius = 8.dp,                  // Corner radius for buttons
    buttonSpacerHeight = 30.dp,           // Space between buttons

    // Text
    labelTextSize = 20.sp,                // Font size for button labels

    // Visual Effects
    backgroundAlpha = 0.8f                // Background image opacity
)

/**
 * Compact Dimensions:
 * For screens with width between 500dp and 600dp (typical larger phones).
 */
val RoleScreenCompactDimens = RoleScreenDimens(
    // Layout & Spacing
    screenPadding = 20.dp,                // Overall padding around screen content
    smallSpacerHeight = 16.dp,            // Small spacer between elements (e.g., at bottom of screen)

    // App Logo Section
    logoImageSize = 120.dp,               // Size of app logo image
    appLogoPadding = 12.dp,               // Padding below the app logo
    appLogoSpacerHeight = 16.dp,          // Space between logo and other elements

    // Role Image Section
    roleImageSize = 120.dp,               // Size of role-specific image
    imagePadding = 12.dp,                 // Padding around role image
    imageSpacerHeight = 20.dp,            // Space after role image

    // Button Section
    buttonWidth = 320.dp,                 // Width for navigation buttons
    buttonHeight = 54.dp,                 // Height for navigation buttons
    cornerRadius = 18.dp,                 // Corner radius for buttons
    buttonSpacerHeight = 24.dp,           // Space between buttons

    // Text
    labelTextSize = 20.sp,                // Font size for button labels

    // Visual Effects
    backgroundAlpha = 0.8f                // Background image opacity
)

/**
 * Medium Dimensions:
 * For screens with width between 600dp and 840dp (small tablets or larger phones in landscape).
 *
 * ⚠️ NOTE: UNUSED DUE TO TABLET RESTRICTIONS
 * This app now blocks tablets at runtime (see DeviceTypeUtil.isTablet()).
 * These dimensions are kept for build compatibility but will never be used.
 */
val RoleScreenMediumDimens = RoleScreenDimens(
    // Layout & Spacing
    screenPadding = 24.dp,                // Overall padding around screen content
    smallSpacerHeight = 16.dp,            // Small spacer between elements (e.g., at bottom of screen)

    // App Logo Section
    logoImageSize = 140.dp,               // Size of app logo image
    appLogoPadding = 14.dp,               // Padding below the app logo
    appLogoSpacerHeight = 18.dp,          // Space between logo and other elements

    // Role Image Section
    roleImageSize = 140.dp,               // Size of role-specific image
    imagePadding = 14.dp,                 // Padding around role image
    imageSpacerHeight = 22.dp,            // Space after role image

    // Button Section
    buttonWidth = 340.dp,                 // Width for navigation buttons
    buttonHeight = 56.dp,                 // Height for navigation buttons
    cornerRadius = 20.dp,                 // Corner radius for buttons
    buttonSpacerHeight = 26.dp,           // Space between buttons

    // Text
    labelTextSize = 22.sp,                // Font size for button labels

    // Visual Effects
    backgroundAlpha = 0.8f                // Background image opacity
)

/**
 * Expanded Dimensions:
 * For screens with width greater than 840dp (larger tablets or desktop-style layouts).
 *
 * ⚠️ NOTE: UNUSED DUE TO TABLET RESTRICTIONS
 * This app now blocks tablets at runtime (see DeviceTypeUtil.isTablet()).
 * These dimensions are kept for build compatibility but will never be used.
 */
val RoleScreenExpandedDimens = RoleScreenDimens(
    // Layout & Spacing
    screenPadding = 28.dp,                // Overall padding around screen content
    smallSpacerHeight = 16.dp,            // Small spacer between elements (e.g., at bottom of screen)

    // App Logo Section
    logoImageSize = 160.dp,               // Size of app logo image
    appLogoPadding = 16.dp,               // Padding below the app logo
    appLogoSpacerHeight = 20.dp,          // Space between logo and other elements

    // Role Image Section
    roleImageSize = 160.dp,               // Size of role-specific image
    imagePadding = 16.dp,                 // Padding around role image
    imageSpacerHeight = 24.dp,            // Space after role image

    // Button Section
    buttonWidth = 360.dp,                 // Width for navigation buttons
    buttonHeight = 60.dp,                 // Height for navigation buttons
    buttonSpacerHeight = 28.dp,           // Space between buttons
    cornerRadius = 22.dp,                 // Corner radius for buttons

    // Text
    labelTextSize = 24.sp,                // Font size for button labels

    // Visual Effects
    backgroundAlpha = 0.8f                // Background image opacity
)
