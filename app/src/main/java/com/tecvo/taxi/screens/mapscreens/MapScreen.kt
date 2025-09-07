@file:OptIn(ExperimentalMaterial3Api::class)

package com.tecvo.taxi.screens.mapscreens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.window.layout.WindowMetricsCalculator
import com.tecvo.taxi.BuildConfig
import com.tecvo.taxi.R
import com.tecvo.taxi.TaxiApplication
import com.tecvo.taxi.components.AnimatedHeader
import com.tecvo.taxi.components.CountBadge
import com.tecvo.taxi.components.EntityVisibilityToggle
import com.tecvo.taxi.components.NotificationBellButton
import com.tecvo.taxi.components.OfflineBanner
import com.tecvo.taxi.components.RadiusSlider
import com.tecvo.taxi.di.HiltEntryPoints
import com.tecvo.taxi.services.ErrorHandlingService
import com.tecvo.taxi.services.GeocodingService
import com.tecvo.taxi.ui.theme.MapScreenCompactDimens
import com.tecvo.taxi.ui.theme.MapScreenCompactMediumDimens
import com.tecvo.taxi.ui.theme.MapScreenCompactSmallDimens
import com.tecvo.taxi.ui.theme.MapScreenExpandedDimens
import com.tecvo.taxi.ui.theme.MapScreenMediumDimens
import com.tecvo.taxi.integration.WithCityBasedOverview
import com.tecvo.taxi.integration.rememberFilteredEntities
import com.tecvo.taxi.viewmodel.CityBasedOverviewViewModel
import com.tecvo.taxi.utils.ConnectivityManager
import com.tecvo.taxi.utils.RadiusCircle
import com.tecvo.taxi.viewmodel.MapViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import timber.log.Timber

private const val TAG = "MapScreen"



/**
 * Unified Map Screen for both Passenger and Driver modes, with Town and Local destinations.
 * Fully updated to use Hilt for dependency injection.
 */
@Composable
fun MapScreen(
    navController: NavController,
    userType: String,
    destination: String,
    viewModel: MapViewModel = hiltViewModel(),
    cityOverviewViewModel: CityBasedOverviewViewModel = hiltViewModel()
) {
    // Performance Optimization: Only log once per parameter change, not on every composition
    LaunchedEffect(userType, destination) {
        if (BuildConfig.DEBUG) {
            Timber.tag(TAG).i("Initializing MapScreen with userType=$userType, destination=$destination")
        }
    }

    // Validate parameters
    require(userType in listOf("passenger", "driver")) {
        val errorMsg = "User type must be passenger or driver"
        Timber.tag(TAG).e("Validation error: $errorMsg (received: $userType)")
        errorMsg
    }
    require(destination in listOf("town", "local")) {
        val errorMsg = "Destination must be town or local"
        Timber.tag(TAG).e("Validation error: $errorMsg (received: $destination)")
        errorMsg
    }

    // MOVE NOTIFICATION STATE DECLARATION HERE - BEFORE IT'S USED
    var isNotificationEnabled by remember { mutableStateOf(false) }
    

    // Construct screen name - memoized to prevent recomposition
    val screenName = remember(userType, destination) {
        "${userType.replaceFirstChar { it.uppercase() }}Map${destination.replaceFirstChar { it.uppercase() }}"
    }
    
    // Only log once per screen name change
    LaunchedEffect(screenName) {
        Timber.tag(TAG).d("Screen name created: $screenName")
    }

    val context = LocalContext.current

    // Navigation-based cleanup: Only cleanup Firebase when truly leaving map
    DisposableEffect(navController) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        
        onDispose {
            // Check if we're navigating away from ANY map route
            val currentRoute = navController.currentDestination?.route
            val mapRoutes = listOf(
                "DriverMapScreenTown", "DriverMapScreenLocal",
                "PassengerMapScreenTown", "PassengerMapScreenLocal"
            )
            
            val leavingMapScreen = currentRoute !in mapRoutes
            
            // Get app background state from TaxiApplication
            val app = try {
                TaxiApplication.getInstance()
            } catch (e: IllegalStateException) {
                null
            }
            
            if (leavingMapScreen && currentUserId != null) {
                // Only cleanup Firebase when truly leaving map screens AND not just backgrounding
                if (app != null) {
                    // If app is just going to background, maintain Firebase presence
                    if (!app.isAppInBackground()) {
                        viewModel.cleanupFirebaseUserData()
                        Timber.tag(TAG).i("Firebase cleanup triggered - user navigated away from map (to: $currentRoute)")
                    } else {
                        Timber.tag(TAG).d("App backgrounding from map - maintaining Firebase presence")
                    }
                } else {
                    // Fallback if app instance unavailable
                    viewModel.cleanupFirebaseUserData()
                    Timber.tag(TAG).w("Firebase cleanup triggered - app instance unavailable")
                }
            } else {
                Timber.tag(TAG).d("Staying within map ecosystem - Firebase cleanup skipped")
            }
        }
    }

    // Get Hilt-managed dependencies
    val entryPoint = HiltEntryPoints.getMapScreenEntryPoint(context)
    val errorHandlingService = entryPoint.errorHandlingService()
    val locationService = entryPoint.locationService()
    val notificationService = entryPoint.notificationService()
    val permissionManager = entryPoint.permissionManager()
    val mapsInitManager = entryPoint.mapsInitializationManager()
    val geocodingService = entryPoint.geocodingService()

    // Create ConnectivityManager - stable reference
    val connectivityManager = remember(context) { ConnectivityManager(context) }

    // Create SnackbarHostState for error messages - stable reference
    val snackbarHostState = remember { SnackbarHostState() }

    // Initialize all component managers with Hilt dependencies
    val mapState = rememberMapScreenState(viewModel)
    val errorHandler = rememberMapScreenErrorHandler(snackbarHostState, errorHandlingService)
    val locationTracker = rememberMapScreenLocationTracker(errorHandler, locationService)
    val entityTracker = rememberMapScreenEntityTracker(locationTracker, context)
    val notificationManager = rememberMapScreenNotificationManager(
        connectivityManager = connectivityManager,
        notificationService = notificationService,
        locationService = locationService  // ADD THIS LINE
    )

    // Initialize ViewModel with user data - memoized to prevent repeated auth calls
    val userId = remember { FirebaseAuth.getInstance().currentUser?.uid }


    // Effect to track location
    LocationTrackerEffect(locationTracker, screenName)

    // Effect for notification tracking
    NotificationManagerEffect(
        notificationManager = notificationManager,
        screenName = screenName,
        userId = userId ?: "", // ADD THIS - handle null case since userId can be null
        userType = userType,
        destination = destination,
        isNotificationEnabled = isNotificationEnabled  // ADD THIS
    )

    // Also add this effect to handle notification state changes:
    LaunchedEffect(isNotificationEnabled, userId, userType, destination) {
        if (userId != null) {
            notificationManager.updateNotificationState(
                userId = userId,
                userType = userType,
                destination = destination,
                isEnabled = isNotificationEnabled
            )
        }
    }


    LaunchedEffect(userId) {
        // Remove the val userId = ... line from here
        if (userId != null) {
            viewModel.initialize(userId, userType, destination)
        } else {
            Timber.tag(TAG).e("User ID is null, cannot initialize ViewModel")
            snackbarHostState.showSnackbar(
                message = "Error: User not logged in. Please login first.",
                actionLabel = "Go Back"
            )
            navController.navigate("login")
        }
    }

    // Performance Optimization: Simplified dimension calculation to reduce main thread work
    val dimens = remember {
        // Use display metrics directly instead of WindowMetricsCalculator for better performance
        val displayMetrics = context.resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        
        // Return dimensions immediately based on calculation
        when {
            screenWidthDp < 400 -> MapScreenCompactSmallDimens
            screenWidthDp in 400f..500f -> MapScreenCompactMediumDimens
            screenWidthDp in 500f..600f -> MapScreenCompactDimens
            screenWidthDp in 600f..840f -> MapScreenMediumDimens
            else -> MapScreenExpandedDimens
        }
    }

    // Collect states
    val currentLocation by mapState.currentLocation()
    val primaryEntityLocations by mapState.primaryEntityLocations()
    val secondaryEntityLocations by mapState.secondaryEntityLocations()
    val nearbyPrimaryCount by mapState.nearbyPrimaryCount()
    val nearbySecondaryCount by mapState.nearbySecondaryCount()
    
    // Get filtered entities for city-based overview
    val (filteredPassengerEntities, filteredDriverEntities) = rememberFilteredEntities(
        originalPassengers = if (userType == "passenger") primaryEntityLocations else secondaryEntityLocations,
        originalDrivers = if (userType == "driver") primaryEntityLocations else secondaryEntityLocations
    )
    
    // Map filtered entities to primary/secondary based on user type
    val filteredPrimaryEntities = if (userType == "passenger") filteredPassengerEntities else filteredDriverEntities
    val filteredSecondaryEntities = if (userType == "passenger") filteredDriverEntities else filteredPassengerEntities
    
    // City overview state
    val cityOverviewState by cityOverviewViewModel.state.collectAsState()
    val isLoading by mapState.isLoading()
    val showSameTypeEntities by mapState.showSameTypeEntities()
    val mapType by mapState.mapType()
    val searchRadius by mapState.searchRadius()
    val isOffline by mapState.isOffline()
    val showOfflineMessage by notificationManager.showOfflineMessage.collectAsState()
    
    // Marker click states for area information
    val selectedMarkerLocation by viewModel.selectedMarkerLocation.collectAsState()
    val selectedMarkerAreaName by viewModel.selectedMarkerAreaName.collectAsState()
    val selectedMarkerType by viewModel.selectedMarkerType.collectAsState()
    val isLoadingAreaInfo by viewModel.isLoadingAreaInfo.collectAsState()

    // Location name state for displaying place name instead of "My Location"
    var locationName by remember { mutableStateOf("Current Location") }
    
    // Update location name when current location changes
    LaunchedEffect(currentLocation) {
        currentLocation?.let { location ->
            try {
                val placeName = geocodingService.getPlaceNameFromCoordinates(
                    location.latitude, 
                    location.longitude
                )
                locationName = placeName
            } catch (e: Exception) {
                Timber.tag(TAG).w(e, "Failed to get place name for location")
                locationName = "Current Location"
            }
        }
    }

    // Performance Optimization: Simplified back navigation animation with reduced logging
    var showBackNavigation by remember { mutableStateOf(false) }
    val backNavAlpha = remember { Animatable(1f) }

    // Back navigation hint animation - optimized with single log
    LaunchedEffect(Unit) {
        if (BuildConfig.DEBUG) {
            Timber.tag(TAG).d("Setting up back navigation hint animation")
        }
        delay(4000) // Wait 4 seconds before showing hint
        showBackNavigation = true
        delay(3000) // Show hint for 3 seconds
        backNavAlpha.animateTo(0f, animationSpec = tween(1000))
        showBackNavigation = false
        if (BuildConfig.DEBUG) {
            Timber.tag(TAG).d("Back navigation hint animation completed")
        }
    }

    // Notification state - must be declared before permission launchers
    //var isNotificationEnabled by remember { mutableStateOf(false) }

    // Function to initialize map after permissions
    fun initializeMapAfterPermissions() {
        mapState.updateLocationPermission(true)
        // Get the current location
        locationTracker.getLastLocation(
            onSuccess = { location ->
                Timber.tag(TAG).i("Retrieved user location: ${location.latitude}, ${location.longitude}")
            },
            onFailure = { e ->
                Timber.tag(TAG).e("Error getting location: ${e.message}")
                errorHandler.handleError(ErrorHandlingService.AppError.LocationError(e, "Failed to get your location. Please make sure location services are enabled."))
            })
        // Start location updates
        locationTracker.startLocationUpdates(interval = 10000L)
    }

    // Background location permission launcher (declare first)
    val backgroundPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            permissionManager.updatePermissionState(
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                isGranted
            )
        }

        Timber.tag(TAG).i("Background location permission result: ${if (isGranted) "Granted" else "Denied"}")

        if (isGranted) {
            android.widget.Toast.makeText(
                context,
                "Background location enabled! You'll stay visible even during calls.",
                android.widget.Toast.LENGTH_LONG
            ).show()
        } else {
            android.widget.Toast.makeText(
                context,
                "Limited functionality: You'll only be visible while using the app.",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    // Permission request launchers
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        permissionManager.updatePermissionState(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            isGranted
        )

        if (isGranted) {
            Timber.tag(TAG).i("Foreground location permission granted")

            // Show background location explanation dialog
            val activity = context as? Activity
            if (activity != null) {
                permissionManager.requestBackgroundLocationPermission(
                    activity = activity,
                    permissionLauncher = backgroundPermissionLauncher,
                    onResult = { backgroundGranted ->
                        Timber.tag(TAG).i("Background location result: $backgroundGranted")
                        initializeMapAfterPermissions()
                    }
                )
            }
        } else {
            Timber.tag(TAG).w("Foreground location permission denied")
            errorHandler.handlePermissionDenial()
        }
    }

    // Notification permission launcher
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        Timber.tag(TAG).i("Notification permission result: ${if (isGranted) "Granted" else "Denied"}")

        if (isGranted) {
            // Permission granted - enable notifications
            isNotificationEnabled = true

            // Show success message
            android.widget.Toast.makeText(
                context,
                "Notifications enabled! You'll be notified when ${if (userType == "driver") "passengers" else "drivers"} are nearby.",
                android.widget.Toast.LENGTH_LONG
            ).show()
        } else {
            // Permission denied - keep notifications disabled
            isNotificationEnabled = false

            // Show message about missing permission
            android.widget.Toast.makeText(
                context,
                "Notification permission denied. You can enable it later in settings.",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    // Check location permission on first composition
    LaunchedEffect(Unit) {
        Timber.tag(TAG).d("Checking location permission")
        val activity = context as? Activity
        if (activity != null) {
            if (permissionManager.isLocationPermissionGranted()) {
                Timber.tag(TAG).i("Location permission already granted, initializing map")
                initializeMapAfterPermissions()
            } else {
                Timber.tag(TAG).i("Requesting location permission")
                permissionManager.requestLocationPermission(
                    activity = activity,
                    permissionLauncher = requestPermissionLauncher,
                    onResult = { /* Handled in launcher callback */ }
                )
            }
        }
    }

    // Create camera position state - with better initialization
    val cameraPositionState = rememberCameraPositionState {
        // Initialize with a default position to avoid camera errors
        com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
            com.google.android.gms.maps.model.LatLng(0.0, 0.0), 10f
        )
    }

    // Create UI controls
    val uiControls = rememberMapScreenUIControls(mapState, cameraPositionState)

    // Camera effect
    MapCameraEffect(currentLocation, cameraPositionState, mapsInitManager, dimens.defaultMapZoom)

    // Main UI Layout - WRAPPED with city-based overview
    WithCityBasedOverview(
                currentUserLocation = currentLocation,
                allPassengerLocations = if (userType == "passenger") primaryEntityLocations else secondaryEntityLocations,
                allDriverLocations = if (userType == "passenger") secondaryEntityLocations else primaryEntityLocations,
                userType = userType,
                destination = destination, // Pass destination for filtering
                showSameTypeEntities = showSameTypeEntities, // Pass visibility state
                showToggleButton = false, // Button is now handled in control stack
                showInfoCard = true,
                cityOverviewViewModel = cityOverviewViewModel,
                content = {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(colors = listOf(Color(0xFF0A1F44), Color(0xFF16294B))))
        ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            // Add the offline banner at the top
            OfflineBanner(
                visible = showOfflineMessage,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(10f) // Ensure it's above other content
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Simple animated header with push/pull animations
                AnimatedHeader(
                    userType = userType,
                    destination = destination,
                    nearbyPrimaryCount = nearbyPrimaryCount,
                    nearbySecondaryCount = nearbySecondaryCount,
                    minibusImageSize = dimens.minibusImageSize,
                    passengerImageSize = dimens.passengerImageSize,
                    headerTextSize = dimens.headerTextSize
                )
                Spacer(modifier = Modifier.height(dimens.mediumSpacing))

                // Map section
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)) {
                    if (isLoading) {
                        // Show loading indicator
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Color.Black.copy(alpha = dimens.loadingBackgroundAlpha),
                                    RoundedCornerShape(dimens.loadingIndicatorCornerRadius)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    } else {
                        // Render Google Map
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            properties = createMapProperties(mapType),
                            uiSettings = createMapUiSettings()
                        ) {
                            // Draw user location marker and notification radius
                            currentLocation?.let { loc ->
                                // Draw notification radius circle
                                RadiusCircle(
                                    center = loc,
                                    radiusKm = searchRadius,
                                    fillColor = Color(0x1A4CAF50), // Light green with transparency
                                    strokeColor = Color(0x4D4CAF50) // Medium green border
                                )

                                // Performance Optimization: Stable marker bitmap creation with lazy initialization
                                val density = LocalDensity.current.density
                                val userMarkerBitmap = remember(userType, dimens.imageSize, density) {
                                    // Create bitmap on background thread would be better, but for now optimize cache usage
                                    entityTracker.createMarkerBitmap(
                                        if (userType == "driver") R.drawable.minibus1 else R.drawable.passenger1,
                                        dimens.imageSize,
                                        density
                                    )
                                }
                                
                                FlashingUserMarker(
                                    location = loc,
                                    iconBitmapDescriptor = userMarkerBitmap,
                                    markerTitle = locationName,
                                    markerSnippet = "You are here",
                                    animationDurationMs = dimens.markerAnimationDuration,
                                    onClick = { marker ->
                                        android.util.Log.d("MapScreen", "USER OWN MARKER CLICKED at ${loc.latitude}, ${loc.longitude}")
                                        viewModel.onMarkerClick(loc, "My Location")
                                        true // Return true to consume the click event
                                    }
                                )
                            }

                            // Only show other entities when online
                            if (!isOffline) {
                                // Draw markers for primary entities (same type as user) if visibility is enabled
                                if (showSameTypeEntities) {
                                    // Performance Optimization: Stable primary marker bitmap with optimized key
                                    val density = LocalDensity.current.density
                                    val primaryMarkerBitmap = remember(userType, dimens.imageSize, density) {
                                        val primaryIcon = if (userType == "driver") R.drawable.minibus1 else R.drawable.passenger1
                                        entityTracker.createMarkerBitmap(
                                            primaryIcon,
                                            dimens.imageSize,
                                            density
                                        )
                                    }

                                    // Filter entities by radius - use derivedStateOf to optimize filtering
                                    // Use city-filtered entities if city overview is active, otherwise use all entities
                                    // BYPASS radius filtering when city overview is active to show ALL city users
                                    val entitiesToFilter = filteredPrimaryEntities.takeIf { it.isNotEmpty() } ?: primaryEntityLocations
                                    val filteredPrimaryLocations by remember(
                                        cityOverviewState.isActive,
                                        filteredPrimaryEntities,
                                        primaryEntityLocations,
                                        currentLocation,
                                        searchRadius,
                                        showSameTypeEntities
                                    ) {
                                        androidx.compose.runtime.derivedStateOf {
                                            if (cityOverviewState.isActive && filteredPrimaryEntities.isNotEmpty()) {
                                                // When city overview is active, show ALL city entities without radius filtering
                                                filteredPrimaryEntities
                                            } else {
                                                // Normal radius-based filtering
                                                currentLocation?.let { userLoc ->
                                                    entitiesToFilter.filter { entityLoc ->
                                                        entityTracker.isEntityWithinRadius(entityLoc, userLoc, searchRadius.toDouble())
                                                    }
                                                } ?: emptyList()
                                            }
                                        }
                                    }

                                    // Group nearby markers if there are many
                                    if (filteredPrimaryLocations.size > 30) {
                                        // Group nearby points into clusters
                                        val markers = filteredPrimaryLocations.groupBy { loc ->
                                            // Create grid cells to group nearby points
                                            val latGrid = (loc.latitude * 556).toInt()
                                            val lngGrid = (loc.longitude * 556).toInt()
                                            "$latGrid,$lngGrid" // Cell identifier
                                        }

                                        // Render clusters or individual markers
                                        markers.forEach { (_, locations) ->
                                            if (locations.size == 1) {
                                                // Single marker
                                                Marker(
                                                    state = MarkerState(position = locations.first()),
                                                    icon = primaryMarkerBitmap,
                                                    alpha = dimens.markerAlpha
                                                )
                                            } else {
                                                // Create cluster center
                                                val center = LatLng(
                                                    locations.map { it.latitude }.average(),
                                                    locations.map { it.longitude }.average()
                                                )
                                                // Performance Optimization: Cache cluster bitmap creation with optimized keys
                                                val density = LocalDensity.current.density
                                                val clusterBitmap = remember(locations.size, dimens.imageSize, density) {
                                                    entityTracker.createClusterBitmap(
                                                        locations.size,
                                                        dimens.imageSize,
                                                        density
                                                    )
                                                }
                                                // Draw cluster marker with count
                                                Marker(
                                                    state = MarkerState(position = center),
                                                    icon = clusterBitmap,
                                                    title = "${locations.size} ${if (userType == "driver") "Taxis" else "Passengers"}"
                                                )
                                            }
                                        }
                                    } else {
                                        // For small number of markers, render normally
                                        filteredPrimaryLocations.forEach { loc ->
                                            Marker(
                                                state = MarkerState(position = loc),
                                                icon = primaryMarkerBitmap,
                                                title = if (userType == "driver") "Taxi" else "Passenger",
                                                snippet = if (userType == "driver")
                                                    "Available taxi for $destination"
                                                else
                                                    "Other passenger going to $destination",
                                                alpha = dimens.markerAlpha,
                                                onClick = { marker ->
                                                    android.util.Log.d("MapScreen", "PRIMARY MARKER CLICKED at ${loc.latitude}, ${loc.longitude}")
                                                    viewModel.onMarkerClick(loc, if (userType == "driver") "Taxi" else "Passenger")
                                                    true // Return true to consume the click event
                                                }
                                            )
                                        }
                                    }
                                }

                                // Performance Optimization: Stable secondary marker bitmap with optimized key
                                val density = LocalDensity.current.density
                                val secondaryMarkerBitmap = remember(userType, dimens.imageSize, density) {
                                    val secondaryIcon = if (userType == "driver") R.drawable.passenger1 else R.drawable.minibus1
                                    entityTracker.createMarkerBitmap(
                                        secondaryIcon,
                                        dimens.imageSize,
                                        density
                                    )
                                }

                                // Filter secondary entities by radius - use derivedStateOf to optimize filtering
                                // Use city-filtered entities if city overview is active, otherwise use all entities
                                // BYPASS radius filtering when city overview is active to show ALL city users
                                val secondaryEntitiesToFilter = filteredSecondaryEntities.takeIf { it.isNotEmpty() } ?: secondaryEntityLocations
                                val filteredSecondaryLocations by remember(
                                    cityOverviewState.isActive,
                                    filteredSecondaryEntities,
                                    secondaryEntityLocations,
                                    currentLocation,
                                    searchRadius,
                                    showSameTypeEntities
                                ) {
                                    androidx.compose.runtime.derivedStateOf {
                                        if (cityOverviewState.isActive && filteredSecondaryEntities.isNotEmpty()) {
                                            // When city overview is active, show ALL city entities without radius filtering
                                            filteredSecondaryEntities
                                        } else {
                                            // Normal radius-based filtering
                                            currentLocation?.let { userLoc ->
                                                secondaryEntitiesToFilter.filter { entityLoc ->
                                                    entityTracker.isEntityWithinRadius(entityLoc, userLoc, searchRadius.toDouble())
                                                }
                                            } ?: emptyList()
                                        }
                                    }
                                }

                                // Draw markers for secondary entities
                                filteredSecondaryLocations.forEach { loc ->
                                    Marker(
                                        state = MarkerState(position = loc),
                                        icon = secondaryMarkerBitmap,
                                        title = if (userType == "driver") "Passenger" else "Taxi",
                                        snippet = if (userType == "driver")
                                            "Passenger going to $destination"
                                        else
                                            "Available taxi to $destination",
                                        alpha = dimens.markerAlpha,
                                        onClick = { marker ->
                                            android.util.Log.d("MapScreen", "SECONDARY MARKER CLICKED at ${loc.latitude}, ${loc.longitude}")
                                            viewModel.onMarkerClick(loc, if (userType == "driver") "Passenger" else "Taxi")
                                            true // Return true to consume the click event
                                        }
                                    )
                                }
                            }
                            
                        }

                        // Map controls - Positioned at the top to match original code
                        MapControlsRow(
                            mapType = mapType,
                            onMapTypeToggle = { mapState.toggleMapType() },
                            onRecenterMap = {
                                uiControls.recenterMap(currentLocation, dimens.closeupMapZoom)
                            },
                            cornerRadius = dimens.cornerRadius,
                            mapButtonTextSize = dimens.mapButtonTextSize,
                            mapIconSize = dimens.mapIconSize,
                            controlsBackgroundAlpha = dimens.controlsBackgroundAlpha,
                            smallSpacing = dimens.smallSpacing
                        )
                    }
                }
            }

            // Entity controls column (radius slider + visibility toggle)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 85.dp, end = 9.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                // Stacked controls in a more aligned manner
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // City overview toggle button
                    com.tecvo.taxi.components.CityOverviewToggleButton(
                        isCityOverviewActive = cityOverviewState.isActive,
                        isLoading = cityOverviewState.isLoading,
                        onToggle = {
                            cityOverviewViewModel.toggleCityOverview()
                            Timber.tag(TAG).d("City overview toggle clicked - new state: ${!cityOverviewState.isActive}")
                        }
                    )

                    // Notification bell button
                    Box(
                        modifier = Modifier.width(48.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        // Update the NotificationBellButton call to save the preference:
                        NotificationBellButton(
                            userType = userType,
                            isNotificationEnabled = isNotificationEnabled,
                            onToggle = { newState ->
                                isNotificationEnabled = newState

                                // Optional: Save this preference to persist across app restarts
                                // You can use your UserPreferencesRepository or SharedPreferences here
                                // Example:
                                // viewModel.updateNotificationPreference(newState)

                                Timber.tag(TAG).d("Notification preference changed to: $newState")
                            },
                            currentRadius = searchRadius,
                            permissionLauncher = notificationPermissionLauncher
                        )

                    }

                    // Radius slider
                    Box(
                        modifier = Modifier.width(48.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        RadiusSlider(
                            currentRadius = searchRadius,
                            onRadiusChange = { newRadius ->
                                mapState.updateSearchRadius(newRadius)
                            }
                        )
                    }

                    // Entity visibility toggle
                    EntityVisibilityToggle(
                        userType = userType,
                        isVisible = showSameTypeEntities,
                        onToggle = {
                            mapState.toggleSameTypeEntitiesVisibility()
                        }
                    )
                }
            }

            // Back navigation animation
            AnimatedVisibility(
                visible = showBackNavigation,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp),
                enter = slideInHorizontally(initialOffsetX = { -it }),
                exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
            ) {
                // Performance Optimization: Removed verbose logging from composition
                Box(
                    modifier = Modifier
                        .background(
                            Color.White.copy(alpha = dimens.backNavigationAlpha),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.fingerpointer),
                            contentDescription = "Back",
                            tint = Color(0xFF0D4C54),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Click here to go back",
                            color = Color(0xFF0D4C54),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Transparent clickable area for navigation
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(dimens.edgeClickableWidth)
                    .align(Alignment.CenterStart)
                    .zIndex(100f)
                    .background(Color.Transparent)
                    .clickable {
                        navController.navigate("home")
                    }
            )

            // Simple Area Name Popup
            selectedMarkerLocation?.let { markerLoc ->
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 120.dp)
                        .zIndex(20f)
                ) {
                    if (isLoadingAreaInfo) {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Black.copy(alpha = 0.8f),
                                contentColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Finding area...",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White
                                    )
                                )
                            }
                        }
                    } else {
                        selectedMarkerAreaName?.let { areaName ->
                            if (areaName != "Current Location" && areaName.isNotBlank()) {
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF0D4C54),
                                        contentColor = Color.White
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                    modifier = Modifier.clickable { viewModel.clearSelectedMarker() }
                                ) {
                                    Text(
                                        text = areaName,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White
                                        ),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Snackbar host for error messages
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
            } // Close WithCityBasedOverview content
        ) // Close WithCityBasedOverview
}

/**
 * Driver Map Screen for Town destination - wrapper composable
 */
@Composable
fun DriverMapScreenTown(navController: NavController) {
    MapScreen(navController = navController, userType = "driver", destination = "town")
}

/**
 * Driver Map Screen for Local destination - wrapper composable
 */
@Composable
fun DriverMapScreenLocal(navController: NavController) {
    MapScreen(navController = navController, userType = "driver", destination = "local")
}

/**
 * Passenger Map Screen for Town destination - wrapper composable
 */
@Composable
fun PassengerMapScreenTown(navController: NavController) {
    MapScreen(navController = navController, userType = "passenger", destination = "town")
}

/**
 * Passenger Map Screen for Local destination - wrapper composable
 */
@Composable
fun PassengerMapScreenLocal(navController: NavController) {
    MapScreen(navController = navController, userType = "passenger", destination = "local")
}