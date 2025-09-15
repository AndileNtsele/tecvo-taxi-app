@file:OptIn(ExperimentalMaterial3Api::class)
package com.tecvo.taxi.screens.homescreens
import android.app.Activity
import android.util.Log
import com.tecvo.taxi.BuildConfig
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.tecvo.taxi.ui.theme.HomeScreenCompactDimens
import com.tecvo.taxi.ui.theme.HomeScreenCompactMediumDimens
import com.tecvo.taxi.ui.theme.HomeScreenCompactSmallDimens
import com.tecvo.taxi.ui.theme.HomeScreenExpandedDimens
import com.tecvo.taxi.ui.theme.HomeScreenMediumDimens
import com.tecvo.taxi.viewmodel.HomeViewModel
import timber.log.Timber

private const val TAG = "HomeScreen"
@Composable
fun HomeScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    // Performance Optimization: Only log in debug builds
    LaunchedEffect(Unit) {
        if (BuildConfig.DEBUG) {
            Timber.tag(TAG).i("UI: Initializing Home Screen")
        }
    }
    
    // Collect ViewModel state with safe defaults for test reliability
    val isLoading by homeViewModel.isLoading.collectAsState(initial = false)
    val isUserLoggedIn by homeViewModel.isUserLoggedIn.collectAsState(initial = false)
// Check if user is logged in and navigate to login if not
    LaunchedEffect(isUserLoggedIn) {
        if (!isUserLoggedIn) {
            Timber.tag(TAG).i("User Flow: User not logged in, redirecting to login screen")
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        }
    }
    // Performance Optimization: Combine window metrics calculation for efficiency
    val context = LocalContext.current
    val dimens = remember(context) {
        val windowMetrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(context as Activity)
        val screenWidthDp = windowMetrics.bounds.width() / context.resources.displayMetrics.density
        
        when {
            screenWidthDp < 400f -> HomeScreenCompactSmallDimens
            screenWidthDp in 400f..500f -> HomeScreenCompactMediumDimens
            screenWidthDp in 500f..600f -> HomeScreenCompactDimens
            screenWidthDp in 600f..840f -> HomeScreenMediumDimens
            else -> HomeScreenExpandedDimens
        }
    }
    
    // Performance Optimization: Removed verbose dimension logging
    // 2) Expose dimension fields for clarity and maintainability - memoized to prevent field access on each recomposition
    val dimensionValues = remember(dimens) {
        object {
            val buttonHeight = dimens.buttonHeight
            val buttonWidth = dimens.buttonWidth
            val bigSpacerHeight = dimens.bigSpacerHeight
            val mediumSpacerHeight = dimens.mediumSpacerHeight
            val appLogoSize = dimens.appLogoSize
            val textSize = dimens.textSize
            val buttonCornerRadius = dimens.buttonCornerRadius
            val settingsIconSize = dimens.settingsIconSize
            val appLogoBottomPadding = dimens.appLogoBottomPadding
            val smallSpacerHeight = dimens.smallSpacerHeight
            val settingsIconCornerRadius = dimens.settingsIconCornerRadius
            val screenPadding = dimens.screenPadding
            val settingsBottomPadding = dimens.settingsBottomPadding
            val settingsEndPadding = dimens.settingsEndPadding
        }
    }
    // Performance Optimization: Reduce analytics logging overhead
    DisposableEffect(Unit) {
        if (BuildConfig.DEBUG) {
            Timber.tag(TAG).i("User Flow: User viewing home screen with role selection")
        }
        onDispose {
            if (BuildConfig.DEBUG) {
                Timber.tag(TAG).i("User Flow: User leaving home screen")
            }
        }
    }
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
// Box container for the whole screen
        Box(modifier = Modifier.fillMaxSize()) {
// Background image with reduced opacity
            Image(
                painter = painterResource(id = R.drawable.background_image),
                contentDescription = "Background Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.8f// Adjust opacity here (0.0f to 1.0f, where 0 is invisible)
            )
// Gradient overlay on top of the image for better contrast
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF0A1F44).copy(alpha = 0.88f),// Dark blue at the top
                                Color(0xFF16294B).copy(alpha = 0.88f)// Slightly lighter blue at the bottom
                            )
                        )
                    )
            )
// Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimensionValues.screenPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(dimensionValues.bigSpacerHeight))
// Logo
                Image(
                    painter = painterResource(id = R.drawable.applogo),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(dimensionValues.appLogoSize)
                        .padding(bottom = dimensionValues.appLogoBottomPadding),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(dimensionValues.bigSpacerHeight))
// Title
                Text(
                    text = "Who are you?",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontSize = dimensionValues.textSize,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("home_title")
                )
                Spacer(modifier = Modifier.height(dimensionValues.mediumSpacerHeight))
// Passenger Button
                Button(
                    onClick = {
                        Timber.tag(TAG).i("User Action: Selected Passenger role")
                        homeViewModel.saveSelectedRole("passenger")
                        navController.navigate("passenger")
                    },
                    modifier = Modifier
                        .testTag("passenger_button")
                        .width(dimensionValues.buttonWidth)
                        .height(dimensionValues.buttonHeight),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(dimensionValues.buttonCornerRadius)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
// Passenger icon
                        Image(
                            painter = painterResource(id = R.drawable.passenger1),
                            contentDescription = null,
                            modifier = Modifier.size(dimensionValues.textSize.value.dp),// Match text height
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Passenger",
                            fontSize = dimensionValues.textSize,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(dimensionValues.smallSpacerHeight))
// Driver Button
                Button(
                    onClick = {
                        Timber.tag(TAG).i("User Action: Selected Driver role")
                        homeViewModel.saveSelectedRole("driver")
                        navController.navigate("driver")
                    },
                    modifier = Modifier
                        .testTag("driver_button")
                        .width(dimensionValues.buttonWidth)
                        .height(dimensionValues.buttonHeight),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(dimensionValues.buttonCornerRadius)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {

// Driver icon (minibus)
                        Image(
                            painter = painterResource(id = R.drawable.minibus1),
                            contentDescription = null,
                            modifier = Modifier.size(dimensionValues.textSize.value.dp),// Match text height
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Driver",
                            fontSize = dimensionValues.textSize,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
// Bottom actions row with Settings
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = dimensionValues.settingsBottomPadding, end = dimensionValues.settingsEndPadding),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Bottom
            ) {
// Settings Icon
                IconButton(
                    onClick = {
                        if (BuildConfig.DEBUG) {
                            Log.i(TAG, "User Action: Opening Settings screen")
                        }
                        navController.navigate("settings")
                    },
                    modifier = Modifier
                        .size(dimensionValues.settingsIconSize)
                        .background(
                            color = Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(dimensionValues.settingsIconCornerRadius)
                        )
                        .padding(4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.settings),
                        contentDescription = "Settings",
                        tint = Color.White,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
// Preview function with a unique name to avoid conflicts
@Preview(showBackground = true)
@Composable
fun HomeScreenPreviewNew() {
    HomeScreen(navController = rememberNavController())
}
