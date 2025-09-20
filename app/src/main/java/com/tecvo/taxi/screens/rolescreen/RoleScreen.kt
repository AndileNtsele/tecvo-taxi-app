@file:OptIn(ExperimentalMaterial3Api::class)
package com.tecvo.taxi.screens.rolescreen
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import com.tecvo.taxi.ui.typography.JotiOneText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.window.layout.WindowMetricsCalculator
import com.tecvo.taxi.BuildConfig
import com.tecvo.taxi.R
import com.tecvo.taxi.ui.theme.RoleScreenCompactDimens
import com.tecvo.taxi.ui.theme.RoleScreenCompactMediumDimens
import com.tecvo.taxi.ui.theme.RoleScreenCompactSmallDimens
import com.tecvo.taxi.ui.theme.RoleScreenExpandedDimens
import com.tecvo.taxi.ui.theme.RoleScreenMediumDimens
import com.tecvo.taxi.utils.DeviceTypeUtil
import com.tecvo.taxi.viewmodel.RoleViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber

// Define a constant tag for consistent logging
private const val TAG = "TaxiRoleScreen"
/**
 * Unified screen that handles both Driver and Passenger roles with the same UI structure
 * but different visual assets and navigation targets.
 *
 * This screen is displayed after the user chooses their role (driver or passenger)
 * and allows them to select either "Town" or "Local" as their destination.
 *
 * @param navController Navigation controller for screen transitions
 * @param role The user role: "driver" or "passenger"
 * @param viewModel The ViewModel instance for this screen
 */
@Composable
fun RoleScreen(
    navController: NavController,
    role: String,
    viewModel: RoleViewModel = hiltViewModel() // Changed from viewModel()
) {
    // Performance Optimization: Only log in debug builds and once per role change
    LaunchedEffect(role) {
        if (BuildConfig.DEBUG) {
            Timber.tag(TAG).i("Initializing RoleScreen with role=$role")
        }
    }
// Validate role parameter
    require(role in listOf("driver", "passenger")) {
        val errorMsg = "Role must be driver or passenger"
        Timber.tag(TAG).e("Validation error: $errorMsg (received: $role)")
        errorMsg
    }
    val context = LocalContext.current
    // Setup coroutine scope for launching coroutines from UI events
    val coroutineScope = rememberCoroutineScope()
// Collect states from ViewModel with safe defaults for test reliability
    val isLoading by viewModel.isLoading.collectAsState(initial = false)
    
    // Navigation state to prevent double-clicks
    var isNavigating by remember { mutableStateOf(false) }
// Set user role in ViewModel
    LaunchedEffect(role) {
        viewModel.setUserRole(role)
    }
    val windowMetrics = remember {
        WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(context)
    }
    
    val screenWidthDp = remember(windowMetrics) {
        windowMetrics.bounds.width() / context.resources.displayMetrics.density
    }
    
    // Performance Optimization: Combine screen width calculation and dimension selection
    val dimens = remember(screenWidthDp, context) {
        // Special handling for foldable phones: Always use phone dimensions
        if (DeviceTypeUtil.isFoldablePhone(context)) {
            // Cap foldables at largest phone dimension (CompactDimens)
            when {
                screenWidthDp < 400f -> RoleScreenCompactSmallDimens
                screenWidthDp in 400f..500f -> RoleScreenCompactMediumDimens
                else -> RoleScreenCompactDimens  // Max phone size
            }
        } else {
            // Normal dimension selection for non-foldable devices
            when {
                screenWidthDp < 400f -> RoleScreenCompactSmallDimens
                screenWidthDp in 400f..500f -> RoleScreenCompactMediumDimens
                screenWidthDp in 500f..600f -> RoleScreenCompactDimens
                screenWidthDp in 600f..840f -> RoleScreenMediumDimens
                else -> RoleScreenExpandedDimens
            }
        }
    }
    // Get role-specific assets and navigation targets - memoized to prevent recalculation
    val roleAssets = remember(role) {
        object {
            val roleImage = if (role == "driver") R.drawable.minibus1 else R.drawable.passenger1
            val townNavigationRoute = if (role == "driver") "DriverMapScreenTown" else "PassengerMapScreenTown"
            val localNavigationRoute = if (role == "driver") "DriverMapScreenLocal" else "PassengerMapScreenLocal"
            val roleDescription = if (role == "driver") "Minibus" else "Passenger"
        }
    }
    // Performance Optimization: Reduce location logging overhead
    LaunchedEffect(Unit) {
        if (BuildConfig.DEBUG) {
            Timber.tag(TAG).d("Fetching current location")
        }
        val location = getCurrentLocationSuspend(context)
        if (location != null) {
            viewModel.updateLocation(LatLng(location.latitude, location.longitude))
            if (BuildConfig.DEBUG) {
                Timber.tag(TAG).d("Location fetched successfully: ${location.latitude}, ${location.longitude}")
            }
        } else {
            Timber.tag(TAG).w("Failed to fetch location or location permissions not granted")
        }
    }
// Root Box container for the whole screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Performance Optimization: Removed verbose background rendering logs
        Image(
            painter = painterResource(id = R.drawable.background_image),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
        alpha = dimens.backgroundAlpha
        )
// Gradient overlay on top of the image for better contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0A1F44).copy(alpha = 0.85f),
                            Color(0xFF16294B).copy(alpha = 0.85f)
                        )
                    )
                )
        )
// Loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White
                )
            }
        } else {
// Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimens.screenPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(dimens.appLogoSpacerHeight))
                // Performance Optimization: Removed verbose logo rendering logs
                Image(
                    painter = painterResource(id = R.drawable.applogo),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(dimens.logoImageSize)
                        .padding(bottom = dimens.appLogoPadding),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(dimens.appLogoSpacerHeight))
                // Performance Optimization: Removed verbose role image rendering logs
                Image(
                    painter = painterResource(id = roleAssets.roleImage),
                    contentDescription = roleAssets.roleDescription,
                    modifier = Modifier
                        .size(dimens.roleImageSize)
                        .padding(dimens.imagePadding),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(dimens.imageSpacerHeight))
                // Performance Optimization: Removed verbose button rendering logs
                Button(
                    onClick = {
                        if (!isNavigating) {
                            isNavigating = true
                            Timber.tag(TAG)
                                .i("User clicked Town button, navigating to ${roleAssets.townNavigationRoute}")
                            coroutineScope.launch {
                                viewModel.saveDestination("town")
                            }
                            navController.navigate(roleAssets.townNavigationRoute)
                        }
                    },
                    modifier = Modifier
                        .testTag("town_button")
                        .width(dimens.buttonWidth)
                        .height(dimens.buttonHeight),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)), // Faint white border
                    shape = RoundedCornerShape(dimens.cornerRadius)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        JotiOneText(
                        text = "Town",
                        fontSize = dimens.labelTextSize,
                        fontWeight = FontWeight.Normal
                        )
                    }
                }
                Spacer(modifier = Modifier.height(dimens.buttonSpacerHeight))
                // Performance Optimization: Removed verbose Local button rendering logs
                Button(
                    onClick = {
                        if (!isNavigating) {
                            isNavigating = true
                            Timber.tag(TAG)
                                .i("User clicked Local button, navigating to ${roleAssets.localNavigationRoute}")
                            coroutineScope.launch {
                                viewModel.saveDestination("local")
                            }
                            navController.navigate(roleAssets.localNavigationRoute)
                        }
                    },
                    modifier = Modifier
                        .testTag("local_button")
                        .width(dimens.buttonWidth)
                        .height(dimens.buttonHeight),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)), // Faint white border
                    shape = RoundedCornerShape(dimens.cornerRadius)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        JotiOneText(
                            text = "Local",
                            fontSize = dimens.labelTextSize,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
                Spacer(modifier = Modifier.height(dimens.buttonSpacerHeight))
                // Performance Optimization: Removed verbose Home button rendering logs
                Button(
                    onClick = {
                        Timber.tag(TAG)
                            .i("User clicked Home button, navigating back to home screen")
                        navController.navigate("home")
                    },
                    modifier = Modifier
                        .width(dimens.buttonWidth)
                        .height(dimens.buttonHeight),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)), // Faint white border
                    shape = RoundedCornerShape(dimens.cornerRadius)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        JotiOneText(
                            text = "Back",
                            fontSize = dimens.labelTextSize,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
// Add a spacer at the bottom to maintain spacing
                Spacer(modifier = Modifier.height(dimens.smallSpacerHeight))
            }
        }
    }
    // Performance Optimization: Removed composition completion log to reduce overhead
}
/**
 * Suspending function to fetch current location using coroutines.
 *
 * This function attempts to get the last known location from the FusedLocationClient.
 * It requires location permissions to be granted before calling.
 *
 * @param context Application context needed for location services
 * @return Location object if available, null otherwise

File - D:\TAXI\app\src\main\java\com\example\taxi\screens\rolescreen\RoleScreen.kt
Page 5 of 5
 */
@SuppressLint("MissingPermission")
private suspend fun getCurrentLocationSuspend(context: Context): Location? {
    // Check permissions before attempting location access
    val hasFineLocation = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    
    val hasCoarseLocation = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    
    if (!hasFineLocation && !hasCoarseLocation) {
        Timber.tag(TAG).w("Location permissions not granted, cannot fetch location")
        return null
    }

    if (BuildConfig.DEBUG) {
        Timber.tag(TAG).d("Attempting to get current location with proper permissions")
    }
    val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    return try {
        val location = fusedLocationClient.lastLocation.await()
        if (location != null && BuildConfig.DEBUG) {
            Timber.tag(TAG).d("Location retrieved: (${location.latitude}, ${location.longitude}), accuracy: ${location.accuracy}m")
        } else if (location == null) {
            Timber.tag(TAG).w("Last known location returned null - device may not have location available")
        }
        location
    } catch (e: SecurityException) {
        Timber.tag(TAG).e(e, "Security exception getting location - permissions issue: ${e.message}")
        null
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "Error getting location: ${e.message}")
        null
    }
}
/**
 * Wrapper composable for Driver role - maintained for backward compatibility
 *
 * This composable provides a consistent entry point for the Driver role screen
 * while using the unified RoleScreen implementation internally.
 *
 * @param navController Navigation controller for screen transitions
 */
@Composable
fun DriverScreen(navController: NavController) {
    // Performance Optimization: Reduced wrapper logging overhead
    if (BuildConfig.DEBUG) {
        Timber.tag(TAG).d("Initializing DriverScreen wrapper")
    }
    RoleScreen(navController = navController, role = "driver")
}
/**
 * Wrapper composable for Passenger role - maintained for backward compatibility
 *
 * This composable provides a consistent entry point for the Passenger role screen
 * while using the unified RoleScreen implementation internally.
 *
 * @param navController Navigation controller for screen transitions
 */
@Composable
fun PassengerScreen(navController: NavController) {
    // Performance Optimization: Reduced wrapper logging overhead
    if (BuildConfig.DEBUG) {
        Timber.tag(TAG).d("Initializing PassengerScreen wrapper")
    }
    RoleScreen(navController = navController, role = "passenger")
}
/**
 * Preview for Driver role screen
 */
@Preview(showBackground = true)
@Composable
fun DriverScreenPreview() {
    // Performance Optimization: Removed preview logging (not needed in production)
    DriverScreen(navController = rememberNavController())
}
/**
 * Preview for Passenger role screen
 */
@Preview(showBackground = true)
@Composable
fun PassengerScreenPreview() {
    // Performance Optimization: Removed preview logging (not needed in production)
    PassengerScreen(navController = rememberNavController())
}