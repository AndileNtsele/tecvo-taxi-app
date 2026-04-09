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

// ── Brand colour tokens (matching tecvo-brand-style-guide.html) ──────────────
private val BrandBlack    = Color(0xFF000000)
private val BrandCharcoal = Color(0xFF1D1D1F)
private val BrandSlate    = Color(0xFF86868B)
private val BrandCloud    = Color(0xFFE0E0E5)
private val BrandMist     = Color(0xFFF4F4F6)
private val BrandWhite    = Color(0xFFFFFFFF)

// ── Reusable section composables ─────────────────────────────────────────────

@Composable
private fun SectionBlock(
    number: String,
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = number,
            fontSize = 10.sp, fontWeight = FontWeight.Normal,
            letterSpacing = 3.sp, color = BrandSlate,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = title,
            fontSize = 19.sp, fontWeight = FontWeight.Light,
            letterSpacing = 0.5.sp, color = BrandBlack
        )
        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = BrandCloud, thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))
        content()
    }
}

@Composable
private fun BodyText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontSize = 14.sp, fontWeight = FontWeight.Light,
        lineHeight = 22.sp, color = BrandCharcoal,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun BulletItem(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier.padding(top = 8.dp).size(4.dp)
                .background(BrandSlate, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = text,
            fontSize = 13.sp, fontWeight = FontWeight.Light,
            lineHeight = 20.sp, color = BrandCharcoal
        )
    }
    HorizontalDivider(color = BrandCloud, thickness = 0.5.dp)
}

@Composable
private fun SubsectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 10.sp, fontWeight = FontWeight.Normal,
        letterSpacing = 2.sp, color = BrandCharcoal,
        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
    )
}

@Composable
private fun NoteBox(text: String) {
    Box(
        modifier = Modifier.fillMaxWidth().background(BrandMist).padding(14.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp, fontWeight = FontWeight.Light,
            lineHeight = 19.sp, color = BrandSlate
        )
    }
}

// ── Main Screen ───────────────────────────────────────────────────────────────

@Composable
fun TermsAndConditionsScreen(navController: NavController) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "A PRODUCT BY TECVO (PTY) LTD",
                            fontSize = 9.sp, fontWeight = FontWeight.Normal,
                            letterSpacing = 2.5.sp, color = BrandSlate
                        )
                        Text(
                            text = "Taxi",
                            fontSize = 22.sp, fontWeight = FontWeight.Light,
                            letterSpacing = 1.sp, color = BrandBlack
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back", tint = BrandBlack
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandWhite)
            )
        },
        containerColor = BrandWhite
    ) { paddingValues ->

        Column(
            modifier = Modifier.fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {

            // ── Document header ───────────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth().background(BrandWhite)
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Text(
                    text = "TERMS AND CONDITIONS",
                    fontSize = 11.sp, fontWeight = FontWeight.Normal,
                    letterSpacing = 2.5.sp, color = BrandSlate
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Last updated: April 2026  ·  Version 1.1",
                    fontSize = 11.sp, fontWeight = FontWeight.Light,
                    letterSpacing = 0.5.sp, color = BrandSlate
                )
            }

            HorizontalDivider(color = BrandCloud, thickness = 1.dp)

            // ── Intro box ─────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Box(modifier = Modifier.width(2.dp).height(96.dp).background(BrandBlack))
                Spacer(modifier = Modifier.width(16.dp))
                Box(modifier = Modifier.fillMaxWidth().background(BrandMist).padding(16.dp)) {
                    Text(
                        text = "These Terms govern your use of the Taxi app, a real-time visibility and community service built by TECVO (Pty) Ltd for the South African taxi industry. By using the app, you agree to these Terms. Please read them — they are written plainly and without unnecessary complexity.",
                        fontSize = 14.sp, fontWeight = FontWeight.Light,
                        lineHeight = 22.sp, color = BrandCharcoal
                    )
                }
            }

            HorizontalDivider(color = BrandCloud, thickness = 1.dp)

            // ── Sections ─────────────────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(40.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // 01 — Agreement
                SectionBlock(number = "01", title = "Agreement") {
                    BodyText("These Terms of Service constitute a legally binding agreement between you and TECVO (Pty) Ltd (\"TECVO\", \"we\", \"us\") governing your access to and use of the Taxi mobile application.")
                    Spacer(modifier = Modifier.height(10.dp))
                    BodyText("By downloading, accessing, or using the app, you confirm that you have read and agreed to these Terms. If you do not agree, you must not use the app.")
                }

                // 02 — What the App Does
                SectionBlock(number = "02", title = "What the App Does") {
                    BodyText("Taxi is built for the South African minibus taxi industry. It combines real-time map visibility with community features designed to connect drivers, passengers, and taxi associations.")
                    SubsectionLabel("The app provides")
                    BulletItem("A live map showing taxis and passengers going the same direction")
                    BulletItem("TOWN and LOCAL direction selection — the standard terminology used at SA taxi ranks")
                    BulletItem("Automatic removal of your location when you leave the map")
                    BulletItem("Community Chat — city-based public chat rooms for drivers, passengers, and commuters")
                    BulletItem("Announcements — official notices posted by verified taxi associations")
                    BulletItem("Taxi Association Registration — a way for associations to create a verified presence in the app")
                    BulletItem("A chat profile — a display name and optional profile picture used in Community Chat")
                    SubsectionLabel("The app does not provide")
                    BulletItem("Ride booking or reservation of any kind")
                    BulletItem("Payment processing between drivers and passengers")
                    BulletItem("Guaranteed taxi availability or arrival times")
                    BulletItem("Dispatch instructions or route management")
                }

                // 03 — Your Responsibilities
                SectionBlock(number = "03", title = "Your Responsibilities") {
                    BulletItem("You must enable location services for the map feature to function.")
                    BulletItem("You must select the correct role (Driver or Passenger) and direction honestly — providing false information degrades the service for everyone.")
                    BulletItem("You must use the app in compliance with all applicable South African laws and regulations.")
                    BulletItem("You must not use the app for any unlawful purpose or in any way that harms other users.")
                    BulletItem("You must not attempt to reverse-engineer, decompile, or interfere with the app or its infrastructure.")
                }

                // 04 — Community Chat — Rules of Conduct
                SectionBlock(number = "04", title = "Community Chat — Rules of Conduct") {
                    BodyText("Community Chat is a public, city-based feature. Messages you send are visible to all app users in the same city. By participating in Community Chat, you agree to the following:")
                    Spacer(modifier = Modifier.height(12.dp))
                    BulletItem("You will not post content that is abusive, threatening, harassing, defamatory, or discriminatory on any grounds including race, gender, religion, or sexual orientation.")
                    BulletItem("You will not impersonate another user, taxi association, TECVO, or any other person or organisation.")
                    BulletItem("You will not post spam, advertisements, or unsolicited commercial messages.")
                    BulletItem("You will not share personal information belonging to others without their consent.")
                    BulletItem("You will not post content that is illegal under South African law.")
                    Spacer(modifier = Modifier.height(12.dp))
                    NoteBox("TECVO does not pre-moderate Community Chat but reserves the right to remove any message and suspend or terminate chat access for any user who violates these rules, without prior notice.")
                }

                // 05 — User-Generated Content
                SectionBlock(number = "05", title = "User-Generated Content") {
                    BodyText("When you post a message in Community Chat or create a chat profile, you are creating user-generated content. You remain responsible for all content you create.")
                    Spacer(modifier = Modifier.height(10.dp))
                    BodyText("By posting content in the app, you grant TECVO a limited, non-exclusive licence to store and display that content to other users within the app for the purpose of delivering the Community Chat service. This licence ends when you delete the relevant content or your account.")
                    Spacer(modifier = Modifier.height(10.dp))
                    BodyText("You have full control over your content at any time:")
                    Spacer(modifier = Modifier.height(4.dp))
                    BulletItem("Delete a message — tap and hold any message you sent and select Delete. The message is removed immediately.")
                    BulletItem("Delete your chat profile — go to your profile in Community Chat and select Delete Profile. Your display name, profile picture, and chat registration are removed immediately. Your messages may remain visible in chat threads unless individually deleted.")
                    BulletItem("Delete your account — removes your phone number, chat profile, and all associated data. Instructions are in Section 07 below.")
                }

                // 06 — Privacy and Data
                SectionBlock(number = "06", title = "Privacy and Data") {
                    BodyText("The Taxi app collects only what is needed to deliver its features. Real-time location data is processed temporarily while the map is active and deleted automatically when you leave. Community Chat stores your display name, profile picture, and messages in Firebase for as long as the content exists.")
                    Spacer(modifier = Modifier.height(10.dp))
                    BodyText("The full details of what we collect, why we collect it, how long we keep it, and your rights under POPIA are set out in our Privacy Policy, which forms part of these Terms.")
                }

                // 07 — Account Termination and Deletion
                SectionBlock(number = "07", title = "Account Termination and Deletion") {
                    BodyText("You may stop using the app at any time. To delete your account and all associated data: open the app → Settings → Delete Account → complete phone verification → deletion is immediate.")
                    Spacer(modifier = Modifier.height(10.dp))
                    BodyText("Alternatively, email andile@tecvo.co.za with the subject line \"Account Deletion Request\" and your registered phone number. Processed within 7 business days.")
                    Spacer(modifier = Modifier.height(10.dp))
                    BodyText("TECVO reserves the right to suspend or terminate your access to the app at any time, without prior notice, if we determine that your conduct violates these Terms, harms other users, or is otherwise contrary to the purpose of the service.")
                }

                // 08 — Intellectual Property
                SectionBlock(number = "08", title = "Intellectual Property") {
                    BodyText("The Taxi app, including its design, code, content, and underlying systems, is the exclusive property of TECVO (Pty) Ltd and is protected under South African and international intellectual property law.")
                    Spacer(modifier = Modifier.height(10.dp))
                    BodyText("You are granted a limited, non-exclusive, non-transferable licence to use the app for its intended purpose. No other rights are granted.")
                }

                // 09 — Limitation of Liability
                SectionBlock(number = "09", title = "Limitation of Liability") {
                    BodyText("The app is provided on an \"as is\" basis. TECVO makes no warranties, expressed or implied, regarding uninterrupted service, data accuracy, or fitness for any particular purpose.")
                    Spacer(modifier = Modifier.height(10.dp))
                    BodyText("TECVO shall not be liable for any direct, indirect, incidental, or consequential damages arising from your use of the app — including but not limited to missed taxis, interactions with other users that occur outside the app, content posted by other users in Community Chat, or any safety incidents arising from physical meetings between users. The app provides visibility and communication tools only; all decisions made on the basis of those tools remain your own responsibility.")
                }

                // 10 — Changes to These Terms
                SectionBlock(number = "10", title = "Changes to These Terms") {
                    BodyText("We may update these Terms from time to time to reflect changes in the service or applicable law. Material changes will be communicated via in-app notification.")
                    Spacer(modifier = Modifier.height(10.dp))
                    BodyText("Continued use of the app after changes are published constitutes acceptance of the updated Terms.")
                }

                // 11 — Governing Law
                SectionBlock(number = "11", title = "Governing Law") {
                    BodyText("These Terms are governed by and construed in accordance with the laws of the Republic of South Africa. Any disputes arising from these Terms or the use of the app are subject to the jurisdiction of the South African courts.")
                }

                // 12 — Contact
                SectionBlock(number = "12", title = "Contact") {
                    Text(
                        text = "TECVO (Pty) Ltd",
                        fontSize = 14.sp, fontWeight = FontWeight.Normal, color = BrandCharcoal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Reg. No. 2024/239147/07  ·  Newcastle, KwaZulu-Natal",
                        fontSize = 13.sp, fontWeight = FontWeight.Light, color = BrandSlate
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    BodyText("For questions about these Terms or the app:")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "andile@tecvo.co.za  ·  Response within 7 business days.",
                        fontSize = 13.sp, fontWeight = FontWeight.Light,
                        lineHeight = 20.sp, color = BrandCharcoal
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            } // end sections Column

            // ── Document footer ───────────────────────────────────────────────
            HorizontalDivider(color = BrandCloud, thickness = 1.dp)
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Taxi — by TECVO (Pty) Ltd",
                    fontSize = 10.sp, fontWeight = FontWeight.Light,
                    letterSpacing = 0.5.sp, color = BrandSlate
                )
                Text(
                    text = "v1.1  ·  April 2026",
                    fontSize = 10.sp, fontWeight = FontWeight.Light,
                    letterSpacing = 0.5.sp, color = BrandSlate,
                    textAlign = TextAlign.End
                )
            }

        } // end main Column
    } // end Scaffold content
} // end TermsAndConditionsScreen
