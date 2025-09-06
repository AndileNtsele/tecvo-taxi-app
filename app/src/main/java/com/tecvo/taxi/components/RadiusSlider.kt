package com.tecvo.taxi.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun RadiusSlider(
    currentRadius: Float,
    onRadiusChange: (Float) -> Unit,
    minRadius: Float = 0.5f,
    maxRadius: Float = 500.0f,
    stepSize: Float = 0.5f
) {
    var radiusValue by remember { mutableFloatStateOf(currentRadius) }

    // State variables to track if buttons are being pressed
    var isPlusButtonPressed by remember { mutableStateOf(false) }
    var isMinusButtonPressed by remember { mutableStateOf(false) }
    
    // Performance optimization: debounce rapid updates
    var lastUpdateTime by remember { mutableStateOf(0L) }
    val updateThrottleMs = 250L // Throttle updates to every 250ms

    // Update the internal state when the external value changes
    LaunchedEffect(currentRadius) {
        radiusValue = currentRadius
    }

    // Increment coroutine for plus button long press - optimized for performance
    LaunchedEffect(isPlusButtonPressed) {
        if (isPlusButtonPressed) {
            // Initial delay before rapid increments begin
            delay(500)
            while(isPlusButtonPressed && radiusValue < maxRadius) {
                val currentTime = System.currentTimeMillis()
                
                val newValue = (radiusValue + stepSize).coerceAtMost(maxRadius)
                radiusValue = newValue
                
                // Performance optimization: throttle callback updates
                if (currentTime - lastUpdateTime > updateThrottleMs) {
                    onRadiusChange(newValue)
                    lastUpdateTime = currentTime
                }
                
                // Longer delay to reduce CPU usage during long press
                delay(150)
            }
            
            // Always send final value when released
            onRadiusChange(radiusValue)
        }
    }

    // Decrement coroutine for minus button long press - optimized for performance
    LaunchedEffect(isMinusButtonPressed) {
        if (isMinusButtonPressed) {
            // Initial delay before rapid decrements begin
            delay(500)
            while(isMinusButtonPressed && radiusValue > minRadius) {
                val currentTime = System.currentTimeMillis()
                
                val newValue = (radiusValue - stepSize).coerceAtLeast(minRadius)
                radiusValue = newValue
                
                // Performance optimization: throttle callback updates
                if (currentTime - lastUpdateTime > updateThrottleMs) {
                    onRadiusChange(newValue)
                    lastUpdateTime = currentTime
                }
                
                // Longer delay to reduce CPU usage during long press
                delay(150)
            }
            
            // Always send final value when released
            onRadiusChange(radiusValue)
        }
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.85f))
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // "Radius" text at the top
        Text(
            text = "Radius",
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Current radius value display
        Text(
            text = "${String.format(Locale.US, "%.1f", radiusValue)} km",
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Plus button with long-press support
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.9f))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            // Handle single press increment without analytics
                            if (radiusValue < maxRadius) {
                                val newValue = (radiusValue + stepSize).coerceAtMost(maxRadius)
                                radiusValue = newValue
                                onRadiusChange(newValue)
                            }

                            // Start long-press handling
                            isPlusButtonPressed = true

                            // Wait for release
                            tryAwaitRelease()

                            // Reset state on release
                            isPlusButtonPressed = false
                        }
                    )
                }
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Increase radius",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Minus button with long-press support
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.9f))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            // Handle single press decrement immediately
                            if (radiusValue > minRadius) {
                                val newValue = (radiusValue - stepSize).coerceAtLeast(minRadius)
                                radiusValue = newValue
                                onRadiusChange(newValue)
                            }

                            // Start long-press handling
                            isMinusButtonPressed = true

                            // Wait for release
                            tryAwaitRelease()

                            // Reset state on release
                            isMinusButtonPressed = false
                        }
                    )
                }
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Decrease radius",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}