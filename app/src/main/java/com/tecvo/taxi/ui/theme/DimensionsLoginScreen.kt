package com.tecvo.taxi.ui.theme


import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


/**
 * Dimensions specific to the LoginScreen.
 */
/**
 * All adjustable values for the LoginScreen and related dialogs.
 *
 * Categories:
 * 1. Spacing and Padding:
 *    - General spacings: extra small, small, medium, large
 *    - Screen edge and container paddings, gap between elements, list/grid/section spacings, safe area and keyboard spacings
 *    - Specific login screen spacings (small3, medium1, medium2)
 *
 * 2. Text Dimensions:
 *    - Font sizes for headings, toggles, labels, buttons, links, errors
 *    - Additional text properties: line heights, letter spacing, text padding, error text spacing
 *
 * 3. Component Sizes:
 *    - Logo size, text field height, button height/width, progress indicator size
 *
 * 4. Icons and Images:
 *    - Icon size and icon padding
 *
 * 5. Layout Dimensions:
 *    - Maximum content width and minimum touch target size
 *
 * 6. Animation Properties:
 *    - Animation durations and delays
 *
 * 7. Dialog and Modal Dimensions:
 *    - Dialog font sizes, dialog padding, dialog corner radius
 *
 * 8. Shapes and Corners:
 *    - Surface and text field corner radii, border widths, shadow elevation, outline thickness
 *
 * 9. Lists and Grids:
 *    - List header height, divider thickness, list indent, nested list padding
 *
 * 10. Form Elements:
 *     - Input field internal paddings, label spacing, helper and error text spacing, form group spacing,
 *       validation icon size, input append/prepend width, placeholder padding
 *
 * 11. Navigation Elements:
 *     - Navigation item heights, menu item heights, dropdown width, toolbar height, tab dimensions,
 *       navigation bar thickness, drawer width, menu padding
 *
 * 12. State-Based Dimensions:
 *     - Focus ring size, hover expansion, active scale, disabled opacity, success indicator size
 *
 * 13. Touch and Interaction:
 *     - Gesture thresholds, swipe thresholds, drag handle size, pull-to-refresh distance, scroll bar thickness, ripple radius
 *
 * Additional (from your extra checklist):
 *   - Password reset dialog icon size (passwordResetIconSize)
 *   - Country picker dialog max height (countryPickerDialogMaxHeight)
 */
data class LoginScreenDimens(
    // 1. Spacing and Padding
    val spacingExtraSmall: Dp,      // e.g. 4.dp – very small gaps (e.g. use for icon spacing)
    val spacingSmall: Dp,           // e.g. 8.dp – small gaps (e.g. between list items)
    val spacingMedium: Dp,          // e.g. 16.dp – medium gaps (e.g. between sections)
    val spacingLarge: Dp,           // e.g. 24.dp – large gaps (e.g. between major sections)
    val screenEdgePadding: Dp,      // e.g. 16.dp – global screen edge padding/margins
    val containerPadding: Dp,       // e.g. 16.dp – padding inside containers (cards, dialogs, etc.)
    val gapBetweenElements: Dp,     // e.g. 8.dp – gap between unrelated elements
    val listItemSpacing: Dp,        // e.g. 8.dp – spacing between list items
    val gridSpacing: Dp,            // e.g. 8.dp – spacing between grid items
    val sectionSpacing: Dp,         // e.g. 16.dp – spacing between sections
    val safeAreaPadding: Dp,        // e.g. 16.dp – safe area/inset padding
    val keyboardSpacing: Dp,        // e.g. 16.dp – additional spacing for keyboard adjustments
    // Specific login screen spacings:
    val small3: Dp,                 // e.g. 17.dp – vertical spacing between items in login screen
    val medium1: Dp,                // e.g. 20.dp – vertical padding for the top logo container
    val medium2: Dp,                // e.g. 30.dp – padding inside the login form Surface

    // 2. Text Dimensions
    val loginTextSize: TextUnit,    // e.g. 22.sp – font size for the main "Log in" title
    val toggleTextSize: TextUnit,   // e.g. 16.sp – font size for phone/email toggle texts
    val textFieldLabelSize: TextUnit, // e.g. 16.sp – font size for text field labels
    val buttonTextSize: TextUnit,   // e.g. 18.sp – font size for the login button text
    val linkTextSize: TextUnit,     // e.g. 16.sp – font size for "Forgot Password?" and "Sign Up" links
    val errorTextSize: TextUnit,    // e.g. 14.sp – font size for error messages
    val headingLineHeight: TextUnit, // e.g. 28.sp – line height for headings
    val bodyLineHeight: TextUnit,   // e.g. 20.sp – line height for body text
    val letterSpacing: TextUnit,    // e.g. 0.sp – default letter spacing
    val textPadding: Dp,            // e.g. 8.dp – general text padding
    val errorTextSpacing: Dp,       // e.g. 4.dp – spacing for error texts

    // 3. Component Sizes
    val logoSize: Dp,               // e.g. 100.dp – app logo image size
    val textFieldHeight: Dp,        // e.g. 56.dp – height of text fields
    val buttonHeight: Dp,           // e.g. 48.dp – height of buttons
    val buttonMinWidth: Dp,         // e.g. 64.dp – minimum width for buttons
    val progressIndicatorSize: Dp,  // e.g. 24.dp – size for circular progress indicators

    // 4. Icons and Images
    val iconSize: Dp,               // e.g. 24.dp – default icon size
    val iconPadding: Dp,            // e.g. 4.dp – padding around icons

    // 5. Layout Dimensions
    val maxContentWidth: Dp,        // e.g. 600.dp – maximum content width on larger screens
    val minTouchTargetSize: Dp,     // e.g. 48.dp – minimum touch target size

    // 6. Animation Properties (milliseconds)
    val animationDurationShort: Int, // e.g. 300ms – short duration animations
    val animationDurationMedium: Int, // e.g. 500ms – medium duration animations
    val animationDurationLong: Int,   // e.g. 800ms – long duration animations
    val animationDelayShort: Int,     // e.g. 100ms – short delay
    val animationDelayMedium: Int,    // e.g. 200ms – medium delay
    val animationDelayLong: Int,      // e.g. 300ms – long delay

    // 7. Dialog and Modal Dimensions
    val dialogTitleTextSize: TextUnit, // e.g. 20.sp – font size for dialog titles
    val dialogBodyTextSize: TextUnit,  // e.g. 16.sp – font size for dialog body texts
    val dialogButtonTextSize: TextUnit, // e.g. 18.sp – font size for dialog button texts
    val dialogPadding: Dp,             // e.g. 16.dp – padding inside dialogs
    val dialogCornerRadius: Dp,        // e.g. 12.dp – corner radius for dialogs

    // 8. Shapes and Corners
    val surfaceCornerRadius: Dp,    // e.g. 16.dp – corner radius for the login form Surface
    val textFieldCornerRadius: Dp,  // e.g. 10.dp – corner radius for text field backgrounds
    val borderWidth: Dp,            // e.g. 1.dp – standard border width
    val shadowElevation: Dp,        // e.g. 4.dp – shadow elevation for components
    val outlineThickness: Dp,       // e.g. 1.dp – thickness of outlines

    // 9. Lists and Grids
    val listHeaderHeight: Dp,       // e.g. 48.dp – height for list headers
    val dividerThickness: Dp,       // e.g. 1.dp – divider thickness
    val listIndent: Dp,             // e.g. 16.dp – indentation for list items
    val nestedListPadding: Dp,      // e.g. 8.dp – padding for nested lists

    // 10. Form Elements
    val textFieldHorizontalPadding: Dp, // e.g. 10.dp – horizontal padding inside text fields
    val textFieldLabelSpacing: Dp,  // e.g. 10.dp – spacing between label and text field input
    val helperTextSpacing: Dp,      // e.g. 4.dp – spacing for helper texts
    val formGroupSpacing: Dp,       // e.g. 12.dp – spacing between form groups
    val validationIconSize: Dp,     // e.g. 20.dp – size for validation icons
    val inputAppendWidth: Dp,       // e.g. 40.dp – width for appended/prepended inputs
    val placeholderPadding: Dp,     // e.g. 8.dp – padding for placeholder texts

    // 11. Navigation Elements
    val navItemHeight: Dp,          // e.g. 48.dp – height for navigation items
    val menuItemHeight: Dp,         // e.g. 48.dp – height for menu items
    val dropdownWidth: Dp,          // e.g. 200.dp – width for dropdown menus
    val toolbarHeight: Dp,          // e.g. 56.dp – toolbar height
    val tabHeight: Dp,              // e.g. 48.dp – tab height
    val tabWidth: Dp,               // e.g. 96.dp – tab width
    val navBarThickness: Dp,        // e.g. 2.dp – navigation bar thickness
    val drawerWidth: Dp,            // e.g. 280.dp – navigation drawer width
    val menuPadding: Dp,            // e.g. 16.dp – padding for menu items

    // 12. State-Based Dimensions
    val focusRingSize: Dp,          // e.g. 2.dp – size of the focus ring
    val hoverExpansion: Dp,         // e.g. 4.dp – extra expansion on hover
    val activeScale: Float,         // e.g. 0.98f – scale factor for active state
    val disabledOpacity: Float,     // e.g. 0.5f – opacity for disabled state
    val successIndicatorSize: Dp,   // e.g. 24.dp – size for success indicators

    // 13. Touch and Interaction
    val gestureThreshold: Dp,       // e.g. 8.dp – gesture threshold distance
    val swipeThreshold: Dp,         // e.g. 16.dp – swipe action threshold
    val dragHandleSize: Dp,         // e.g. 24.dp – drag handle size
    val pullToRefreshDistance: Dp,  // e.g. 80.dp – pull-to-refresh distance
    val scrollBarThickness: Dp,     // e.g. 4.dp – scroll bar thickness
    val rippleRadius: Dp,           // e.g. 24.dp – touch ripple radius

    // Additional dimensions from your checklist:
    val passwordResetIconSize: Dp,  // e.g. 48.dp – icon size for the password reset dialog
    val countryPickerDialogMaxHeight: Dp // e.g. 300.dp – maximum height for the country picker dialog
)

val LoginScreenCompactSmallDimens = LoginScreenDimens(
    // 1. Spacing and Padding
    spacingExtraSmall = 4.dp,            // very small gaps (use for icon spacing, e.g. in phone field)
    spacingSmall = 8.dp,                 // small gaps (e.g. between list items)
    spacingMedium = 16.dp,               // medium gaps (e.g. between sections)
    spacingLarge = 24.dp,                // large gaps (e.g. between major sections)
    screenEdgePadding = 16.dp,           // global screen edge padding
    containerPadding = 16.dp,            // padding within containers (cards, dialogs)
    gapBetweenElements = 8.dp,           // gap between unrelated elements
    listItemSpacing = 8.dp,              // spacing between list items
    gridSpacing = 8.dp,                  // spacing between grid items
    sectionSpacing = 16.dp,              // spacing between sections
    safeAreaPadding = 16.dp,             // safe area/inset padding
    keyboardSpacing = 16.dp,             // extra spacing for keyboard adjustments
    small3 = 17.dp,                      // vertical spacing between items in the login screen
    medium1 = 20.dp,                     // vertical padding for the top logo container
    medium2 = 30.dp,                     // padding inside the login form Surface

    // 2. Text Dimensions
    loginTextSize = 22.sp,               // "Log in" title font size
    toggleTextSize = 19.sp,              // Phone/email toggle text font size
    textFieldLabelSize = 16.sp,          // Text field label font size
    buttonTextSize = 18.sp,              // Login button font size
    linkTextSize = 16.sp,                // "Forgot Password?" and "Sign Up" link text font size
    errorTextSize = 14.sp,               // Error text font size
    headingLineHeight = 28.sp,           // Heading line height
    bodyLineHeight = 20.sp,              // Body text line height
    letterSpacing = 0.sp,                // Default letter spacing
    textPadding = 8.dp,                  // General text padding
    errorTextSpacing = 4.dp,             // Spacing for error texts

    // 3. Component Sizes
    logoSize = 140.dp,                   // App logo size
    textFieldHeight = 56.dp,             // Text field height
    buttonHeight = 48.dp,                // Button height
    buttonMinWidth = 64.dp,              // Minimum button width
    progressIndicatorSize = 24.dp,       // Progress indicator size

    // 4. Icons and Images
    iconSize = 24.dp,                    // Default icon size
    iconPadding = 4.dp,                  // Icon padding

    // 5. Layout Dimensions
    maxContentWidth = 600.dp,            // Maximum content width
    minTouchTargetSize = 48.dp,          // Minimum touch target size

    // 6. Animation Properties
    animationDurationShort = 300,        // 300ms for short animations
    animationDurationMedium = 500,       // 500ms for medium animations
    animationDurationLong = 800,         // 800ms for long animations
    animationDelayShort = 100,           // 100ms short delay
    animationDelayMedium = 200,          // 200ms medium delay
    animationDelayLong = 300,            // 300ms long delay

    // 7. Dialog and Modal Dimensions
    dialogTitleTextSize = 20.sp,         // Dialog title font size
    dialogBodyTextSize = 16.sp,          // Dialog body font size
    dialogButtonTextSize = 18.sp,        // Dialog button font size
    dialogPadding = 16.dp,               // Padding inside dialogs
    dialogCornerRadius = 12.dp,          // Dialog corner radius

    // 8. Shapes and Corners
    surfaceCornerRadius = 16.dp,         // Surface corner radius for login form
    textFieldCornerRadius = 10.dp,       // Text field background corner radius
    borderWidth = 1.dp,                  // Standard border width
    shadowElevation = 4.dp,              // Shadow elevation for components
    outlineThickness = 1.dp,             // Outline thickness

    // 9. Lists and Grids
    listHeaderHeight = 48.dp,            // List header height
    dividerThickness = 1.dp,             // Divider thickness
    listIndent = 16.dp,                  // List indentation
    nestedListPadding = 8.dp,            // Padding for nested lists

    // 10. Form Elements
    textFieldHorizontalPadding = 10.dp,  // Horizontal padding inside text fields
    textFieldLabelSpacing = 10.dp,       // Spacing between text field label and input
    helperTextSpacing = 4.dp,            // Spacing for helper texts
    formGroupSpacing = 12.dp,            // Spacing between form groups
    validationIconSize = 20.dp,          // Size for validation icons
    inputAppendWidth = 40.dp,            // Width for appended inputs
    placeholderPadding = 8.dp,           // Padding for placeholder texts

    // 11. Navigation Elements
    navItemHeight = 48.dp,               // Navigation item height
    menuItemHeight = 48.dp,              // Menu item height
    dropdownWidth = 200.dp,              // Dropdown width
    toolbarHeight = 56.dp,               // Toolbar height
    tabHeight = 48.dp,                   // Tab height
    tabWidth = 96.dp,                    // Tab width
    navBarThickness = 2.dp,              // Navigation bar thickness
    drawerWidth = 280.dp,                // Navigation drawer width
    menuPadding = 16.dp,                 // Menu item padding

    // 12. State-Based Dimensions
    focusRingSize = 2.dp,                // Focus ring size
    hoverExpansion = 4.dp,               // Hover expansion value
    activeScale = 0.98f,                 // Active state scale factor
    disabledOpacity = 0.5f,              // Disabled state opacity
    successIndicatorSize = 24.dp,        // Success indicator size

    // 13. Touch and Interaction
    gestureThreshold = 8.dp,             // Gesture threshold distance
    swipeThreshold = 16.dp,              // Swipe action threshold
    dragHandleSize = 24.dp,              // Drag handle size
    pullToRefreshDistance = 80.dp,       // Pull-to-refresh distance
    scrollBarThickness = 4.dp,           // Scroll bar thickness
    rippleRadius = 24.dp,                // Touch ripple radius

    // Additional dimensions from your checklist:
    passwordResetIconSize = 48.dp,       // Icon size for the password reset dialog (was hardcoded as 48.dp)
    countryPickerDialogMaxHeight = 300.dp // Maximum height for the country picker dialog (was hardcoded as 300.dp)
)

val LoginScreenCompactMediumDimens = LoginScreenDimens(
    // 1. Spacing and Padding
    spacingExtraSmall = 4.dp,            // very small gaps (use for icon spacing, e.g. in phone field)
    spacingSmall = 8.dp,                 // small gaps (e.g. between list items)
    spacingMedium = 16.dp,               // medium gaps (e.g. between sections)
    spacingLarge = 24.dp,                // large gaps (e.g. between major sections)
    screenEdgePadding = 16.dp,           // global screen edge padding
    containerPadding = 16.dp,            // padding within containers (cards, dialogs)
    gapBetweenElements = 8.dp,           // gap between unrelated elements
    listItemSpacing = 8.dp,              // spacing between list items
    gridSpacing = 8.dp,                  // spacing between grid items
    sectionSpacing = 16.dp,              // spacing between sections
    safeAreaPadding = 16.dp,             // safe area/inset padding
    keyboardSpacing = 16.dp,             // extra spacing for keyboard adjustments
    small3 = 17.dp,                      // vertical spacing between items in the login screen
    medium1 = 20.dp,                     // vertical padding for the top logo container
    medium2 = 30.dp,                     // padding inside the login form Surface

    // 2. Text Dimensions
    loginTextSize = 22.sp,               // "Log in" title font size
    toggleTextSize = 19.sp,              // Phone/email toggle text font size
    textFieldLabelSize = 16.sp,          // Text field label font size
    buttonTextSize = 18.sp,              // Login button font size
    linkTextSize = 16.sp,                // "Forgot Password?" and "Sign Up" link text font size
    errorTextSize = 14.sp,               // Error text font size
    headingLineHeight = 28.sp,           // Heading line height
    bodyLineHeight = 20.sp,              // Body text line height
    letterSpacing = 0.sp,                // Default letter spacing
    textPadding = 8.dp,                  // General text padding
    errorTextSpacing = 4.dp,             // Spacing for error texts

    // 3. Component Sizes
    logoSize = 140.dp,                   // App logo size
    textFieldHeight = 56.dp,             // Text field height
    buttonHeight = 48.dp,                // Button height
    buttonMinWidth = 64.dp,              // Minimum button width
    progressIndicatorSize = 24.dp,       // Progress indicator size

    // 4. Icons and Images
    iconSize = 24.dp,                    // Default icon size
    iconPadding = 4.dp,                  // Icon padding

    // 5. Layout Dimensions
    maxContentWidth = 600.dp,            // Maximum content width
    minTouchTargetSize = 48.dp,          // Minimum touch target size

    // 6. Animation Properties
    animationDurationShort = 300,        // 300ms for short animations
    animationDurationMedium = 500,       // 500ms for medium animations
    animationDurationLong = 800,         // 800ms for long animations
    animationDelayShort = 100,           // 100ms short delay
    animationDelayMedium = 200,          // 200ms medium delay
    animationDelayLong = 300,            // 300ms long delay

    // 7. Dialog and Modal Dimensions
    dialogTitleTextSize = 20.sp,         // Dialog title font size
    dialogBodyTextSize = 16.sp,          // Dialog body font size
    dialogButtonTextSize = 18.sp,        // Dialog button font size
    dialogPadding = 16.dp,               // Padding inside dialogs
    dialogCornerRadius = 12.dp,          // Dialog corner radius

    // 8. Shapes and Corners
    surfaceCornerRadius = 16.dp,         // Surface corner radius for login form
    textFieldCornerRadius = 10.dp,       // Text field background corner radius
    borderWidth = 1.dp,                  // Standard border width
    shadowElevation = 4.dp,              // Shadow elevation for components
    outlineThickness = 1.dp,             // Outline thickness

    // 9. Lists and Grids
    listHeaderHeight = 48.dp,            // List header height
    dividerThickness = 1.dp,             // Divider thickness
    listIndent = 16.dp,                  // List indentation
    nestedListPadding = 8.dp,            // Padding for nested lists

    // 10. Form Elements
    textFieldHorizontalPadding = 10.dp,  // Horizontal padding inside text fields
    textFieldLabelSpacing = 10.dp,       // Spacing between text field label and input
    helperTextSpacing = 4.dp,            // Spacing for helper texts
    formGroupSpacing = 12.dp,            // Spacing between form groups
    validationIconSize = 20.dp,          // Size for validation icons
    inputAppendWidth = 40.dp,            // Width for appended inputs
    placeholderPadding = 8.dp,           // Padding for placeholder texts

    // 11. Navigation Elements
    navItemHeight = 48.dp,               // Navigation item height
    menuItemHeight = 48.dp,              // Menu item height
    dropdownWidth = 200.dp,              // Dropdown width
    toolbarHeight = 56.dp,               // Toolbar height
    tabHeight = 48.dp,                   // Tab height
    tabWidth = 96.dp,                    // Tab width
    navBarThickness = 2.dp,              // Navigation bar thickness
    drawerWidth = 280.dp,                // Navigation drawer width
    menuPadding = 16.dp,                 // Menu item padding

    // 12. State-Based Dimensions
    focusRingSize = 2.dp,                // Focus ring size
    hoverExpansion = 4.dp,               // Hover expansion value
    activeScale = 0.98f,                 // Active state scale factor
    disabledOpacity = 0.5f,              // Disabled state opacity
    successIndicatorSize = 24.dp,        // Success indicator size

    // 13. Touch and Interaction
    gestureThreshold = 8.dp,             // Gesture threshold distance
    swipeThreshold = 16.dp,              // Swipe action threshold
    dragHandleSize = 24.dp,              // Drag handle size
    pullToRefreshDistance = 80.dp,       // Pull-to-refresh distance
    scrollBarThickness = 4.dp,           // Scroll bar thickness
    rippleRadius = 24.dp,                // Touch ripple radius

    // Additional dimensions from your checklist:
    passwordResetIconSize = 48.dp,       // Icon size for the password reset dialog (was hardcoded as 48.dp)
    countryPickerDialogMaxHeight = 300.dp // Maximum height for the country picker dialog (was hardcoded as 300.dp)
)

val LoginScreenCompactDimens = LoginScreenDimens(
    // 1. Spacing and Padding
    spacingExtraSmall = 4.dp,            // very small gaps (use for icon spacing, e.g. in phone field)
    spacingSmall = 8.dp,                 // small gaps (e.g. between list items)
    spacingMedium = 16.dp,               // medium gaps (e.g. between sections)
    spacingLarge = 24.dp,                // large gaps (e.g. between major sections)
    screenEdgePadding = 16.dp,           // global screen edge padding
    containerPadding = 16.dp,            // padding within containers (cards, dialogs)
    gapBetweenElements = 8.dp,           // gap between unrelated elements
    listItemSpacing = 8.dp,              // spacing between list items
    gridSpacing = 8.dp,                  // spacing between grid items
    sectionSpacing = 16.dp,              // spacing between sections
    safeAreaPadding = 16.dp,             // safe area/inset padding
    keyboardSpacing = 16.dp,             // extra spacing for keyboard adjustments
    small3 = 17.dp,                      // vertical spacing between items in the login screen
    medium1 = 20.dp,                     // vertical padding for the top logo container
    medium2 = 30.dp,                     // padding inside the login form Surface

    // 2. Text Dimensions
    loginTextSize = 22.sp,               // "Log in" title font size
    toggleTextSize = 16.sp,              // Phone/email toggle text font size
    textFieldLabelSize = 16.sp,          // Text field label font size
    buttonTextSize = 18.sp,              // Login button font size
    linkTextSize = 16.sp,                // "Forgot Password?" and "Sign Up" link text font size
    errorTextSize = 14.sp,               // Error text font size
    headingLineHeight = 28.sp,           // Heading line height
    bodyLineHeight = 20.sp,              // Body text line height
    letterSpacing = 0.sp,                // Default letter spacing
    textPadding = 8.dp,                  // General text padding
    errorTextSpacing = 4.dp,             // Spacing for error texts

    // 3. Component Sizes
    logoSize = 100.dp,                   // App logo size
    textFieldHeight = 56.dp,             // Text field height
    buttonHeight = 48.dp,                // Button height
    buttonMinWidth = 64.dp,              // Minimum button width
    progressIndicatorSize = 24.dp,       // Progress indicator size

    // 4. Icons and Images
    iconSize = 24.dp,                    // Default icon size
    iconPadding = 4.dp,                  // Icon padding

    // 5. Layout Dimensions
    maxContentWidth = 600.dp,            // Maximum content width
    minTouchTargetSize = 48.dp,          // Minimum touch target size

    // 6. Animation Properties
    animationDurationShort = 300,        // 300ms for short animations
    animationDurationMedium = 500,       // 500ms for medium animations
    animationDurationLong = 800,         // 800ms for long animations
    animationDelayShort = 100,           // 100ms short delay
    animationDelayMedium = 200,          // 200ms medium delay
    animationDelayLong = 300,            // 300ms long delay

    // 7. Dialog and Modal Dimensions
    dialogTitleTextSize = 20.sp,         // Dialog title font size
    dialogBodyTextSize = 16.sp,          // Dialog body font size
    dialogButtonTextSize = 18.sp,        // Dialog button font size
    dialogPadding = 16.dp,               // Padding inside dialogs
    dialogCornerRadius = 12.dp,          // Dialog corner radius

    // 8. Shapes and Corners
    surfaceCornerRadius = 16.dp,         // Surface corner radius for login form
    textFieldCornerRadius = 10.dp,       // Text field background corner radius
    borderWidth = 1.dp,                  // Standard border width
    shadowElevation = 4.dp,              // Shadow elevation for components
    outlineThickness = 1.dp,             // Outline thickness

    // 9. Lists and Grids
    listHeaderHeight = 48.dp,            // List header height
    dividerThickness = 1.dp,             // Divider thickness
    listIndent = 16.dp,                  // List indentation
    nestedListPadding = 8.dp,            // Padding for nested lists

    // 10. Form Elements
    textFieldHorizontalPadding = 10.dp,  // Horizontal padding inside text fields
    textFieldLabelSpacing = 10.dp,       // Spacing between text field label and input
    helperTextSpacing = 4.dp,            // Spacing for helper texts
    formGroupSpacing = 12.dp,            // Spacing between form groups
    validationIconSize = 20.dp,          // Size for validation icons
    inputAppendWidth = 40.dp,            // Width for appended inputs
    placeholderPadding = 8.dp,           // Padding for placeholder texts

    // 11. Navigation Elements
    navItemHeight = 48.dp,               // Navigation item height
    menuItemHeight = 48.dp,              // Menu item height
    dropdownWidth = 200.dp,              // Dropdown width
    toolbarHeight = 56.dp,               // Toolbar height
    tabHeight = 48.dp,                   // Tab height
    tabWidth = 96.dp,                    // Tab width
    navBarThickness = 2.dp,              // Navigation bar thickness
    drawerWidth = 280.dp,                // Navigation drawer width
    menuPadding = 16.dp,                 // Menu item padding

    // 12. State-Based Dimensions
    focusRingSize = 2.dp,                // Focus ring size
    hoverExpansion = 4.dp,               // Hover expansion value
    activeScale = 0.98f,                 // Active state scale factor
    disabledOpacity = 0.5f,              // Disabled state opacity
    successIndicatorSize = 24.dp,        // Success indicator size

    // 13. Touch and Interaction
    gestureThreshold = 8.dp,             // Gesture threshold distance
    swipeThreshold = 16.dp,              // Swipe action threshold
    dragHandleSize = 24.dp,              // Drag handle size
    pullToRefreshDistance = 80.dp,       // Pull-to-refresh distance
    scrollBarThickness = 4.dp,           // Scroll bar thickness
    rippleRadius = 24.dp,                // Touch ripple radius

    // Additional dimensions from your checklist:
    passwordResetIconSize = 48.dp,       // Icon size for the password reset dialog (was hardcoded as 48.dp)
    countryPickerDialogMaxHeight = 300.dp // Maximum height for the country picker dialog (was hardcoded as 300.dp)
)

val LoginScreenMediumDimens = LoginScreenDimens(
    // 1. Spacing and Padding
    spacingExtraSmall = 4.dp,            // very small gaps (use for icon spacing, e.g. in phone field)
    spacingSmall = 8.dp,                 // small gaps (e.g. between list items)
    spacingMedium = 16.dp,               // medium gaps (e.g. between sections)
    spacingLarge = 24.dp,                // large gaps (e.g. between major sections)
    screenEdgePadding = 16.dp,           // global screen edge padding
    containerPadding = 16.dp,            // padding within containers (cards, dialogs)
    gapBetweenElements = 8.dp,           // gap between unrelated elements
    listItemSpacing = 8.dp,              // spacing between list items
    gridSpacing = 8.dp,                  // spacing between grid items
    sectionSpacing = 16.dp,              // spacing between sections
    safeAreaPadding = 16.dp,             // safe area/inset padding
    keyboardSpacing = 16.dp,             // extra spacing for keyboard adjustments
    small3 = 17.dp,                      // vertical spacing between items in the login screen
    medium1 = 20.dp,                     // vertical padding for the top logo container
    medium2 = 30.dp,                     // padding inside the login form Surface

    // 2. Text Dimensions
    loginTextSize = 60.sp,               // "Log in" title font size
    toggleTextSize = 16.sp,              // Phone/email toggle text font size
    textFieldLabelSize = 16.sp,          // Text field label font size
    buttonTextSize = 18.sp,              // Login button font size
    linkTextSize = 16.sp,                // "Forgot Password?" and "Sign Up" link text font size
    errorTextSize = 14.sp,               // Error text font size
    headingLineHeight = 28.sp,           // Heading line height
    bodyLineHeight = 20.sp,              // Body text line height
    letterSpacing = 0.sp,                // Default letter spacing
    textPadding = 8.dp,                  // General text padding
    errorTextSpacing = 4.dp,             // Spacing for error texts

    // 3. Component Sizes
    logoSize = 100.dp,                   // App logo size
    textFieldHeight = 56.dp,             // Text field height
    buttonHeight = 48.dp,                // Button height
    buttonMinWidth = 64.dp,              // Minimum button width
    progressIndicatorSize = 24.dp,       // Progress indicator size

    // 4. Icons and Images
    iconSize = 24.dp,                    // Default icon size
    iconPadding = 4.dp,                  // Icon padding

    // 5. Layout Dimensions
    maxContentWidth = 600.dp,            // Maximum content width
    minTouchTargetSize = 48.dp,          // Minimum touch target size

    // 6. Animation Properties
    animationDurationShort = 300,        // 300ms for short animations
    animationDurationMedium = 500,       // 500ms for medium animations
    animationDurationLong = 800,         // 800ms for long animations
    animationDelayShort = 100,           // 100ms short delay
    animationDelayMedium = 200,          // 200ms medium delay
    animationDelayLong = 300,            // 300ms long delay

    // 7. Dialog and Modal Dimensions
    dialogTitleTextSize = 20.sp,         // Dialog title font size
    dialogBodyTextSize = 16.sp,          // Dialog body font size
    dialogButtonTextSize = 18.sp,        // Dialog button font size
    dialogPadding = 16.dp,               // Padding inside dialogs
    dialogCornerRadius = 12.dp,          // Dialog corner radius

    // 8. Shapes and Corners
    surfaceCornerRadius = 16.dp,         // Surface corner radius for login form
    textFieldCornerRadius = 10.dp,       // Text field background corner radius
    borderWidth = 1.dp,                  // Standard border width
    shadowElevation = 4.dp,              // Shadow elevation for components
    outlineThickness = 1.dp,             // Outline thickness

    // 9. Lists and Grids
    listHeaderHeight = 48.dp,            // List header height
    dividerThickness = 1.dp,             // Divider thickness
    listIndent = 16.dp,                  // List indentation
    nestedListPadding = 8.dp,            // Padding for nested lists

    // 10. Form Elements
    textFieldHorizontalPadding = 10.dp,  // Horizontal padding inside text fields
    textFieldLabelSpacing = 10.dp,       // Spacing between text field label and input
    helperTextSpacing = 4.dp,            // Spacing for helper texts
    formGroupSpacing = 12.dp,            // Spacing between form groups
    validationIconSize = 20.dp,          // Size for validation icons
    inputAppendWidth = 40.dp,            // Width for appended inputs
    placeholderPadding = 8.dp,           // Padding for placeholder texts

    // 11. Navigation Elements
    navItemHeight = 48.dp,               // Navigation item height
    menuItemHeight = 48.dp,              // Menu item height
    dropdownWidth = 200.dp,              // Dropdown width
    toolbarHeight = 56.dp,               // Toolbar height
    tabHeight = 48.dp,                   // Tab height
    tabWidth = 96.dp,                    // Tab width
    navBarThickness = 2.dp,              // Navigation bar thickness
    drawerWidth = 280.dp,                // Navigation drawer width
    menuPadding = 16.dp,                 // Menu item padding

    // 12. State-Based Dimensions
    focusRingSize = 2.dp,                // Focus ring size
    hoverExpansion = 4.dp,               // Hover expansion value
    activeScale = 0.98f,                 // Active state scale factor
    disabledOpacity = 0.5f,              // Disabled state opacity
    successIndicatorSize = 24.dp,        // Success indicator size

    // 13. Touch and Interaction
    gestureThreshold = 8.dp,             // Gesture threshold distance
    swipeThreshold = 16.dp,              // Swipe action threshold
    dragHandleSize = 24.dp,              // Drag handle size
    pullToRefreshDistance = 80.dp,       // Pull-to-refresh distance
    scrollBarThickness = 4.dp,           // Scroll bar thickness
    rippleRadius = 24.dp,                // Touch ripple radius

    // Additional dimensions from your checklist:
    passwordResetIconSize = 48.dp,       // Icon size for the password reset dialog (was hardcoded as 48.dp)
    countryPickerDialogMaxHeight = 300.dp // Maximum height for the country picker dialog (was hardcoded as 300.dp)
)

val LoginScreenExpandedDimens = LoginScreenDimens(
    // 1. Spacing and Padding
    spacingExtraSmall = 4.dp,            // very small gaps (use for icon spacing, e.g. in phone field)
    spacingSmall = 8.dp,                 // small gaps (e.g. between list items)
    spacingMedium = 16.dp,               // medium gaps (e.g. between sections)
    spacingLarge = 24.dp,                // large gaps (e.g. between major sections)
    screenEdgePadding = 16.dp,           // global screen edge padding
    containerPadding = 16.dp,            // padding within containers (cards, dialogs)
    gapBetweenElements = 8.dp,           // gap between unrelated elements
    listItemSpacing = 8.dp,              // spacing between list items
    gridSpacing = 8.dp,                  // spacing between grid items
    sectionSpacing = 16.dp,              // spacing between sections
    safeAreaPadding = 16.dp,             // safe area/inset padding
    keyboardSpacing = 16.dp,             // extra spacing for keyboard adjustments
    small3 = 17.dp,                      // vertical spacing between items in the login screen
    medium1 = 20.dp,                     // vertical padding for the top logo container
    medium2 = 30.dp,                     // padding inside the login form Surface

    // 2. Text Dimensions
    loginTextSize = 22.sp,               // "Log in" title font size
    toggleTextSize = 16.sp,              // Phone/email toggle text font size
    textFieldLabelSize = 16.sp,          // Text field label font size
    buttonTextSize = 18.sp,              // Login button font size
    linkTextSize = 16.sp,                // "Forgot Password?" and "Sign Up" link text font size
    errorTextSize = 14.sp,               // Error text font size
    headingLineHeight = 28.sp,           // Heading line height
    bodyLineHeight = 20.sp,              // Body text line height
    letterSpacing = 0.sp,                // Default letter spacing
    textPadding = 8.dp,                  // General text padding
    errorTextSpacing = 4.dp,             // Spacing for error texts

    // 3. Component Sizes
    logoSize = 100.dp,                   // App logo size
    textFieldHeight = 56.dp,             // Text field height
    buttonHeight = 48.dp,                // Button height
    buttonMinWidth = 64.dp,              // Minimum button width
    progressIndicatorSize = 24.dp,       // Progress indicator size

    // 4. Icons and Images
    iconSize = 24.dp,                    // Default icon size
    iconPadding = 4.dp,                  // Icon padding

    // 5. Layout Dimensions
    maxContentWidth = 600.dp,            // Maximum content width
    minTouchTargetSize = 48.dp,          // Minimum touch target size

    // 6. Animation Properties
    animationDurationShort = 300,        // 300ms for short animations
    animationDurationMedium = 500,       // 500ms for medium animations
    animationDurationLong = 800,         // 800ms for long animations
    animationDelayShort = 100,           // 100ms short delay
    animationDelayMedium = 200,          // 200ms medium delay
    animationDelayLong = 300,            // 300ms long delay

    // 7. Dialog and Modal Dimensions
    dialogTitleTextSize = 20.sp,         // Dialog title font size
    dialogBodyTextSize = 16.sp,          // Dialog body font size
    dialogButtonTextSize = 18.sp,        // Dialog button font size
    dialogPadding = 16.dp,               // Padding inside dialogs
    dialogCornerRadius = 12.dp,          // Dialog corner radius

    // 8. Shapes and Corners
    surfaceCornerRadius = 16.dp,         // Surface corner radius for login form
    textFieldCornerRadius = 10.dp,       // Text field background corner radius
    borderWidth = 1.dp,                  // Standard border width
    shadowElevation = 4.dp,              // Shadow elevation for components
    outlineThickness = 1.dp,             // Outline thickness

    // 9. Lists and Grids
    listHeaderHeight = 48.dp,            // List header height
    dividerThickness = 1.dp,             // Divider thickness
    listIndent = 16.dp,                  // List indentation
    nestedListPadding = 8.dp,            // Padding for nested lists

    // 10. Form Elements
    textFieldHorizontalPadding = 10.dp,  // Horizontal padding inside text fields
    textFieldLabelSpacing = 10.dp,       // Spacing between text field label and input
    helperTextSpacing = 4.dp,            // Spacing for helper texts
    formGroupSpacing = 12.dp,            // Spacing between form groups
    validationIconSize = 20.dp,          // Size for validation icons
    inputAppendWidth = 40.dp,            // Width for appended inputs
    placeholderPadding = 8.dp,           // Padding for placeholder texts

    // 11. Navigation Elements
    navItemHeight = 48.dp,               // Navigation item height
    menuItemHeight = 48.dp,              // Menu item height
    dropdownWidth = 200.dp,              // Dropdown width
    toolbarHeight = 56.dp,               // Toolbar height
    tabHeight = 48.dp,                   // Tab height
    tabWidth = 96.dp,                    // Tab width
    navBarThickness = 2.dp,              // Navigation bar thickness
    drawerWidth = 280.dp,                // Navigation drawer width
    menuPadding = 16.dp,                 // Menu item padding

    // 12. State-Based Dimensions
    focusRingSize = 2.dp,                // Focus ring size
    hoverExpansion = 4.dp,               // Hover expansion value
    activeScale = 0.98f,                 // Active state scale factor
    disabledOpacity = 0.5f,              // Disabled state opacity
    successIndicatorSize = 24.dp,        // Success indicator size

    // 13. Touch and Interaction
    gestureThreshold = 8.dp,             // Gesture threshold distance
    swipeThreshold = 16.dp,              // Swipe action threshold
    dragHandleSize = 24.dp,              // Drag handle size
    pullToRefreshDistance = 80.dp,       // Pull-to-refresh distance
    scrollBarThickness = 4.dp,           // Scroll bar thickness
    rippleRadius = 24.dp,                // Touch ripple radius

    // Additional dimensions from your checklist:
    passwordResetIconSize = 48.dp,       // Icon size for the password reset dialog (was hardcoded as 48.dp)
    countryPickerDialogMaxHeight = 300.dp // Maximum height for the country picker dialog (was hardcoded as 300.dp)
)