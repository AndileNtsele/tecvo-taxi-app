package com.tecvo.taxi.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A semi-transparent toggle button to show/hide same-type entities on the map.
 *
 * @param userType The current user type ("driver" or "passenger")
 * @param isVisible Whether entities of the same type are currently visible
 * @param onToggle Callback function when the visibility is toggled
 */
@Composable
fun EntityVisibilityToggle(
    userType: String,
    isVisible: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Show tooltip on first use
    val prefsKey = "visibility_toggle_shown"
    val sharedPrefs = context.getSharedPreferences("taxi_app_prefs", 0)
    val wasTooltipShown = sharedPrefs.getBoolean(prefsKey, false)
    var showTooltip by remember { mutableStateOf(!wasTooltipShown) }

    if (showTooltip) {
        // Mark tooltip as shown in preferences
        sharedPrefs.edit {
            putBoolean(prefsKey, true)
        }

        // Auto-hide tooltip after delay
        LaunchedEffect(Unit) {
            delay(4000) // 4 seconds
            showTooltip = false
        }
    }

    Box(
        modifier = Modifier
            .padding(bottom = 16.dp)
            .wrapContentSize(),
        contentAlignment = Alignment.Center
    ) {
        // Tooltip that appears on first use
        AnimatedVisibility(
            visible = showTooltip,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .offset(y = (-60).dp)
                .align(Alignment.TopCenter)
        ) {
            Surface(
                modifier = Modifier
                    .width(200.dp)
                    .padding(8.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.8f),
                shadowElevation = 4.dp
            ) {
                Text(
                    text = "Toggle visibility of other ${userType}s on the map",
                    color = Color.White,
                    modifier = Modifier.padding(8.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Main visibility toggle button
        Surface(
            modifier = Modifier
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(24.dp)
                )
                .clip(RoundedCornerShape(24.dp))
                .clickable {
                    val newState = !isVisible
                    onToggle(newState)

                    // Show a quick toast message when toggled
                    coroutineScope.launch {
                        val message = if (newState)
                            "Showing other ${userType}s"
                        else
                            "Hiding other ${userType}s"
                        android.widget.Toast.makeText(
                            context,
                            message,
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                },
            color = Color.White.copy(alpha = 0.7f),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (isVisible) "Hide other ${userType}s" else "Show other ${userType}s",
                    tint = Color(0xFF333333)
                )
            }
        }
    }
}