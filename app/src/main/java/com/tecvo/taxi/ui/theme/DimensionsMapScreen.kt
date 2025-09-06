package com.tecvo.taxi.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Unified dimensions for all map screens (driver/passenger, town/local).
 *
 * This data class consolidates all dimension values previously spread across
 * multiple files to improve maintainability and consistency.
 */
data class MapScreenDimens(
    // ===== SPACING & LAYOUT =====
    /** Space at the top of the screen */
    val topSpacerHeight: Dp,
    /** Small gap spacing between elements */
    val smallSpacing: Dp,
    /** Medium gap spacing for padding in rows */
    val mediumSpacing: Dp,
    /** Large gap spacing between major components */
    val largeSpacing: Dp,
    /** Specific spacing after header row */
    val headerSpacing: Dp,
    /** Bottom padding for the bottom row */
    val bottomRowPadding: Dp,

    // ===== TYPOGRAPHY =====
    /** Font size for header texts (Town/Local) */
    val headerTextSize: TextUnit,
    /** Base font size used throughout the screen */
    val textSize: TextUnit,
    /** Font size for texts on map-type toggle buttons */
    val mapButtonTextSize: TextUnit,
    /** Font size for "Find Me" text */
    val findMeTextSize: TextUnit,
    /** Font size for "Home" text */
    val homeTextSize: TextUnit,

    // ===== HEADER ROW IMAGES =====
    /** Size of the passenger image on the header row */
    val passengerImageSize: Dp,
    /** Size of the minibus image on the header row */
    val minibusImageSize: Dp,

    // ===== MAP CONTROLS =====
    /** Size of the "Find Me" icon */
    val findMeIconSize: Dp,
    /** Size of the icon used to re-center the map */
    val mapIconSize: Dp,
    /** Size for map-type toggle button */
    val mapButtonSize: Dp,
    /** Base image size used for creating marker bitmaps */
    val imageSize: Dp,
    /** Height for standard buttons */
    val buttonHeight: Dp,

    // ===== PADDING & ALIGNMENT =====
    /** Padding for aligning elements at the top of the screen */
    val alignmentPadding: Dp,
    /** Padding inside background containers */
    val backgroundPadding: Dp,
    /** Padding around the text inside the map toggle button */
    val hybridTextPadding: Dp,
    /** Padding inside the re-center map button */
    val centerMapPadding: Dp,
    /** Padding for the badge icon */
    val badgePadding: Dp,

    // ===== VISUAL STYLING =====
    /** Standard corner radius for UI elements */
    val cornerRadius: Dp,
    /** Corner radius for the re-center map button */
    val centerMapRoundedCornerShape: Dp,
    /** Corner radius for the loading indicator */
    val loadingIndicatorCornerRadius: Dp,
    /** Size for the count badge */
    val badgeSize: Dp,
    /** Text size for the count badge */
    val badgeTextSize: TextUnit,
    /** Width of the edge-clickable area for back navigation */
    val edgeClickableWidth: Dp,

    // ===== VISUAL EFFECTS =====
    /** Default opacity for map markers */
    val markerAlpha: Float,
    /** Opacity for control backgrounds */
    val controlsBackgroundAlpha: Float,
    /** Opacity for loading indicator background */
    val loadingBackgroundAlpha: Float,
    /** Opacity for back navigation hint */
    val backNavigationAlpha: Float,

    // ===== FUNCTIONAL PARAMETERS =====
    /** Default search radius in kilometers */
    val defaultSearchRadius: Double,
    /** Default map zoom level */
    val defaultMapZoom: Float,
    /** Close-up map zoom level (for re-center) */
    val closeupMapZoom: Float,
    /** Animation duration for marker blinking (in milliseconds) */
    val markerAnimationDuration: Int
)

/**
 * Compact Small Dimensions:
 * For screens with width < 400dp (very small devices).
 */
val MapScreenCompactSmallDimens = MapScreenDimens(
    // Spacing & Layout
    topSpacerHeight = 40.dp,                   // Space at the top of the screen
    smallSpacing = 10.dp,                      // Small spacing between elements
    mediumSpacing = 10.dp,                     // Medium spacing between related elements
    largeSpacing = 24.dp,                      // Large spacing between major sections
    headerSpacing = 10.dp,                     // Spacing after header row
    bottomRowPadding = 18.dp,                  // Padding around the bottom row

    // Typography
    headerTextSize = 19.sp,                    // Size for header text
    textSize = 18.sp,                          // Base text size throughout the screen
    mapButtonTextSize = 16.sp,                 // Text size for map toggle button
    findMeTextSize = 16.sp,                    // Text size for "Find Me" element
    homeTextSize = 14.sp,                      // Text size for "Home" navigation element

    // Header Row Images
    passengerImageSize = 30.dp,                // Size of passenger icon in header
    minibusImageSize = 30.dp,                  // Size of minibus icon in header

    // Map Controls
    findMeIconSize = 40.dp,                    // Size of the location icon
    mapIconSize = 35.dp,                       // Size of the map re-center icon
    imageSize = 40.dp,                         // Size for map markers
    mapButtonSize = 30.dp,                     // Size of map type toggle button
    buttonHeight = 42.dp,                      // Height for standard buttons

    // Padding & Alignment
    alignmentPadding = 10.dp,                  // Padding for top alignment
    backgroundPadding = 10.dp,                 // Padding inside containers
    hybridTextPadding = 1.dp,                  // Padding around hybrid/normal text
    centerMapPadding = 1.dp,                   // Padding inside re-center button
    badgePadding = 8.dp,                       // Padding for the count badge

    // Visual Styling
    cornerRadius = 10.dp,                      // Standard corner radius for containers
    centerMapRoundedCornerShape = 2.dp,        // Corner radius for re-center button
    loadingIndicatorCornerRadius = 10.dp,      // Corner radius for loading indicator
    badgeSize = 24.dp,                         // Size of count badge
    badgeTextSize = 12.sp,                     // Text size for count badge
    edgeClickableWidth = 40.dp,                // Width of edge-clickable area

    // Visual Effects
    markerAlpha = 0.9f,                        // Opacity for map markers
    controlsBackgroundAlpha = 0.9f,            // Opacity for control backgrounds
    loadingBackgroundAlpha = 0.6f,             // Opacity for loading background
    backNavigationAlpha = 0.8f,                // Opacity for back navigation hint

    // Functional Parameters
    defaultSearchRadius = 0.5,                 // Default search radius in kilometers
    defaultMapZoom = 15.0f,                    // Default map zoom level
    closeupMapZoom = 15.5f,                    // Close-up map zoom level
    markerAnimationDuration = 500              // Duration for marker animation in ms
)

/**
 * Compact Medium Dimensions:
 * For screens with width between 400dp and 500dp (small to medium devices).
 */
val MapScreenCompactMediumDimens = MapScreenDimens(
    // Spacing & Layout
    topSpacerHeight = 70.dp,                   // Space at the top of the screen
    smallSpacing = 30.dp,                      // Small spacing between elements
    mediumSpacing = 10.dp,                     // Medium spacing between related elements
    largeSpacing = 26.dp,                      // Large spacing between major sections
    headerSpacing = 10.dp,                     // Spacing after header row
    bottomRowPadding = 18.dp,                  // Padding around the bottom row

    // Typography
    headerTextSize = 22.sp,                    // Size for header text
    textSize = 18.sp,                          // Base text size throughout the screen
    mapButtonTextSize = 16.sp,                 // Text size for map toggle button
    findMeTextSize = 16.sp,                    // Text size for "Find Me" element
    homeTextSize = 16.sp,                      // Text size for "Home" navigation element

    // Header Row Images
    passengerImageSize = 36.dp,                // Size of passenger icon in header
    minibusImageSize = 36.dp,                  // Size of minibus icon in header

    // Map Controls
    findMeIconSize = 45.dp,                    // Size of the location icon
    mapIconSize = 35.dp,                       // Size of the map re-center icon
    imageSize = 45.dp,                         // Size for map markers
    mapButtonSize = 30.dp,                     // Size of map type toggle button
    buttonHeight = 42.dp,                      // Height for standard buttons

    // Padding & Alignment
    alignmentPadding = 5.dp,                   // Padding for top alignment
    backgroundPadding = 10.dp,                 // Padding inside containers
    hybridTextPadding = 2.dp,                  // Padding around hybrid/normal text
    centerMapPadding = 2.dp,                   // Padding inside re-center button
    badgePadding = 8.dp,                       // Padding for the count badge

    // Visual Styling
    cornerRadius = 10.dp,                      // Standard corner radius for containers
    centerMapRoundedCornerShape = 10.dp,       // Corner radius for re-center button
    loadingIndicatorCornerRadius = 10.dp,      // Corner radius for loading indicator
    badgeSize = 24.dp,                         // Size of count badge
    badgeTextSize = 12.sp,                     // Text size for count badge
    edgeClickableWidth = 40.dp,                // Width of edge-clickable area

    // Visual Effects
    markerAlpha = 0.9f,                        // Opacity for map markers
    controlsBackgroundAlpha = 0.9f,            // Opacity for control backgrounds
    loadingBackgroundAlpha = 0.6f,             // Opacity for loading background
    backNavigationAlpha = 0.8f,                // Opacity for back navigation hint

    // Functional Parameters
    defaultSearchRadius = 0.5,                 // Default search radius in kilometers
    defaultMapZoom = 15.0f,                    // Default map zoom level
    closeupMapZoom = 15.5f,                    // Close-up map zoom level
    markerAnimationDuration = 500              // Duration for marker animation in ms
)

/**
 * Compact Dimensions:
 * For screens with width between 500dp and 600dp (typical larger phones).
 */
val MapScreenCompactDimens = MapScreenDimens(
    // Spacing & Layout
    topSpacerHeight = 20.dp,                   // Space at the top of the screen
    smallSpacing = 12.dp,                      // Small spacing between elements
    mediumSpacing = 20.dp,                     // Medium spacing between related elements
    largeSpacing = 28.dp,                      // Large spacing between major sections
    headerSpacing = 6.dp,                      // Spacing after header row
    bottomRowPadding = 20.dp,                  // Padding around the bottom row

    // Typography
    headerTextSize = 24.sp,                    // Size for header text
    textSize = 20.sp,                          // Base text size throughout the screen
    mapButtonTextSize = 18.sp,                 // Text size for map toggle button
    findMeTextSize = 18.sp,                    // Text size for "Find Me" element
    homeTextSize = 18.sp,                      // Text size for "Home" navigation element

    // Header Row Images
    passengerImageSize = 120.dp,               // Size of passenger icon in header
    minibusImageSize = 120.dp,                 // Size of minibus icon in header

    // Map Controls
    findMeIconSize = 28.dp,                    // Size of the location icon
    mapIconSize = 28.dp,                       // Size of the map re-center icon
    imageSize = 50.dp,                         // Size for map markers
    mapButtonSize = 50.dp,                     // Size of map type toggle button
    buttonHeight = 44.dp,                      // Height for standard buttons

    // Padding & Alignment
    alignmentPadding = 12.dp,                  // Padding for top alignment
    backgroundPadding = 12.dp,                 // Padding inside containers
    hybridTextPadding = 12.dp,                 // Padding around hybrid/normal text
    centerMapPadding = 12.dp,                  // Padding inside re-center button
    badgePadding = 8.dp,                       // Padding for the count badge

    // Visual Styling
    cornerRadius = 12.dp,                      // Standard corner radius for containers
    centerMapRoundedCornerShape = 12.dp,       // Corner radius for re-center button
    loadingIndicatorCornerRadius = 12.dp,      // Corner radius for loading indicator
    badgeSize = 24.dp,                         // Size of count badge
    badgeTextSize = 12.sp,                     // Text size for count badge
    edgeClickableWidth = 40.dp,                // Width of edge-clickable area

    // Visual Effects
    markerAlpha = 0.9f,                        // Opacity for map markers
    controlsBackgroundAlpha = 0.9f,            // Opacity for control backgrounds
    loadingBackgroundAlpha = 0.6f,             // Opacity for loading background
    backNavigationAlpha = 0.8f,                // Opacity for back navigation hint

    // Functional Parameters
    defaultSearchRadius = 0.5,                 // Default search radius in kilometers
    defaultMapZoom = 15.0f,                    // Default map zoom level
    closeupMapZoom = 15.5f,                    // Close-up map zoom level
    markerAnimationDuration = 500              // Duration for marker animation in ms
)

/**
 * Medium Dimensions:
 * For screens with width between 600dp and 840dp (small tablets or larger phones in landscape).
 */
val MapScreenMediumDimens = MapScreenDimens(
    // Spacing & Layout
    topSpacerHeight = 24.dp,                   // Space at the top of the screen
    smallSpacing = 14.dp,                      // Small spacing between elements
    mediumSpacing = 24.dp,                     // Medium spacing between related elements
    largeSpacing = 32.dp,                      // Large spacing between major sections
    headerSpacing = 6.dp,                      // Spacing after header row
    bottomRowPadding = 24.dp,                  // Padding around the bottom row

    // Typography
    headerTextSize = 26.sp,                    // Size for header text
    textSize = 22.sp,                          // Base text size throughout the screen
    mapButtonTextSize = 20.sp,                 // Text size for map toggle button
    findMeTextSize = 20.sp,                    // Text size for "Find Me" element
    homeTextSize = 20.sp,                      // Text size for "Home" navigation element

    // Header Row Images
    passengerImageSize = 130.dp,               // Size of passenger icon in header
    minibusImageSize = 130.dp,                 // Size of minibus icon in header

    // Map Controls
    findMeIconSize = 32.dp,                    // Size of the location icon
    mapIconSize = 32.dp,                       // Size of the map re-center icon
    imageSize = 55.dp,                         // Size for map markers
    mapButtonSize = 55.dp,                     // Size of map type toggle button
    buttonHeight = 46.dp,                      // Height for standard buttons

    // Padding & Alignment
    alignmentPadding = 14.dp,                  // Padding for top alignment
    backgroundPadding = 14.dp,                 // Padding inside containers
    hybridTextPadding = 14.dp,                 // Padding around hybrid/normal text
    centerMapPadding = 14.dp,                  // Padding inside re-center button
    badgePadding = 8.dp,                       // Padding for the count badge

    // Visual Styling
    cornerRadius = 14.dp,                      // Standard corner radius for containers
    centerMapRoundedCornerShape = 14.dp,       // Corner radius for re-center button
    loadingIndicatorCornerRadius = 14.dp,      // Corner radius for loading indicator
    badgeSize = 24.dp,                         // Size of count badge
    badgeTextSize = 12.sp,                     // Text size for count badge
    edgeClickableWidth = 40.dp,                // Width of edge-clickable area

    // Visual Effects
    markerAlpha = 0.9f,                        // Opacity for map markers
    controlsBackgroundAlpha = 0.9f,            // Opacity for control backgrounds
    loadingBackgroundAlpha = 0.6f,             // Opacity for loading background
    backNavigationAlpha = 0.8f,                // Opacity for back navigation hint

    // Functional Parameters
    defaultSearchRadius = 0.5,                 // Default search radius in kilometers
    defaultMapZoom = 15.0f,                    // Default map zoom level
    closeupMapZoom = 15.5f,                    // Close-up map zoom level
    markerAnimationDuration = 500              // Duration for marker animation in ms
)

/**
 * Expanded Dimensions:
 * For screens with width greater than 840dp (larger tablets or desktop-style layouts).
 */
val MapScreenExpandedDimens = MapScreenDimens(
    // Spacing & Layout
    topSpacerHeight = 28.dp,                   // Space at the top of the screen
    smallSpacing = 16.dp,                      // Small spacing between elements
    mediumSpacing = 28.dp,                     // Medium spacing between related elements
    largeSpacing = 36.dp,                      // Large spacing between major sections
    headerSpacing = 6.dp,                      // Spacing after header row
    bottomRowPadding = 28.dp,                  // Padding around the bottom row

    // Typography
    headerTextSize = 28.sp,                    // Size for header text
    textSize = 24.sp,                          // Base text size throughout the screen
    mapButtonTextSize = 22.sp,                 // Text size for map toggle button
    findMeTextSize = 22.sp,                    // Text size for "Find Me" element
    homeTextSize = 22.sp,                      // Text size for "Home" navigation element

    // Header Row Images
    passengerImageSize = 140.dp,               // Size of passenger icon in header
    minibusImageSize = 140.dp,                 // Size of minibus icon in header

    // Map Controls
    findMeIconSize = 36.dp,                    // Size of the location icon
    mapIconSize = 36.dp,                       // Size of the map re-center icon
    imageSize = 60.dp,                         // Size for map markers
    mapButtonSize = 60.dp,                     // Size of map type toggle button
    buttonHeight = 48.dp,                      // Height for standard buttons

    // Padding & Alignment
    alignmentPadding = 16.dp,                  // Padding for top alignment
    backgroundPadding = 16.dp,                 // Padding inside containers
    hybridTextPadding = 16.dp,                 // Padding around hybrid/normal text
    centerMapPadding = 16.dp,                  // Padding inside re-center button
    badgePadding = 8.dp,                       // Padding for the count badge

    // Visual Styling
    cornerRadius = 16.dp,                      // Standard corner radius for containers
    centerMapRoundedCornerShape = 16.dp,       // Corner radius for re-center button
    loadingIndicatorCornerRadius = 16.dp,      // Corner radius for loading indicator
    badgeSize = 24.dp,                         // Size of count badge
    badgeTextSize = 12.sp,                     // Text size for count badge
    edgeClickableWidth = 40.dp,                // Width of edge-clickable area

    // Visual Effects
    markerAlpha = 0.9f,                        // Opacity for map markers
    controlsBackgroundAlpha = 0.9f,            // Opacity for control backgrounds
    loadingBackgroundAlpha = 0.6f,             // Opacity for loading background
    backNavigationAlpha = 0.8f,                // Opacity for back navigation hint

    // Functional Parameters
    defaultSearchRadius = 0.5,                 // Default search radius in kilometers
    defaultMapZoom = 15.0f,                    // Default map zoom level
    closeupMapZoom = 15.5f,                    // Close-up map zoom level
    markerAnimationDuration = 500              // Duration for marker animation in ms
)