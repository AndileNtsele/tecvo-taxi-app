@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
package com.tecvo.taxi.screens.loginscreens
// Add these imports
import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tecvo.taxi.BuildConfig
import com.tecvo.taxi.R
import com.tecvo.taxi.Routes
import com.tecvo.taxi.models.Country
import com.tecvo.taxi.ui.theme.LoginScreenCompactDimens
import com.tecvo.taxi.ui.theme.LoginScreenCompactMediumDimens
import com.tecvo.taxi.ui.theme.LoginScreenCompactSmallDimens
import com.tecvo.taxi.ui.theme.LoginScreenExpandedDimens
import com.tecvo.taxi.ui.theme.LoginScreenMediumDimens
import com.tecvo.taxi.utils.CountryUtils
import com.tecvo.taxi.viewmodel.LoginState
import com.tecvo.taxi.viewmodel.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import timber.log.Timber

private const val TAG = "LoginScreen"
@Composable
fun LoginScreen(
    navController: NavController,
    handlePostRegistrationPermissions: (NavController) -> Unit,
    viewModel: LoginViewModel = hiltViewModel() // Changed from viewModel()
) {
    // Performance Optimization: Reduce initialization and dimension logging overhead
    LaunchedEffect(Unit) {
        if (BuildConfig.DEBUG) {
            Timber.tag(TAG).i("UI: Initializing Login Screen")
        }
    }
    
    // Performance Optimization: Combine screen width calculation and dimension selection
    val context = LocalContext.current
    val dimens = remember(context) {
        val displayMetrics = context.resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density


        when {
            screenWidthDp < 400f -> LoginScreenCompactSmallDimens
            screenWidthDp in 400f..500f -> LoginScreenCompactMediumDimens
            screenWidthDp in 500f..600f -> LoginScreenCompactDimens
            screenWidthDp in 600f..840f -> LoginScreenMediumDimens
            else -> LoginScreenExpandedDimens
        }
    }
// 2) Expose dimension fields for easier access
    val medium1 = dimens.medium1
    val logoSize = dimens.logoSize
    val scrollState = rememberScrollState()

    // Collect states from ViewModel
    val loginState by viewModel.loginState.collectAsState()
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val otp by viewModel.otp.collectAsState()
    val isOtpSent by viewModel.isOtpSent.collectAsState()
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val termsAccepted by viewModel.termsAccepted.collectAsState()
    val error by viewModel.error.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val showLoginForm by viewModel.showLoginForm.collectAsState()
// Local UI state only
    var isImeVisible by remember { mutableStateOf(false) }
// Check for network availability
    val isNetworkAvailable = remember { isNetworkAvailable(context) }
// Handle keyboard visibility
    val view = LocalView.current

    LaunchedEffect(view) {
        Timber.tag(TAG).d("UI: Setting up keyboard visibility listener")
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val keyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            if (keyboardVisible != isImeVisible) {
                Timber.tag(TAG)
                    .d("UI: Keyboard visibility changed to: ${if (keyboardVisible) "visible" else "hidden"}")
                isImeVisible = keyboardVisible
            }
            insets
        }
    }
// Adjust system bar color - UPDATED to use EdgeToEdge instead of SystemUiController
    LaunchedEffect(Unit) {
        Timber.tag(TAG).d("UI: Setting up edge-to-edge display")
        val activity = (context as? ComponentActivity)
        activity?.let {
            WindowCompat.setDecorFitsSystemWindows(it.window, false)
            it.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.dark(
                    Color(0xFF0A1F44).toArgb()
                )
            )
        } ?: run {
            // Fallback for non-ComponentActivity contexts
            (context as? Activity)?.let { act ->
                WindowCompat.setDecorFitsSystemWindows(act.window, false)
                WindowInsetsControllerCompat(act.window, act.window.decorView).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
                @Suppress("DEPRECATION")
                act.window.statusBarColor = Color(0xFF0A1F44).toArgb()
            }
        }
    }
// Track screen view for analytics
    DisposableEffect(Unit) {
        Timber.tag(TAG).i("User Flow: User viewing login/registration screen")
        onDispose {
            Timber.tag(TAG).i("User Flow: User leaving login/registration screen")
        }
    }
// Optimize animation startup
    LaunchedEffect(Unit) {
        Timber.tag(TAG).d("UI: Setting up initial animations")
// Reduce initial delay to improve perceived performance
        delay(500)
        withContext(Dispatchers.Default) {
// Prepare any data needed for the form in background
// ...
        }
        viewModel.showLoginForm()
    }
// Navigate on successful login with permission checks
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            Timber.tag(TAG).i("User Flow: Login successful, proceeding to permission flow")
// Remove unnecessary delay
            handlePostRegistrationPermissions(navController)
        }
    }
// ------------------ MAIN CONTAINER ------------------
    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
// Gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0A1F44), // Dark blue at the top
                            Color(0xFF16294B) // Slightly lighter blue at the bottom
                        )
                    )
                )
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
// Animated visibility for the top logo (only when not typing and not logged in)
            AnimatedVisibility(
                visible = !isImeVisible && !isLoggedIn,
                enter = fadeIn(animationSpec = tween(durationMillis = 500)) +
                        expandVertically(animationSpec = tween(durationMillis = 500)),
                exit = fadeOut() + shrinkVertically()
            ) {
                Timber.tag(TAG).d("UI: Showing app logo animation")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = medium1),
                    contentAlignment = Alignment.Center
                ) {
// Modernized logo presentation with shadow effect
                    Image(
                        painter = painterResource(id = R.drawable.applogo),
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .size(logoSize)
                            .shadow(
                                elevation = 12.dp,
                                shape = CircleShape,spotColor = Color(0xFF001A66).copy(alpha = 0.5f)
                            ),
                        contentScale = ContentScale.Fit
                    )
                }
            }
// ------------------ LOGIN FORM ------------------
            if (showLoginForm && !isLoggedIn) {
                Timber.tag(TAG).d("UI: Displaying login form, OTP sent: $isOtpSent")
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White,
                    shape = RoundedCornerShape(
                        topStart = 28.dp,
                        topEnd = 28.dp
                    ),
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = 32.dp,
                                bottom = 24.dp,
                                start = 24.dp,
                                end = 24.dp
                            )
                            .verticalScroll(scrollState),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text(
                            text = "Register",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = dimens.loginTextSize,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
// ------------------ PHONE & OTP ------------------
                        if (!isOtpSent) {
                            ModernPhoneNumberField(
                                selectedCountry = selectedCountry,
                                onCountrySelected = { chosenCountry ->
                                    Timber.tag(TAG).i(
                                        "User Action: Selected country ${chosenCountry.name} (${
                                            chosenCountry.dialCode
                                        })"
                                    )
                                    viewModel.updateSelectedCountry(chosenCountry)
                                },
                                phoneNumber = phoneNumber,
                                onPhoneNumberChange = { newValue ->
                                    viewModel.updatePhoneNumber(newValue)
                                }
                            )
                        } else {
                            ModernOtpField(
                                otp = otp,
                                onOtpChange = {
                                    viewModel.updateOtp(it)
                                },
                                onSubmit = {
                                    if (otp.length == 6) {
                                        Timber.tag(TAG)
                                            .i("User Action: Submitting OTP verification")
                                        viewModel.verifyOtpCode()
                                    }
                                }
                            )
                        }
// ------------------ TERMS AND CONDITIONS CHECKBOX ------------------
                        if (!isOtpSent) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = termsAccepted,
                                    onCheckedChange = {
                                        Timber.tag(TAG)
                                            .i("User Action: Terms and conditions ${if (it) "accepted" else "declined"}")
                                        viewModel.toggleTerms(it)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "I agree to the Terms of Service and Privacy Policy",style = MaterialTheme.
                                    typography.bodySmall,
                                    modifier = Modifier.clickable {
                                        Timber.tag(TAG).i("User Action: Viewing Terms and Conditions")
                                        navController.navigate(Routes.TERMS_AND_CONDITIONS)
                                    }
                                )
                            }
                        }
// ------------------ ERROR MESSAGE ------------------
                        if (error != null) {
                            Timber.tag(TAG).d("UI: Showing login error: $error")
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFF3F3)
                                ),
                                border = BorderStroke(1.dp, Color(0xFFFFCCCC)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = "Error",
                                        tint = Color.Red,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = error ?: "",
                                        color = Color.Red,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
// ------------------ OFFLINE MESSAGE ------------------
                        if (!isNetworkAvailable) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFF3F3)
                                ),
                                border = BorderStroke(1.dp, Color(0xFFFFCCCC)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SignalWifiOff,
                                        contentDescription = "Offline",
                                        tint = Color.Red,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "Internet connection required to register. Please connect to the internet and try again.",color = Color.Red,style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
// ------------------ LOGIN BUTTON ------------------
                        val buttonEnabled = ((!isOtpSent && CountryUtils.isValidLocalPhoneNumber(phoneNumber,
                            selectedCountry) && termsAccepted) ||
                                (isOtpSent && otp.length == 6))
                        ElevatedButton(
                            onClick = {
                                if (!isOtpSent) {
                                    Timber.tag(TAG).i("User Action: Clicked verify phone button")
                                    viewModel.verifyPhoneNumber(context as Activity)
                                } else {
                                    Timber.tag(TAG).i("User Action: Clicked verify OTP button")
                                    viewModel.verifyOtpCode()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = buttonEnabled,
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.elevatedButtonElevation(
                                defaultElevation = 6.dp,pressedElevation = 8.dp
                            )
                        ) {
                            when (loginState) {
                                is LoginState.Loading -> {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 3.dp
                                    )
                                }
                                else -> {
                                    Text(
                                        text = if (isOtpSent) "Login" else "Verify Phone Number",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                        Timber.tag(TAG).d("UI: Login button enabled: $buttonEnabled")
                    }
                }
            }
        }
    }
}
// Helper function to check network availability
private fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkCapabilities = connectivityManager.activeNetwork ?: return false
    val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
    return actNw.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
// ------------------ Modernized Phone Number Field ------------------
@Composable
fun ModernPhoneNumberField(
    selectedCountry: Country,
    onCountrySelected: (Country) -> Unit,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit
) {
    Timber.tag(TAG).d("UI: Rendering phone number field with country ${selectedCountry.name}")
    var showDialog by remember { mutableStateOf(false) }
    val isValid = CountryUtils.isValidLocalPhoneNumber(phoneNumber, selectedCountry)
    val phoneError = if (!isValid && phoneNumber.isNotEmpty()) {
        "Invalid number for ${selectedCountry.name}"
    } else null
// Get local number example for the selected country
    val localNumberExample = remember(selectedCountry) {
        CountryUtils.getLocalNumberExample(selectedCountry.code)
    }
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Phone Number",
            style = MaterialTheme.typography.labelLarge,
            color = Color.Gray,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5)
            ),
            border = BorderStroke(
                width = 1.dp,
                color = if (phoneError != null) Color.Red else Color(0xFFE0E0E0)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
// Country selector
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            Timber.tag(TAG).d("User Action: Opening country selector dialog")
                            showDialog = true
                        }.background(Color(0xFFEBEBEB))
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedCountry.flagEmoji,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = selectedCountry.dialCode,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Icon(
                            imageVector = Icons.Rounded.ArrowDropDown,
                            contentDescription = "Select country",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
// Phone number field with local number example
                BasicTextField(
                    value = phoneNumber,
                    onValueChange = {
                        Timber.tag(TAG).d("User Input: Phone number updated")
                        onPhoneNumberChange(it)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 12.dp),
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = Color.Black
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Phone
                    ),
                    decorationBox = { innerTextField ->
                        Box(modifier = Modifier.fillMaxWidth()) {
                            if (phoneNumber.isEmpty()) {
                                Text(
                                    text = "Example: $localNumberExample", // Local format example
                                    color = Color.Gray,
                                    fontSize = 16.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                Icon(
                    imageVector = Icons.Rounded.Phone,
                    contentDescription = "Phone",
                    tint = Color.Gray,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
        }
        phoneError?.let { errorMsg ->
            Timber.tag(TAG).w("Validation: Phone validation error - $errorMsg")
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Error",
                    tint = Color.Red,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = errorMsg,
                    color = Color.Red,
                    fontSize = 14.sp
                )
            }
        }
// Helper text to explain local number format
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Enter your local number without country code",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(start = 4.dp))
    }
    if (showDialog) {
        Timber.tag(TAG).d("UI: Showing country picker dialog")
        ModernCountryPickerDialog(
            open = true,
            onDismiss = {
                Timber.tag(TAG).d("User Action: Dismissed country picker dialog")
                showDialog = false
            },
            onSelectCountry = { country ->
                Timber.tag(TAG)
                    .i("User Action: Selected country ${country.name} (${country.dialCode})")
                onCountrySelected(country)
                showDialog = false
            }
        )
    }
}
// ------------------ Modernized OTP Field ------------------
@Composable
fun ModernOtpField(
    otp: String,
    onOtpChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Timber.tag(TAG).d("UI: Rendering OTP input field")
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Enter OTP",
            style = MaterialTheme.typography.labelLarge,
            color = Color.Gray,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5)
            ),
            border = BorderStroke(1.dp, Color(0xFFE0E0E0))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = otp,
                    onValueChange = {
                        if (it.length <= 6) {
                            Timber.tag(TAG).d("User Input: OTP updated (${it.length}/6 digits)")
                            onOtpChange(it)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        letterSpacing = 4.sp,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (otp.length == 6) {
                                Timber.tag(TAG).i("User Action: Submitting OTP via keyboard done")
                                onSubmit()
                            } else {
                                Timber.tag(TAG)
                                    .d("Validation: OTP not yet complete (${otp.length}/6)")
                            }
                        }
                    ),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (otp.isEmpty()) {
                                Text(
                                    text = "Enter 6-digit code",
                                    color = Color.Gray,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center)
                            }
                            innerTextField()
                        }
                    }
                )
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = "OTP",
                    tint = Color.Gray
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "We've sent a verification code to your phone",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
// ------------------ Modernized Country Picker Dialog ------------------
@Composable
fun ModernCountryPickerDialog(
    open: Boolean,
    onDismiss: () -> Unit,
    onSelectCountry: (Country) -> Unit
) {
    Timber.tag(TAG).d("UI: Initializing country picker dialog")
    var searchQuery by remember { mutableStateOf("") }
    val filteredCountries = remember(searchQuery) {
        val filtered = CountryUtils.allCountries.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.dialCode.contains(searchQuery, ignoreCase = true)
        }
        Timber.tag(TAG).d("Search: Found ${filtered.size} countries matching query '$searchQuery'")
        filtered
    }
    if (open) {
        AlertDialog(
            onDismissRequest = {
                Timber.tag(TAG).d("User Action: Dismissed country picker dialog")
                onDismiss()
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = {
                        Timber.tag(TAG).d("User Action: Canceled country selection")
                        onDismiss()
                    },
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            title = {
                Text(
                    "Select Your Country",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            Timber.tag(TAG).d("User Input: Searching for country: $it")
                            searchQuery = it
                        },
                        placeholder = { Text("Search by name or code") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = Color.LightGray,
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5)
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.Gray)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredCountries) { country ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        Timber.tag(TAG)
                                            .i("User Action: Selected country ${country.name} (${country.dialCode})")
                                        onSelectCountry(country)
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFF5F5F5)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp, horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        country.flagEmoji,
                                        fontSize = 24.sp
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            country.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                        Text(
                                            country.dialCode,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}
// ---------------------- Preview ----------------------
@Composable
@Preview(showBackground = true)
fun LoginScreenPreview() {
    Timber.tag(TAG).d("Preview: Generating LoginScreen preview")
    LoginScreen(
        navController = rememberNavController(),
        handlePostRegistrationPermissions = {}
    )
}