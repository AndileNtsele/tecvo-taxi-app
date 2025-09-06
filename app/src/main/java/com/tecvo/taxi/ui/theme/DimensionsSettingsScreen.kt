// DimensionsSettingsScreen.kt
package com.tecvo.taxi.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Dimensions specific to the SettingsScreen.
 *
 * This data class holds all the adjustable values for spacing, typography,
 * and component sizes used in the SettingsScreen.
 */
data class SettingsScreenDimens(
    // Spacing and Padding
    val topPadding: Dp,         // Vertical padding at the top of the screen
    val sidePadding: Dp,        // Horizontal padding for the screen

    // Component Sizes
    val buttonHeight: Dp,       // Height of each settings button
    val iconSize: Dp,           // Size of the icons used in buttons
    val cornerRadius: Dp,       // Corner radius for the button shapes

    // Typography
    val titleTextSize: TextUnit, // Font size for the top app bar title
    val bodyTextSize: TextUnit,  // Font size for the button texts or body content

    // Additional spacing within a button
    val spacerWidth: Dp         // Spacing between the icon and text inside a button
)

/*
 * Example dimension values for different screen configurations:
 *
 * 1. SettingsScreenCompactSmallDimens:
 *    - Intended for screens with width less than 400dp.
 *    - Suitable for very small phones or devices in portrait mode.
 */
val SettingsScreenCompactSmallDimens = SettingsScreenDimens(
    topPadding = 8.dp,
    sidePadding = 8.dp,
    buttonHeight = 40.dp,
    iconSize = 20.dp,
    cornerRadius = 8.dp,
    titleTextSize = 18.sp,
    bodyTextSize = 14.sp,
    spacerWidth = 4.dp
)

/*
 * 2. SettingsScreenCompactMediumDimens:
 *    - Intended for screens with width between 400dp and 500dp.
 *    - Suitable for small to medium phones.
 */
val SettingsScreenCompactMediumDimens = SettingsScreenDimens(
    topPadding = 20.dp,
    sidePadding = 10.dp,
    buttonHeight = 52.dp,
    iconSize = 30.dp,
    cornerRadius = 25.dp,
    titleTextSize = 20.sp,
    bodyTextSize = 20.sp,
    spacerWidth = 8.dp
)

/*
 * 3. SettingsScreenCompactDimens:
 *    - Intended for screens with width between 500dp and 600dp.
 *    - Typical for larger phones.
 */
val SettingsScreenCompactDimens = SettingsScreenDimens(
    topPadding = 12.dp,
    sidePadding = 12.dp,
    buttonHeight = 44.dp,
    iconSize = 24.dp,
    cornerRadius = 12.dp,
    titleTextSize = 22.sp,
    bodyTextSize = 18.sp,
    spacerWidth = 8.dp
)

/*
 * 4. SettingsScreenMediumDimens:
 *    - Intended for screens with width between 600dp and 840dp.
 *    - Suitable for small tablets or larger phones in landscape mode.
 */
val SettingsScreenMediumDimens = SettingsScreenDimens(
    topPadding = 14.dp,
    sidePadding = 14.dp,
    buttonHeight = 46.dp,
    iconSize = 26.dp,
    cornerRadius = 14.dp,
    titleTextSize = 24.sp,
    bodyTextSize = 20.sp,
    spacerWidth = 10.dp
)

/*
 * 5. SettingsScreenExpandedDimens:
 *    - Intended for screens with width greater than 840dp.
 *    - Ideal for larger tablets or desktop-style layouts.
 */
val SettingsScreenExpandedDimens = SettingsScreenDimens(
    topPadding = 16.dp,
    sidePadding = 16.dp,
    buttonHeight = 48.dp,
    iconSize = 28.dp,
    cornerRadius = 16.dp,
    titleTextSize = 26.sp,
    bodyTextSize = 22.sp,
    spacerWidth = 12.dp
)
