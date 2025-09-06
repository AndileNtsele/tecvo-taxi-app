package com.tecvo.taxi.screens.settingsscreen
import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.window.layout.WindowMetricsCalculator
import com.tecvo.taxi.Routes
import com.tecvo.taxi.models.Country
import com.tecvo.taxi.ui.theme.SettingsScreenCompactDimens
import com.tecvo.taxi.ui.theme.SettingsScreenCompactMediumDimens
import com.tecvo.taxi.ui.theme.SettingsScreenCompactSmallDimens
import com.tecvo.taxi.ui.theme.SettingsScreenExpandedDimens
import com.tecvo.taxi.ui.theme.SettingsScreenMediumDimens
import com.tecvo.taxi.utils.CountryUtils
import com.tecvo.taxi.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay
import timber.log.Timber

private const val TAG = "TaxiSettingsScreen"
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel() // Changed from viewModel()
) {
    Timber.tag(TAG).i("Initializing SettingsScreen")
    val context = LocalContext.current
    val activity = context as? Activity ?: run {
        Timber.tag(TAG).e("Context is not an Activity, cannot proceed")
        return
    }
    val windowMetrics = remember {
        WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(activity)
    }
    val screenWidthDp = windowMetrics.bounds.width() / context.resources.displayMetrics.density
    val dimens = when {
        screenWidthDp < 400f -> SettingsScreenCompactSmallDimens
        screenWidthDp in 400f..500f -> SettingsScreenCompactMediumDimens
        screenWidthDp in 500f..600f -> SettingsScreenCompactDimens
        screenWidthDp in 600f..840f -> SettingsScreenMediumDimens
        else -> SettingsScreenExpandedDimens
    }
    val topPadding = dimens.topPadding
    val sidePadding = dimens.sidePadding
    val buttonHeight = dimens.buttonHeight
    val iconSize = dimens.iconSize
    val cornerRadius = dimens.cornerRadius
    val titleTextSize = dimens.titleTextSize
    val spacerWidth = dimens.spacerWidth
// Collect ViewModel state
    val accountDeletionStep by viewModel.accountDeletionStep.collectAsState()
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val otpCode by viewModel.otpCode.collectAsState()
    val isProcessingDeletion by viewModel.isProcessingDeletion.collectAsState()
    val phoneError by viewModel.phoneError.collectAsState()
    val otpError by viewModel.otpError.collectAsState()
    val selectedCountry by viewModel.selectedCountry.collectAsState()
// Local UI state
    var isVerifyingPhone by remember { mutableStateOf(false) }
    var isVerifyingOtp by remember { mutableStateOf(false) }
    var verifyPhoneStartTime by remember { mutableLongStateOf(0L) }
    var verifyOtpStartTime by remember { mutableLongStateOf(0L) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showCountryDialog by remember { mutableStateOf(false) }
    var accountDeletionSuccess by remember { mutableStateOf(false) }
// Get shared preferences
    // Handle phone verification timeouts
    LaunchedEffect(verifyPhoneStartTime) {
        if (verifyPhoneStartTime > 0) {
            delay(10000)// 10 seconds timeout
            isVerifyingPhone = false
            verifyPhoneStartTime = 0
        }
    }
// Handle OTP verification timeouts
    LaunchedEffect(verifyOtpStartTime) {
        if (verifyOtpStartTime > 0) {
            delay(5000)// 5 seconds timeout
            isVerifyingOtp = false
            verifyOtpStartTime = 0
        }
    }
// Handle account deletion success
    LaunchedEffect(accountDeletionStep) {
        if (accountDeletionStep == 0 && (isVerifyingOtp || isProcessingDeletion)) {
// Account deletion completed
            isVerifyingOtp = false
            if (!isProcessingDeletion) {
                accountDeletionSuccess = true
            }
        }
    }
// Navigate after successful account deletion
    if (accountDeletionSuccess) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Account deleted successfully", Toast.LENGTH_LONG).show()
            navController.navigate(Routes.LOGIN) {
                popUpTo(Routes.HOME) { inclusive = true }
            }
        }
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings", fontSize = titleTextSize) },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(Routes.HOME)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Home"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = sidePadding, vertical = topPadding),
            verticalArrangement = Arrangement.spacedBy(topPadding)
        ) {
// Terms and Conditions Button
            SettingsButton(
                text = "Terms and Conditions",
                icon = Icons.Default.Description,
                onClick = { navController.navigate(Routes.TERMS_AND_CONDITIONS) },
                buttonHeight = buttonHeight,
                iconSize = iconSize,
                spacerWidth = spacerWidth,
                cornerRadius = cornerRadius
            )
// Privacy Policy Button
            SettingsButton(
                text = "Privacy Policy",
                icon = Icons.Default.PrivacyTip, // Using the privacy tip icon
                onClick = { navController.navigate(Routes.PRIVACY_POLICY) },
                buttonHeight = buttonHeight,
                iconSize = iconSize,
                spacerWidth = spacerWidth,
                cornerRadius = cornerRadius
            )
// Sign Out Button (for development/testing)
            SettingsButton(
                text = "Sign Out",
                icon = Icons.Default.Logout,
                onClick = { 
                    viewModel.signOut()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                buttonHeight = buttonHeight,
                iconSize = iconSize,
                spacerWidth = spacerWidth,
                cornerRadius = cornerRadius
            )
// Delete Account Button
            SettingsButton(
                text = "Delete Account",
                icon = Icons.Default.DeleteForever,
                onClick = { showDeleteAccountDialog = true },
                color = Color.Red,
                buttonHeight = buttonHeight,
                iconSize = iconSize,
                spacerWidth = spacerWidth,
                cornerRadius = cornerRadius
            )
        }
// Delete Account Warning Dialog
        if (showDeleteAccountDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteAccountDialog = false
                    viewModel.cancelAccountDeletion()
                },
                title = { Text("Delete Account", color = Color.Red) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "WARNING: You are about to delete your account",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                        Text("This action cannot be undone")
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFECEC)
                            )
                        ) {
                        Text(
                            "You will need to verify your identity through phone verification before account deletion."
                            ,
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteAccountDialog = false
                            viewModel.startAccountDeletion()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        )
                    ) {
                        Text("Proceed to Verification")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteAccountDialog = false
                        viewModel.cancelAccountDeletion()
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
// Step 1: Phone Verification Dialog
        if (accountDeletionStep == 1) {
            AlertDialog(
                onDismissRequest = {
                    viewModel.cancelAccountDeletion()
                },
                title = { Text("Step 1: Verify Phone Number") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Please enter your phone number to verify your identity")
                        if (phoneError.isNotEmpty()) {
                            Text(
                                text = phoneError,
                                color = Color.Red,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
// Country selection field
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .clickable { showCountryDialog = true }
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = selectedCountry.flagEmoji,
                                        fontSize = 24.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${selectedCountry.name} (${selectedCountry.dialCode})",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Rounded.ArrowDropDown,
                                    contentDescription = "Select country"
                                )
                            }
                        }
// Phone number input field
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { viewModel.updatePhoneNumber(it) },
                            label = { Text("Phone Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter your local number without country code") }
                        )
// Get local number example for the selected country
                        val localNumberExample = remember(selectedCountry) {
                            CountryUtils.getLocalNumberExample(selectedCountry.code)
                        }
// Helper text
                        Text(
                            "Example: $localNumberExample",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        Text(
                            "A verification code will be sent to this number",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
// Country picker dialog
                        if (showCountryDialog) {
                            CountryPickerDialog(
                                open = true,
                                onDismiss = { showCountryDialog = false },
                                onSelectCountry = { country ->
                                    viewModel.updateSelectedCountry(country)
                                    showCountryDialog = false
                                }
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (phoneNumber.isNotEmpty()) {
                                isVerifyingPhone = true
                                verifyPhoneStartTime = System.currentTimeMillis()
                                viewModel.sendVerificationCode(activity)
                            }
                        },
                        enabled = !isVerifyingPhone && phoneNumber.isNotEmpty()
                    ) {
                        if (isVerifyingPhone) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text("Send Verification Code")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.cancelAccountDeletion() }) {
                        Text("Cancel")
                    }
                }
            )
        }
// Step 2: OTP Verification Dialog
        if (accountDeletionStep == 2) {
            AlertDialog(
                onDismissRequest = {
                    viewModel.nextAccountDeletionStep()// Go back to step 1
                },
                title = { Text("Step 2: Enter Verification Code") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
// Show full international number with flag
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = selectedCountry.flagEmoji,
                                fontSize = 20.sp,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text(
                                "We've sent a verification code to ${selectedCountry.dialCode}$phoneNumber",
                                textAlign = TextAlign.Center
                            )
                        }
                        if (otpError.isNotEmpty()) {
                            Text(
                                text = otpError,
                                color = Color.Red,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = {
                                viewModel.updateOtpCode(it)
                            },
                            label = { Text("6-digit Code") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        TextButton(
                            onClick = {
                                isVerifyingOtp = true
                                verifyOtpStartTime = System.currentTimeMillis()
                                viewModel.sendVerificationCode(activity)// Resend code
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            enabled = !isVerifyingOtp
                        ) {
                            if (isVerifyingOtp) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Didn't receive a code? Resend")
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (otpCode.length == 6) {
                                isVerifyingOtp = true
                                viewModel.verifyOtpCode()
                            }
                        },
                        enabled = !isVerifyingOtp && !isProcessingDeletion && otpCode.length == 6,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        )
                    ) {
                        if (isVerifyingOtp || isProcessingDeletion) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text("Verify and Delete Account")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.nextAccountDeletionStep() }) {// Go back to step 1
                        Text("Back")
                    }
                }
            )
        }
    }
}

@Composable
private fun SettingsButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.primary,
    buttonHeight: Dp,
    iconSize: Dp,
    spacerWidth: Dp,
    cornerRadius: Dp
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(buttonHeight),
        shape = RoundedCornerShape(cornerRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (color == Color.Red) Color.Red else MaterialTheme.colorScheme.primary
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.width(spacerWidth))
            Text(text = text)
        }
    }
}
@Composable
fun CountryPickerDialog(
    open: Boolean,
    onDismiss: () -> Unit,
    onSelectCountry: (Country) -> Unit
) {
    if (!open) return
    var searchQuery by remember { mutableStateOf("") }
    val filteredCountries = remember(searchQuery) {
        CountryUtils.allCountries.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.dialCode.contains(searchQuery, ignoreCase = true)
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Your Country") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search country") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    filteredCountries.forEach { country ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectCountry(country) }
                                .padding(vertical = 8.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = country.flagEmoji,
                                fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(country.name)
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
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    val navController = rememberNavController()
    SettingsScreen(navController)
}