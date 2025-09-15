@file:OptIn(ExperimentalMaterial3Api::class)

package com.tecvo.taxi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController


@Composable
fun TermsAndConditionsScreen(navController: NavController) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Terms and Conditions")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Terms and Conditions content
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "TERMS OF SERVICE",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Last Updated: September 2025",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "1. AGREEMENT TO TERMS",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "These Terms of Service (\"Terms\") constitute a legally binding agreement between you (\"User\", \"you\", or \"your\") and Tecvo Pty Ltd (\"Company\", \"we\", \"us\", or \"our\"), governing your access to and use of the Taxi mobile application (the \"App\").\n\n" +
                            "By downloading, accessing, or using the App, you acknowledge that you have read, understood, and agree to be bound by these Terms. If you do not agree to these Terms, you must not access or use the App.",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "2. SERVICE DESCRIPTION",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "2.1 Map Visualization Tool. The App is a location-based map visualization tool that\n" +
                            "      a) Shows commuters the real-time location of available taxis in their vicinity\n" +
                            "      b) Shows drivers the real-time location of potential commuters in their vicinity",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "2.2 Destination Selection. The App allows users to specify their intended route:\n" +
                            "      a) \"Town\" - for intercity travel\n" +
                            "      b) \"Local\" - for travel within a specific area",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "2.3 Not a Booking Service. The App does not:\n" +
                            "      a) Process ride requests or bookings\n" +
                            "      b) Facilitate payments between commuters",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                // Add more terms as needed

                Text(
                    text = "3. USER RESPONSIBILITIES",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "3.1 Location Services. Users must enable location services for the App to function properly.\n\n" +
                            "3.2 Accurate Information. Users must provide accurate information about their identity and destination.\n\n" +
                            "3.3 Appropriate Use. Users must use the App in accordance with applicable laws and regulations.",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "4. PRIVACY POLICY",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "4.1 Privacy-by-Design Service. The App operates as a real-time visibility service, not a data collection app:\n" +
                            "• Location data is processed temporarily only while actively using the map\n" +
                            "• All location data is automatically deleted when you leave the map screen\n" +
                            "• No permanent location history or trip records are stored\n\n" +
                            "4.2 Foreground-Only Operation. The App:\n" +
                            "• Only accesses location while you are actively using the map\n" +
                            "• Does NOT track your location when the app is closed\n" +
                            "• Maintains visibility accuracy by automatic cleanup\n\n" +
                            "4.3 Service-Based Model. Unlike data collection apps, we:\n" +
                            "• Provide temporary visibility service, not permanent data storage\n" +
                            "• Do not profile users or collect behavioral data\n" +
                            "• Do not monetize or sell user data",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}