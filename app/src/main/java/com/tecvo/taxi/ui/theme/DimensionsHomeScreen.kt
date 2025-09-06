package com.tecvo.taxi.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Data class that contains all dimensions for the HomeScreen.
 * Organized to follow the UI flow from top to bottom.
 */
data class HomeScreenDimens(
    // ===== SPACING BETWEEN SECTIONS =====
    /** Large vertical space used between major sections - e.g., between logo and "Who are you?" text */
    val bigSpacerHeight: Dp,
    /** Medium vertical space used between related elements - e.g., between "Who are you?" text and Passenger button */
    val mediumSpacerHeight: Dp,
    /** Small vertical space used for minimal separation - e.g., between Passenger button and "or" divider */
    val smallSpacerHeight: Dp,

    // ===== LOGO SECTION =====
    /** Size of the app logo (width and height) - for the main taxi app logo */
    val appLogoSize: Dp,
    /** Bottom padding specifically for the app logo - space under the logo image */
    val appLogoBottomPadding: Dp,

    // ===== TEXT =====
    /** Font size for text elements on the screen - e.g., "Who are you?", "Passenger", "Driver", "or" text */
    val textSize: TextUnit,

    // ===== BUTTONS =====
    /** Height of the Passenger and Driver buttons - standard height for both role selection buttons */
    val buttonHeight: Dp,
    /** Width of the Passenger and Driver buttons - standard width for both role selection buttons */
    val buttonWidth: Dp,
    /** Corner radius for rounded buttons - applies to Passenger and Driver buttons */
    val buttonCornerRadius: Dp,

    // ===== ICONS =====
    /** Size of the settings icon - for the gear icon in the bottom right corner */
    val settingsIconSize: Dp,
    /** Corner radius for settings icon background - rounded corners of the settings icon background */
    val settingsIconCornerRadius: Dp,

    // ===== LAYOUT PADDING =====
    /** Overall screen padding - applies to the main content Column */
    val screenPadding: Dp,
    /** Bottom padding for settings icon - distance from bottom edge to settings icon */
    val settingsBottomPadding: Dp,
    /** End (right) padding for settings icon - distance from right edge to settings icon */
    val settingsEndPadding: Dp,
    /** Padding for divider elements - horizontal spacing around the "or" text in divider */
    val dividerPadding: Dp,
)

/**
 * 1) HomeScreenCompactSmallDimens (width < 400dp)
 * Dimensions optimized for very small screens
 */
val HomeScreenCompactSmallDimens = HomeScreenDimens(
    // Spacing between sections
    bigSpacerHeight = 45.dp,              // Major section spacing (e.g., between logo and "Who are you?" text)
    mediumSpacerHeight = 22.dp,           // Medium spacing (e.g., between "Who are you?" text and Passenger button)
    smallSpacerHeight = 12.dp,            // Minimal spacing (e.g., between Passenger button and "or" divider)

    // Logo section
    appLogoSize = 180.dp,                 // Size of main taxi app logo image (170×170dp square)
    appLogoBottomPadding = 20.dp,         // Padding below the app logo (creates space between logo and text)

    // Text
    textSize = 18.sp,                     // Font size for "Who are you?", "Passenger", "Driver", and "or" text

    // Buttons
    buttonHeight = 60.dp,                 // Height for both Passenger and Driver buttons
    buttonWidth = 310.dp,                 // Width for both Passenger and Driver buttons
    buttonCornerRadius = 8.dp,            // Corner rounding for both Passenger and Driver buttons

    // Icons
    settingsIconSize = 30.dp,             // Size of settings gear icon in bottom right corner
    settingsIconCornerRadius = 12.dp,     // Corner radius for settings icon background

    // Layout padding
    screenPadding = 24.dp,                // Overall padding around the main content Column
    settingsBottomPadding = 36.dp,        // Bottom padding from screen edge to settings icon
    settingsEndPadding = 24.dp,           // Right padding from screen edge to settings icon
    dividerPadding = 8.dp,                // Padding around "or" text in divider
)

/**
 * 2) HomeScreenCompactMediumDimens (400dp <= width < 500dp)
 * Dimensions optimized for medium-small screens
 */
val HomeScreenCompactMediumDimens = HomeScreenDimens(
    // Spacing between sections
    bigSpacerHeight = 100.dp,             // Larger spacing (e.g., between logo and "Who are you?" text)
    mediumSpacerHeight = 19.dp,           // Medium spacing (e.g., between "Who are you?" text and Passenger button)
    smallSpacerHeight = 15.dp,            // Minimal spacing (e.g., between Passenger button and "or" divider)

    // Logo section
    appLogoSize = 200.dp,                 // Size of main taxi app logo image (200×200dp square)
    appLogoBottomPadding = 20.dp,         // Padding below the app logo (creates space between logo and text)

    // Text
    textSize = 20.sp,                     // Font size for "Who are you?", "Passenger", "Driver", and "or" text

    // Buttons
    buttonHeight = 70.dp,                 // Height for both Passenger and Driver buttons
    buttonWidth = 340.dp,                 // Width for both Passenger and Driver buttons
    buttonCornerRadius = 8.dp,            // Corner rounding for both Passenger and Driver buttons

    // Icons
    settingsIconSize = 30.dp,             // Size of settings gear icon in bottom right corner
    settingsIconCornerRadius = 12.dp,     // Corner radius for settings icon background

    // Layout padding
    screenPadding = 24.dp,                // Overall padding around the main content Column
    settingsBottomPadding = 36.dp,        // Bottom padding from screen edge to settings icon
    settingsEndPadding = 24.dp,           // Right padding from screen edge to settings icon
    dividerPadding = 8.dp,                // Padding around "or" text in divider
)

/**
 * 3) HomeScreenCompactDimens (500dp <= width < 600dp)
 * Dimensions optimized for standard mobile screens
 */
val HomeScreenCompactDimens = HomeScreenDimens(
    // Spacing between sections
    bigSpacerHeight = 50.dp,              // Larger spacing (e.g., between logo and "Who are you?" heading)
    mediumSpacerHeight = 24.dp,           // Medium spacing (e.g., between "Who are you?" heading and Passenger button)
    smallSpacerHeight = 14.dp,            // Small spacing (e.g., between Passenger button and "or" divider)

    // Logo section
    appLogoSize = 120.dp,                 // Size of main taxi app logo image (120×120dp square)
    appLogoBottomPadding = 20.dp,         // Padding below the app logo (space between logo and "Who are you?" text)

    // Text
    textSize = 22.sp,                     // Font size for "Who are you?", "Passenger", "Driver", and "or" text

    // Buttons
    buttonHeight = 44.dp,                 // Height for both Passenger and Driver buttons
    buttonWidth = 300.dp,                 // Width for both Passenger and Driver buttons
    buttonCornerRadius = 25.dp,           // Corner rounding radius for Passenger and Driver buttons

    // Icons
    settingsIconSize = 30.dp,             // Size of settings gear icon in bottom right corner
    settingsIconCornerRadius = 12.dp,     // Corner radius for settings icon background

    // Layout padding
    screenPadding = 24.dp,                // Overall padding around the main content Column
    settingsBottomPadding = 36.dp,        // Bottom padding from screen edge to settings icon
    settingsEndPadding = 24.dp,           // Right padding from screen edge to settings icon
    dividerPadding = 8.dp,                // Padding around "or" text in divider
)

/**
 * 4) HomeScreenMediumDimens (600dp <= width < 840dp)
 * Dimensions optimized for larger devices like small tablets
 */
val HomeScreenMediumDimens = HomeScreenDimens(
    // Spacing between sections
    bigSpacerHeight = 55.dp,              // Larger spacing (e.g., between logo and "Who are you?" heading)
    mediumSpacerHeight = 26.dp,           // Medium spacing (e.g., between "Who are you?" heading and Passenger button)
    smallSpacerHeight = 16.dp,            // Small spacing (e.g., between Passenger button and "or" divider)

    // Logo section
    appLogoSize = 130.dp,                 // Size of main taxi app logo image (130×130dp square)
    appLogoBottomPadding = 24.dp,         // Padding below the app logo (space between logo and "Who are you?" text)

    // Text
    textSize = 24.sp,                     // Font size for "Who are you?", "Passenger", "Driver", and "or" text

    // Buttons
    buttonHeight = 46.dp,                 // Height for both Passenger and Driver buttons
    buttonWidth = 340.dp,                 // Width for both Passenger and Driver buttons
    buttonCornerRadius = 25.dp,           // Corner rounding radius for Passenger and Driver buttons

    // Icons
    settingsIconSize = 30.dp,             // Size of settings gear icon in bottom right corner
    settingsIconCornerRadius = 12.dp,     // Corner radius for settings icon background

    // Layout padding
    screenPadding = 24.dp,                // Overall padding around the main content Column
    settingsBottomPadding = 36.dp,        // Bottom padding from screen edge to settings icon
    settingsEndPadding = 24.dp,           // Right padding from screen edge to settings icon
    dividerPadding = 8.dp,                // Padding around "or" text in divider
)

/**
 * 5) HomeScreenExpandedDimens (width >= 840dp)
 * Dimensions optimized for tablets and larger screens
 */
val HomeScreenExpandedDimens = HomeScreenDimens(
    // Spacing between sections
    bigSpacerHeight = 60.dp,              // Larger spacing (e.g., between logo and "Who are you?" heading)
    mediumSpacerHeight = 28.dp,           // Medium spacing (e.g., between "Who are you?" heading and Passenger button)
    smallSpacerHeight = 18.dp,            // Small spacing (e.g., between Passenger button and "or" divider)

    // Logo section
    appLogoSize = 140.dp,                 // Size of main taxi app logo image (140×140dp square)
    appLogoBottomPadding = 24.dp,         // Padding below the app logo (space between logo and "Who are you?" text)

    // Text
    textSize = 26.sp,                     // Font size for "Who are you?", "Passenger", "Driver", and "or" text

    // Buttons
    buttonHeight = 48.dp,                 // Height for both Passenger and Driver buttons
    buttonWidth = 400.dp,                 // Width for both Passenger and Driver buttons
    buttonCornerRadius = 25.dp,           // Corner rounding radius for Passenger and Driver buttons

    // Icons
    settingsIconSize = 40.dp,             // Size of settings gear icon in bottom right corner
    settingsIconCornerRadius = 12.dp,     // Corner radius for settings icon background

    // Layout padding
    screenPadding = 24.dp,                // Overall padding around the main content Column
    settingsBottomPadding = 36.dp,        // Bottom padding from screen edge to settings icon
    settingsEndPadding = 24.dp,           // Right padding from screen edge to settings icon
    dividerPadding = 8.dp,                // Padding around "or" text in divider
)