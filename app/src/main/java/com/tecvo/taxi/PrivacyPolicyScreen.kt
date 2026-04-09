@file:OptIn(ExperimentalMaterial3Api::class)

package com.tecvo.taxi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// ── Brand colour tokens ───────────────────────────────────────────────────────
private val PPBlack    = Color(0xFF000000)
private val PPCharcoal = Color(0xFF1D1D1F)
private val PPSlate    = Color(0xFF86868B)
private val PPCloud    = Color(0xFFE0E0E5)
private val PPMist     = Color(0xFFF4F4F6)
private val PPWhite    = Color(0xFFFFFFFF)

// ── Shared composables ────────────────────────────────────────────────────────

@Composable
private fun PPSectionBlock(number: String, title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(number, fontSize = 10.sp, fontWeight = FontWeight.Normal,
            letterSpacing = 3.sp, color = PPSlate, modifier = Modifier.padding(bottom = 6.dp))
        Text(title, fontSize = 19.sp, fontWeight = FontWeight.Light,
            letterSpacing = 0.5.sp, color = PPBlack)
        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = PPCloud, thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))
        content()
    }
}

@Composable
private fun PPBodyText(text: String, modifier: Modifier = Modifier) {
    Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Light,
        lineHeight = 22.sp, color = PPCharcoal, modifier = modifier.fillMaxWidth())
}

@Composable
private fun PPBulletItem(text: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.Top) {
        Box(modifier = Modifier.padding(top = 8.dp).size(4.dp).background(PPSlate, shape = CircleShape))
        Spacer(modifier = Modifier.width(14.dp))
        Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.Light,
            lineHeight = 20.sp, color = PPCharcoal)
    }
    HorizontalDivider(color = PPCloud, thickness = 0.5.dp)
}

@Composable
private fun PPSubsectionLabel(text: String) {
    Text(text = text.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Normal,
        letterSpacing = 2.sp, color = PPCharcoal,
        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp))
}

@Composable
private fun PPNoteBox(text: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
        .background(PPMist).padding(horizontal = 16.dp, vertical = 14.dp)) {
        Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Light,
            lineHeight = 19.sp, color = PPSlate)
    }
}

@Composable
private fun PPStepItem(number: String, text: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp), verticalAlignment = Alignment.Top) {
        Text(text = number, fontSize = 9.sp, fontWeight = FontWeight.Normal,
            letterSpacing = 2.sp, color = PPSlate,
            modifier = Modifier.width(28.dp).padding(top = 2.dp))
        Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.Light,
            lineHeight = 20.sp, color = PPCharcoal, modifier = Modifier.weight(1f))
    }
    HorizontalDivider(color = PPCloud, thickness = 0.5.dp)
}

@Composable
private fun PPDataTableHeader() {
    Row(modifier = Modifier.fillMaxWidth().background(PPMist)
        .padding(horizontal = 12.dp, vertical = 8.dp)) {
        Text("DATA", fontSize = 9.sp, fontWeight = FontWeight.Normal,
            letterSpacing = 2.sp, color = PPSlate, modifier = Modifier.weight(1.2f))
        Text("WHEN", fontSize = 9.sp, fontWeight = FontWeight.Normal,
            letterSpacing = 2.sp, color = PPSlate, modifier = Modifier.weight(1.5f))
        Text("DELETED", fontSize = 9.sp, fontWeight = FontWeight.Normal,
            letterSpacing = 2.sp, color = PPSlate, modifier = Modifier.weight(1.3f))
    }
    HorizontalDivider(color = PPBlack, thickness = 1.dp)
}

@Composable
private fun PPDataTableRow(data: String, `when`: String, deleted: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top) {
        Text(data, fontSize = 12.sp, fontWeight = FontWeight.Light,
            lineHeight = 18.sp, color = PPCharcoal, modifier = Modifier.weight(1.2f))
        Text(`when`, fontSize = 12.sp, fontWeight = FontWeight.Light,
            lineHeight = 18.sp, color = PPCharcoal, modifier = Modifier.weight(1.5f))
        Text(deleted, fontSize = 12.sp, fontWeight = FontWeight.Light,
            lineHeight = 18.sp, color = PPCharcoal, modifier = Modifier.weight(1.3f))
    }
    HorizontalDivider(color = PPCloud, thickness = 0.5.dp)
}

// ── Main Screen ───────────────────────────────────────────────────────────────

@Composable
fun PrivacyPolicyScreen(navController: NavController) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("A PRODUCT BY TECVO (PTY) LTD", fontSize = 9.sp,
                            fontWeight = FontWeight.Normal, letterSpacing = 2.5.sp, color = PPSlate)
                        Text("Taxi", fontSize = 22.sp, fontWeight = FontWeight.Light,
                            letterSpacing = 1.sp, color = PPBlack)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back", tint = PPBlack)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PPWhite)
            )
        },
        containerColor = PPWhite
    ) { paddingValues ->

        Column(modifier = Modifier.fillMaxSize()
            .padding(paddingValues).verticalScroll(scrollState)
        ) {
            // ── Document header ───────────────────────────────────────────
            Column(modifier = Modifier.fillMaxWidth().background(PPWhite)
                .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Text("PRIVACY POLICY", fontSize = 11.sp, fontWeight = FontWeight.Normal,
                    letterSpacing = 2.5.sp, color = PPSlate)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Last updated: April 2026  ·  Version 1.1",
                    fontSize = 11.sp, fontWeight = FontWeight.Light,
                    letterSpacing = 0.5.sp, color = PPSlate)
            }

            HorizontalDivider(color = PPCloud, thickness = 1.dp)

            // ── Intro box ─────────────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp)) {
                Box(modifier = Modifier.width(2.dp).height(112.dp).background(PPBlack))
                Spacer(modifier = Modifier.width(16.dp))
                Box(modifier = Modifier.fillMaxWidth().background(PPMist).padding(16.dp)) {
                    Text(
                        text = "The Taxi app is a real-time visibility and community service for South African commuters and taxi drivers. Your location is processed temporarily while you use the map and removed automatically the moment you leave. Community features — such as chat profiles and messages — are stored only for as long as they exist in the app, and you can delete them at any time.",
                        fontSize = 14.sp, fontWeight = FontWeight.Light,
                        lineHeight = 22.sp, color = PPCharcoal
                    )
                }
            }

            HorizontalDivider(color = PPCloud, thickness = 1.dp)

            // ── Sections ──────────────────────────────────────────────────
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(40.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // 01 — What This App Is
                PPSectionBlock("01", "What This App Is") {
                    PPBodyText("Taxi (by TECVO) gives drivers and passengers a real-time birds-eye view of who is at taxi pickup points and where they are going. It also includes Community Chat — city-based public chat rooms for the taxi community — and Announcements posted by verified taxi associations.")
                    Spacer(modifier = Modifier.height(10.dp))
                    PPBodyText("Like Google Maps showing your location during navigation, or WhatsApp's live location feature, Taxi processes your location only while the map service is actively in use. The moment the service ends, the location data is gone. Community Chat data is persistent but fully deletable by you.")
                }

                // 02 — What We Process and Why
                PPSectionBlock("02", "What We Process and Why") {
                    PPBodyText("We process the minimum amount of information required to deliver each feature. The table below covers everything.")
                    Spacer(modifier = Modifier.height(14.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        PPDataTableHeader()
                        PPDataTableRow("GPS coordinates", "While map screen is active", "Auto on map exit")
                        PPDataTableRow("Role & direction\n(driver/passenger\n· town/local)", "While using the map", "Auto when service ends")
                        PPDataTableRow("Phone number", "At login only", "On account deletion")
                        PPDataTableRow("Chat display name", "When you register for Community Chat", "On profile or account deletion")
                        PPDataTableRow("Profile picture", "When you upload one", "On profile or account deletion")
                        PPDataTableRow("Chat messages", "When you send a message", "When you delete the message or account")
                        PPDataTableRow("Reactions\n(likes/dislikes)", "When you react to a message", "When the message is deleted")
                        PPDataTableRow("City & province", "When using Chat or Announcements", "Not stored beyond session")
                        PPDataTableRow("Association data", "When an association registers", "On association deletion request")
                        PPDataTableRow("App preferences", "Ongoing", "On uninstall / reset")
                        PPDataTableRow("Crash reports", "If app crashes", "90 days, auto-deleted")
                        PPDataTableRow("Aggregated usage", "Ongoing", "14 months, anonymised")
                    }
                    PPNoteBox("What we do not collect: location history, trip records, behavioural profiles, advertising data, or any information not listed above.")
                }

                // 03 — How the Map Service Works
                PPSectionBlock("03", "How the Map Service Works") {
                    HorizontalDivider(color = PPCloud, thickness = 0.5.dp)
                    PPStepItem("01", "You walk to your usual taxi pickup point — nothing about your routine changes.")
                    PPStepItem("02", "You open the app, select your role (Driver or Passenger) and your direction (TOWN or LOCAL).")
                    PPStepItem("03", "The live map shows your position to other users going the same direction. Passengers see taxis. Drivers see passengers.")
                    PPStepItem("04", "You leave the map screen. Your location is removed from the system immediately and automatically. No location data remains.")
                }

                // 04 — Community Chat and Your Data
                PPSectionBlock("04", "Community Chat and Your Data") {
                    PPBodyText("Community Chat is a public, city-based feature. When you participate, your display name, profile picture, and messages are visible to all app users in the same city.")
                    Spacer(modifier = Modifier.height(10.dp))
                    PPBodyText("You have full control over your Community Chat data at any time:")
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = PPCloud, thickness = 0.5.dp)
                    PPBulletItem("Delete a message — tap and hold any message you sent and select Delete. The message is removed from Firebase immediately.")
                    PPBulletItem("Delete your chat profile — go to your profile in Community Chat and select Delete Profile. Your display name and profile picture are deleted immediately from Firebase and Firebase Storage. Messages you sent may remain in chat threads unless you individually delete them.")
                    PPBulletItem("Delete your account — removes your phone number, chat profile, profile picture, and all associated app data. See Section 06 for full instructions.")
                    PPNoteBox("Community Chat messages are stored in Firebase Firestore. They are not ephemeral — they remain until you delete them or delete your account.")
                }

                // 05 — Third-Party Services
                PPSectionBlock("05", "Third-Party Services") {
                    PPBodyText("We use the following services to deliver the app. Each is operationally necessary — we use nothing else.")
                    PPSubsectionLabel("Firebase Firestore & Realtime Database (Google)")
                    PPBodyText("Powers real-time location visibility, Community Chat messages, and Announcements. Temporary location data is written and removed in real time. Chat messages and profiles are stored persistently until deleted. Google's Firebase Privacy Policy applies.")
                    PPSubsectionLabel("Firebase Authentication (Google)")
                    PPBodyText("Handles phone number login using OTP verification. Your phone number is stored securely by Firebase for authentication purposes only.")
                    PPSubsectionLabel("Firebase Storage (Google)")
                    PPBodyText("Stores profile pictures uploaded during Community Chat registration. Images are stored under your user ID and are deleted when you delete your chat profile or account. Google's Firebase Privacy Policy applies.")
                    PPSubsectionLabel("Google Maps Platform")
                    PPBodyText("Displays the map and processes location data for geocoding. Google's Privacy Policy applies.")
                    PPSubsectionLabel("Firebase Crashlytics & Analytics")
                    PPBodyText("Crashlytics captures anonymised crash data (device model, OS version, app version, crash context) to help us fix bugs. Analytics captures aggregated usage patterns. Neither service has access to your location data, phone number, or chat messages.")
                }

                // 06 — Your Rights Under POPIA
                PPSectionBlock("06", "Your Rights Under POPIA") {
                    PPBodyText("The Protection of Personal Information Act gives you the following rights in relation to your personal information:")
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = PPCloud, thickness = 0.5.dp)
                    PPBulletItem("Access — request a copy of the personal information we hold about you.")
                    PPBulletItem("Correction — request correction of inaccurate information.")
                    PPBulletItem("Deletion — delete your chat profile, individual messages, or your entire account directly in the app. You may also request deletion by email.")
                    PPBulletItem("Objection — object to processing of your information.")
                    PPBulletItem("Complaint — lodge a complaint with South Africa's Information Regulator at inforeg@justice.gov.za")
                    PPNoteBox("To delete your account: Open the app → Settings → Delete Account → complete phone verification → deletion is immediate.\n\nTo delete your chat profile: Open Community Chat → tap your profile → Delete Profile → confirmed immediately.\n\nTo delete a message: Tap and hold the message → Delete → removed immediately.\n\nAlternatively, email andile@tecvo.co.za with the subject \"Account Deletion Request\" and your registered phone number. Processed within 7 business days.")
                }

                // 07 — Security
                PPSectionBlock("07", "Security") {
                    PPBodyText("All data transmitted by the app uses HTTPS/TLS encryption. Firebase security rules prevent any unauthorised access to the database or storage. Because location data is automatically deleted when the map service ends, the risk profile of a data breach is materially lower than apps that retain permanent location history.")
                }

                // 08 — Children
                PPSectionBlock("08", "Children") {
                    PPBodyText("This app is designed for adults using South African taxi services. It is not intended for users under 18. We do not knowingly process personal information from minors. If we become aware that a minor has registered, that account will be deleted immediately.")
                }

                // 09 — Changes to This Policy
                PPSectionBlock("09", "Changes to This Policy") {
                    PPBodyText("We may update this Privacy Policy to reflect changes in our practices or applicable law. Material changes will be communicated via in-app notification and by updating the date at the top of this document. Continued use of the app after changes constitutes acceptance of the updated policy.")
                }

                // 10 — Contact
                PPSectionBlock("10", "Contact") {
                    Text("TECVO (Pty) Ltd", fontSize = 14.sp, fontWeight = FontWeight.Normal, color = PPCharcoal)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Reg. No. 2024/239147/07  ·  Newcastle, KwaZulu-Natal",
                        fontSize = 13.sp, fontWeight = FontWeight.Light, color = PPSlate)
                    Spacer(modifier = Modifier.height(14.dp))
                    PPBodyText("For all privacy-related enquiries, account deletion requests, and POPIA rights:")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("andile@tecvo.co.za  ·  Response within 7 business days.",
                        fontSize = 13.sp, fontWeight = FontWeight.Light,
                        lineHeight = 20.sp, color = PPCharcoal)
                }

                Spacer(modifier = Modifier.height(8.dp))
            } // end sections Column

            // ── Document footer ───────────────────────────────────────────
            HorizontalDivider(color = PPCloud, thickness = 1.dp)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Taxi — by TECVO (Pty) Ltd", fontSize = 10.sp,
                    fontWeight = FontWeight.Light, letterSpacing = 0.5.sp, color = PPSlate)
                Text("v1.1  ·  April 2026", fontSize = 10.sp,
                    fontWeight = FontWeight.Light, letterSpacing = 0.5.sp,
                    color = PPSlate, textAlign = TextAlign.End)
            }

        } // end main Column
    } // end Scaffold content
} // end PrivacyPolicyScreen
