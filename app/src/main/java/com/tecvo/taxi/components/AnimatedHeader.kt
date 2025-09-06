package com.tecvo.taxi.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.tecvo.taxi.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Animated header component that creates professional "push" (passenger) or "pull/drag" (driver) 
 * animations to reinforce the user journey narrative.
 * 
 * Animation Mechanics:
 * - Passenger: Icon "pushes" destination text to center position
 * - Driver: Icon "drags/pulls" destination text to center position
 */
@Composable
fun AnimatedHeader(
    userType: String,
    destination: String,
    nearbyPrimaryCount: Int,
    nearbySecondaryCount: Int,
    minibusImageSize: Dp,
    passengerImageSize: Dp,
    headerTextSize: TextUnit,
    modifier: Modifier = Modifier
) {
    // Animation state variables
    val density = LocalDensity.current
    
    // Convert dp to pixels for calculations - make less extreme for passenger visibility
    val screenStartOffset = with(density) { -80.dp.toPx() }
    val centerPosition = 0f
    
    // Icon animation states
    val iconOffsetX = remember { Animatable(screenStartOffset) }
    val iconScale = remember { Animatable(0.8f) }
    val iconRotation = remember { Animatable(if (userType == "driver") -15f else 0f) }
    val iconAlpha = remember { Animatable(if (userType == "passenger") 0.3f else 0f) } // Start passenger slightly visible
    
    // Text animation states  
    val textOffsetX = remember { Animatable(if (userType == "driver") screenStartOffset * 0.3f else -50f) }
    val textScale = remember { Animatable(0.9f) }
    val textAlpha = remember { Animatable(0f) }
    
    // Animation trigger
    LaunchedEffect(userType, destination) {
        // Reset all animations
        iconOffsetX.snapTo(screenStartOffset)
        iconScale.snapTo(0.8f)
        iconRotation.snapTo(if (userType == "driver") -15f else 0f)
        iconAlpha.snapTo(if (userType == "passenger") 0.3f else 0f) // Start passenger slightly visible
        textOffsetX.snapTo(if (userType == "driver") screenStartOffset * 0.3f else -50f)
        textScale.snapTo(0.9f)
        textAlpha.snapTo(0f)
        
        // Wait a moment for screen to stabilize
        delay(300L)
        
        if (userType == "passenger") {
            // PASSENGER "PUSH" ANIMATION
            
            // Phase 1: Icon slides in and becomes visible
            this.launch {
                iconAlpha.animateTo(1f, animationSpec = tween(200, easing = FastOutSlowInEasing))
            }
            this.launch {
                iconScale.animateTo(1f, animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ))
            }
            this.launch {
                iconOffsetX.animateTo(-30f, animationSpec = tween(
                    600, 
                    easing = FastOutSlowInEasing
                ))
            }
            
            // Phase 2: Text appears and gets "pushed" (delayed start for interaction effect)
            delay(150L)
            this.launch {
                textAlpha.animateTo(1f, animationSpec = tween(300, easing = LinearOutSlowInEasing))
            }
            this.launch {
                textOffsetX.animateTo(20f, animationSpec = tween(
                    500,
                    easing = FastOutSlowInEasing
                ))
            }
            
            // Phase 3: Icon "pushes" and both settle into final positions
            delay(200L)
            this.launch {
                iconOffsetX.animateTo(centerPosition, animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ))
            }
            this.launch {
                textOffsetX.animateTo(centerPosition, animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                ))
            }
            this.launch {
                textScale.animateTo(1f, animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ))
            }
            
        } else {
            // DRIVER "PULL/DRAG" ANIMATION
            
            // Phase 1: Icon and text appear together (taxi drags text with it)
            this.launch {
                iconAlpha.animateTo(1f, animationSpec = tween(200, easing = FastOutSlowInEasing))
                textAlpha.animateTo(0.7f, animationSpec = tween(250, easing = LinearOutSlowInEasing))
            }
            this.launch {
                iconScale.animateTo(1.1f, animationSpec = tween(
                    400, 
                    easing = FastOutSlowInEasing
                ))
            }
            
            // Phase 2: Coordinated "dragging" movement - both move together
            delay(100L)
            this.launch {
                iconOffsetX.animateTo(30f, animationSpec = tween(
                    700,
                    easing = FastOutSlowInEasing
                ))
            }
            this.launch {
                // Text follows icon with slight delay for dragging effect
                textOffsetX.animateTo(15f, animationSpec = tween(
                    750,
                    50, // 50ms delay for drag effect
                    easing = FastOutSlowInEasing
                ))
            }
            this.launch {
                textAlpha.animateTo(1f, animationSpec = tween(400, easing = LinearOutSlowInEasing))
            }
            
            // Phase 3: Icon "drives back" with car-like deceleration, text settles
            delay(300L)
            this.launch {
                iconOffsetX.animateTo(centerPosition, animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ))
            }
            this.launch {
                iconRotation.animateTo(0f, animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ))
            }
            this.launch {
                iconScale.animateTo(1f, animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                ))
            }
            
            // Text "releases" and settles into center
            delay(50L)
            this.launch {
                textOffsetX.animateTo(centerPosition, animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                ))
            }
            this.launch {
                textScale.animateTo(1f, animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ))
            }
        }
    }
    
    // Header layout
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: User's role icon with animation
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Image(
                painter = painterResource(
                    id = if (userType == "driver") R.drawable.minibus1 else R.drawable.passenger1
                ),
                contentDescription = "${userType.replaceFirstChar { it.uppercase() }} Icon",
                modifier = Modifier
                    .size(if (userType == "driver") minibusImageSize else passengerImageSize)
                    .offset(x = with(density) { iconOffsetX.value.toDp() })
                    .scale(iconScale.value)
                    .rotate(iconRotation.value)
                    .alpha(iconAlpha.value)
            )
            if (nearbyPrimaryCount > 0) {
                CountBadge(
                    count = nearbyPrimaryCount, 
                    backgroundColor = if (userType == "driver") Color(0xFF2196F3) else Color(0xFF4CAF50)
                )
            }
        }
        
        // Center: Animated destination text
        Box(
            contentAlignment = Alignment.Center, 
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = destination.replaceFirstChar { it.uppercase() },
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = headerTextSize, 
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .offset(x = with(density) { textOffsetX.value.toDp() })
                    .scale(textScale.value)
                    .alpha(textAlpha.value)
            )
        }
        
        // Right side: Other role icon (static)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 8.dp)
        ) {
            if (nearbySecondaryCount > 0) {
                CountBadge(
                    count = nearbySecondaryCount, 
                    backgroundColor = if (userType == "driver") Color(0xFF4CAF50) else Color(0xFF2196F3)
                )
            }
            Image(
                painter = painterResource(
                    id = if (userType == "driver") R.drawable.passenger1 else R.drawable.minibus1
                ),
                contentDescription = "${if (userType == "driver") "Passenger" else "Driver"} Icon",
                modifier = Modifier.size(
                    if (userType == "driver") passengerImageSize else minibusImageSize
                )
            )
        }
    }
}