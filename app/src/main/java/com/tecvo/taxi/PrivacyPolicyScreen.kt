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
fun PrivacyPolicyScreen(navController: NavController) {
    val scrollState = rememberScrollState()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Privacy Policy")
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
            // Privacy Policy content
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "PRIVACY POLICY",
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
                    text = "1. INTRODUCTION",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "TECVO TAXI is a real-time visibility service for South African taxi users, not a data collection app. Like Google Maps showing your location while navigating, our app temporarily shows your location only while actively using the taxi visibility service.\n\n" +
                            "Our Core Service: Provides 'birds eye view' of available taxis and passengers at pickup points - just like having eyes in the sky to see what's coming.",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "2. PRIVACY-BY-DESIGN APPROACH",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "WE ARE NOT A DATA COLLECTION APP\n\n" +
                            "Unlike social media, e-commerce, or analytics apps that collect and store your data permanently, TECVO TAXI operates on a service-based model:\n\n" +
                            "✅ Real-time visibility service (temporary operational data)\n" +
                            "❌ Not data collection (permanent storage for business purposes)\n" +
                            "❌ Not user profiling (no behavioral tracking)\n" +
                            "❌ Not data monetization (no selling or advertising use)",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "3. WHAT WE PROCESS TEMPORARILY",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "3.1 Real-Time Location Data (Temporary Service Operation)\n" +
                            "• What: Your GPS coordinates (latitude, longitude)\n" +
                            "• When: Only while actively using the map screen\n" +
                            "• Why: To show your position to other users going the same direction\n" +
                            "• Duration: AUTOMATICALLY DELETED when you leave the map screen\n" +
                            "• Like: Similar to showing your location during a WhatsApp live location share",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "3.2 Phone Authentication (Standard Login)\n" +
                            "• What: Phone number for login verification\n" +
                            "• Why: Standard app authentication (like WhatsApp, Uber, banking apps)\n" +
                            "• Storage: Firebase Auth service (Google's secure authentication system)\n" +
                            "• Not used for: Marketing, profiling, or data collection\n\n" +
                            "3.3 Service Selection Data (Temporary)\n" +
                            "• What: Your role (driver/passenger) and destination (town/local)\n" +
                            "• Duration: AUTOMATICALLY DELETED when service ends",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "4. HOW OUR SERVICE WORKS",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "1. You open app at pickup point → Select role and destination\n" +
                            "2. Real-time visibility starts → Your location appears on Firebase at drivers/town/{userId}\n" +
                            "3. Other users see you → Only users going same direction see your location\n" +
                            "4. You leave map screen → LOCATION AUTOMATICALLY DELETED from Firebase\n" +
                            "5. Service ends → No permanent data remains\n\n" +
                            "Key Point: Data exists only for immediate service delivery, not permanent storage.",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "5. DATA RETENTION (Minimal by Design)",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Real-Time Location Data:\n" +
                            "• Retention Period: 0 SECONDS after leaving map\n" +
                            "• Cleanup: Automatic via FirebaseCleanupUtil.removeUserData()\n" +
                            "• Purpose: Maintains visibility accuracy - if you're not there, you shouldn't appear on map\n\n" +
                            "Phone Authentication:\n" +
                            "• Retention Period: Until account deletion\n" +
                            "• Purpose: Standard app login (like any authenticated app)\n\n" +
                            "NO PERMANENT HISTORY:\n" +
                            "❌ No location history stored\n" +
                            "❌ No trip records kept\n" +
                            "❌ No behavioral data collected",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "6. YOUR CONTROL AND RIGHTS",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Immediate Control:\n" +
                            "• Start/Stop Service: Use or don't use the app\n" +
                            "• Leave Map: Instantly removes your location from system\n" +
                            "• Location Permissions: Control via device settings\n\n" +
                            "Account Control:\n" +
                            "• Delete Account: Removes phone auth record\n" +
                            "• Uninstall App: Removes all local data\n\n" +
                            "NO DATA DELETION REQUEST NEEDED:\n" +
                            "Since we don't store permanent personal data (only temporary service data), traditional 'data deletion requests' don't apply to our service model.",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "7. UNDERSTANDING THE DIFFERENCE",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "TECVO TAXI (Privacy-by-Design Service):\n" +
                            "✅ Temporary operational data for real-time visibility\n" +
                            "✅ Automatic cleanup when service not in use\n" +
                            "✅ No permanent user profiles or history\n" +
                            "✅ No data monetization or advertising\n\n" +
                            "Traditional Apps (Data Collection Model):\n" +
                            "❌ Permanent data storage for business purposes\n" +
                            "❌ User profiling and behavioral tracking\n" +
                            "❌ Data sharing for advertising/marketing\n" +
                            "❌ Complex deletion processes required",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "8. SECURITY & LEGAL COMPLIANCE",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Security Measures:\n" +
                            "• Encrypted transmission (HTTPS/TLS)\n" +
                            "• Firebase security rules prevent unauthorized access\n" +
                            "• Automatic data cleanup maintains system integrity\n" +
                            "• No permanent data storage reduces security risks\n\n" +
                            "South African POPIA Compliance:\n" +
                            "• Lawful Processing: Real-time visibility service with user consent\n" +
                            "• Purpose Limitation: Data used only for taxi visibility, not other purposes\n" +
                            "• Data Minimization: Only location data needed for immediate service\n" +
                            "• Retention Minimization: Automatic deletion when service not in use\n\n" +
                            "Children's Privacy:\n" +
                            "Our app is designed for adults using SA taxi services. Not intended for children under 18.",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "9. CONTACT US",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "TECVO (Pty) Ltd\n" +
                            "Email: privacy@tecvo.com\n" +
                            "Purpose: Real-time taxi visibility service for South African taxi industry\n" +
                            "Mission: Technology that extends natural abilities without changing behavior\n\n" +
                            "For questions about this privacy-by-design service model, contact us using the information above.",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "10. CONSENT & UNDERSTANDING",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "By using TECVO TAXI, you consent to this REAL-TIME VISIBILITY SERVICE and understand that:\n\n" +
                            "• Your location is shared temporarily while actively using the service\n" +
                            "• Data is automatically deleted when you stop using the service\n" +
                            "• This is operational service data, not permanent data collection\n" +
                            "• You maintain full control through standard app usage patterns\n\n" +
                            "This policy reflects our commitment to privacy-by-design principles in providing real-time taxi visibility services to the South African taxi industry.",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}