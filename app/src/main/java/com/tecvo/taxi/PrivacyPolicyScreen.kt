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
                    text = "Last Updated: April 6, 2025",
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
                    text = "Tecvo Pty Ltd (\"we,\" \"our,\" or \"us\") is committed to protecting your privacy. This Privacy Policy explains how we collect, use, disclose, and safeguard your information when you use our Taxi mobile application (the \"App\").\n\n" +
                            "Please read this Privacy Policy carefully. By downloading, accessing, or using the App, you consent to the collection and use of your information as described here.",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "2. INFORMATION WE COLLECT",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "2.1 Location Information\n" +
                            "• Precise location data (latitude, longitude) collected when the App is in use\n" +
                            "• Background location data when the App is running in the background\n" +
                            "• Destination preferences (\"town\" or \"local\") selected within the App",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "2.2 User Identity Information\n" +
                            "• Phone number used for authentication\n" +
                            "• User role (driver or passenger) selected by you\n" +
                            "• Firebase User ID automatically generated upon registration",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "3. HOW WE USE YOUR INFORMATION",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "We use the information we collect solely for the purpose of providing and improving the App's services:\n\n" +
                            "• To show the real-time location of available taxis to passengers\n" +
                            "• To show the real-time location of potential passengers to drivers\n" +
                            "• To authenticate your identity when you log in\n\n" +
                            "We do not use your data for marketing, advertising, or analytics purposes at this time. The App is offered free of charge for at least the first year as we build our user base. Like other messaging apps, you will need to have your own mobile data plan to use the App.",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "4. DATA SHARING PRACTICES",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "4.1 Third-Party Sharing\n" +
                            "We do not sell, trade, or otherwise transfer your personal information to outside parties except in the following cases:\n\n" +
                            "• Firebase: We use Google Firebase for authentication and real-time database services. Firebase collects and processes data according to their privacy policy.\n" +
                            "• Google Maps: We use Google Maps API to provide mapping functionality. Your location data is processed by Google according to their privacy policy.\n" +
                            "• Legal Requirements: We may disclose your information if required by law or in response to valid requests by public authorities.",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "5. DATA RETENTION POLICY",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "5.1 Storage Period\n" +
                            "• Account information is retained for as long as you maintain an active account\n" +
                            "• Location data is stored temporarily and updated with your current position\n" +
                            "• When you close the App or log out, your real-time location is removed from our database\n\n" +
                            "5.2 Account Deletion\n" +
                            "When you delete your account, all personal information associated with your account will be deleted from our active databases within 30 days.",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "6. USER RIGHTS",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "You have the following rights regarding your personal information:\n\n" +
                            "• Access: You can access your personal information through the App settings\n" +
                            "• Correction: You can update your profile information at any time\n" +
                            "• Deletion: You can delete your account through the settings menu\n" +
                            "• Data Portability: You can request a copy of your personal data by contacting us\n\n" +
                            "To exercise these rights, please use the relevant features in the App or contact us using the information provided in Section 9.",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "7. SECURITY MEASURES",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "We implement appropriate technical and organizational measures to protect your personal information, including:\n\n" +
                            "• Secure authentication through Firebase Authentication\n" +
                            "• Data encryption during transmission\n" +
                            "• Secure database storage with proper access controls\n" +
                            "• Regular security assessments and updates\n\n" +
                            "While we strive to use commercially acceptable means to protect your personal information, no method of transmission over the internet or electronic storage is 100% secure, and we cannot guarantee absolute security.",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "8. CHILDREN'S PRIVACY",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Our App is not intended for children under 13 years of age, and we do not knowingly collect personal information from children under 13. If we discover that a child under 13 has provided us with personal information, we will delete such information from our servers immediately. If you are a parent or guardian and you believe your child has provided us with personal information, please contact us.",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "9. CONTACT INFORMATION",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "If you have any questions, concerns, or requests regarding this Privacy Policy or our data practices, please contact us at:\n\n" +
                            "Tecvo Pty Ltd\n" +
                            "Email: privacy@tecvo.com\n" +
                            "Address: 123 Main Street, Pretoria, South Africa",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "10. UPDATES TO THIS POLICY",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "We may update this Privacy Policy from time to time to reflect changes in our practices or for other operational, legal, or regulatory reasons. We will notify you of any material changes by:\n\n" +
                            "• Posting the updated Privacy Policy in the App\n" +
                            "• Updating the \"Last Updated\" date at the top of this policy\n" +
                            "• Sending you a notification within the App\n\n" +
                            "We encourage you to review this Privacy Policy periodically to stay informed about our data practices.",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}