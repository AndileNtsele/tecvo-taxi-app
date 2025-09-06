package com.tecvo.taxi.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.tecvo.taxi.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * Ultra-realistic animated header where the passenger figure literally pushes the destination word
 * with their hands like a real human, complete with body language, effort, and physics.
 */
@Composable
fun RealisticAnimatedHeader(
    userType: String,
    destination: String,
    nearbyPrimaryCount: Int,
    nearbySecondaryCount: Int,
    minibusImageSize: Dp,
    passengerImageSize: Dp,
    headerTextSize: TextUnit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    
    // Animation states for the pushing figure
    val figureX = remember { Animatable(0f) }
    val figureBodyLean = remember { Animatable(0f) } // Body leaning forward for push
    val figureArmExtension = remember { Animatable(0f) } // Arms extending to push
    val figureEffortShake = remember { Animatable(0f) } // Realistic effort/strain
    val figureStepPosition = remember { Animatable(0f) } // Foot positioning for leverage
    val figureAlpha = remember { Animatable(0f) }
    
    // Text animation states
    val textX = remember { Animatable(0f) }
    val textResistance = remember { Animatable(0f) } // Text resistance to being pushed
    val textScale = remember { Animatable(1f) }
    val textAlpha = remember { Animatable(0f) }
    
    // Driver animation states (for taxi pulling)
    val driverRotation = remember { Animatable(0f) }
    val driverPullForce = remember { Animatable(0f) }
    
    LaunchedEffect(userType, destination) {
        // Reset all animations
        figureX.snapTo(-200f)
        figureBodyLean.snapTo(0f)
        figureArmExtension.snapTo(0f)
        figureEffortShake.snapTo(0f)
        figureStepPosition.snapTo(0f)
        figureAlpha.snapTo(0f)
        textX.snapTo(if (userType == "passenger") -50f else -100f)
        textResistance.snapTo(0f)
        textScale.snapTo(0.9f)
        textAlpha.snapTo(0f)
        driverRotation.snapTo(-10f)
        driverPullForce.snapTo(0f)
        
        delay(400L) // Screen settle time
        
        if (userType == "passenger") {
            // REALISTIC PASSENGER PUSHING ANIMATION
            
            // Phase 1: Figure approaches the text
            this.launch {
                figureAlpha.animateTo(1f, animationSpec = tween(300))
            }
            this.launch {
                textAlpha.animateTo(1f, animationSpec = tween(400, delayMillis = 100))
            }
            this.launch {
                figureX.animateTo(-120f, animationSpec = tween(800, easing = FastOutSlowInEasing))
            }
            
            delay(600L)
            
            // Phase 2: Figure positions themselves and prepares to push
            this.launch {
                figureStepPosition.animateTo(1f, animationSpec = tween(400))
            }
            this.launch {
                figureBodyLean.animateTo(0.3f, animationSpec = tween(600))
            }
            
            delay(300L)
            
            // Phase 3: The actual PUSH with realistic effort
            this.launch {
                figureArmExtension.animateTo(1f, animationSpec = tween(300))
            }
            this.launch {
                figureBodyLean.animateTo(0.8f, animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ))
            }
            
            delay(200L)
            
            // Phase 4: Text resists then gives way (realistic physics)
            this.launch {
                textResistance.animateTo(1f, animationSpec = tween(150))
                textResistance.animateTo(0f, animationSpec = tween(100))
            }
            this.launch {
                // Effort shake while pushing
                repeat(3) {
                    figureEffortShake.animateTo(1f, animationSpec = tween(80))
                    figureEffortShake.animateTo(-1f, animationSpec = tween(80))
                }
                figureEffortShake.animateTo(0f, animationSpec = tween(100))
            }
            this.launch {
                delay(100L) // Text resistance delay
                textX.animateTo(120f, animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                ))
            }
            
            // Phase 5: Success! Figure relaxes, text settles in center
            delay(400L)
            this.launch {
                figureBodyLean.animateTo(0.2f, animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ))
            }
            this.launch {
                figureArmExtension.animateTo(0.5f, animationSpec = tween(500))
            }
            this.launch {
                textX.animateTo(0f, animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
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
            // DRIVER PULLING ANIMATION (taxi dragging text)
            this.launch {
                figureAlpha.animateTo(1f, animationSpec = tween(300))
                textAlpha.animateTo(0.8f, animationSpec = tween(350))
            }
            this.launch {
                driverPullForce.animateTo(1f, animationSpec = tween(500))
            }
            
            delay(200L)
            
            // Coordinated pulling - both move together
            this.launch {
                figureX.animateTo(80f, animationSpec = tween(900, easing = FastOutSlowInEasing))
            }
            this.launch {
                textX.animateTo(40f, animationSpec = tween(950, delayMillis = 100, easing = FastOutSlowInEasing))
            }
            this.launch {
                textAlpha.animateTo(1f, animationSpec = tween(400, delayMillis = 200))
            }
            
            delay(500L)
            
            // Driver "releases" and drives back to position
            this.launch {
                driverRotation.animateTo(0f, animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ))
            }
            this.launch {
                figureX.animateTo(0f, animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ))
            }
            this.launch {
                textX.animateTo(0f, animationSpec = spring(
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
    
    // Header layout with realistic figure animation
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: Animated pushing/pulling figure
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            if (userType == "passenger") {
                // Custom animated pushing figure
                Canvas(
                    modifier = Modifier
                        .size(if (userType == "driver") minibusImageSize else passengerImageSize)
                        .offset(x = with(density) { figureX.value.toDp() })
                        .alpha(figureAlpha.value)
                ) {
                    drawRealisticPushingFigure(
                        bodyLean = figureBodyLean.value,
                        armExtension = figureArmExtension.value,
                        effortShake = figureEffortShake.value,
                        stepPosition = figureStepPosition.value
                    )
                }
            } else {
                // Driver with pulling animation
                Image(
                    painter = painterResource(id = R.drawable.minibus1),
                    contentDescription = "Driver Taxi",
                    modifier = Modifier
                        .size(minibusImageSize)
                        .offset(x = with(density) { figureX.value.toDp() })
                        .rotate(driverRotation.value)
                        .scale(1f + driverPullForce.value * 0.1f)
                        .alpha(figureAlpha.value)
                )
            }
            
            if (nearbyPrimaryCount > 0) {
                CountBadge(
                    count = nearbyPrimaryCount, 
                    backgroundColor = if (userType == "driver") Color(0xFF2196F3) else Color(0xFF4CAF50)
                )
            }
        }
        
        // Center: Text with realistic physics response
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
                    .offset(
                        x = with(density) { textX.value.toDp() },
                        y = with(density) { (textResistance.value * 3f).toDp() } // Resistance wobble
                    )
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

/**
 * Draws a realistic stick figure in pushing pose with human-like body language
 */
private fun DrawScope.drawRealisticPushingFigure(
    bodyLean: Float,
    armExtension: Float,
    effortShake: Float,
    stepPosition: Float
) {
    val figureColor = Color(0xFFFB0404) // Same red as original
    val strokeWidth = 3.dp.toPx()
    val centerX = size.width / 2
    val centerY = size.height / 2
    
    // Calculate realistic body positions with physics
    val leanOffset = bodyLean * 15f + effortShake * 2f
    val armReach = armExtension * 20f
    val footSpacing = stepPosition * 8f
    
    // Head (with slight movement from effort)
    drawCircle(
        color = figureColor,
        radius = 6.dp.toPx(),
        center = Offset(centerX - leanOffset * 0.3f, centerY - 25f + effortShake),
        style = Stroke(width = strokeWidth)
    )
    
    // Body (leaning forward based on push effort)
    val bodyTop = Offset(centerX - leanOffset * 0.5f, centerY - 15f)
    val bodyBottom = Offset(centerX - leanOffset, centerY + 5f)
    drawLine(
        color = figureColor,
        start = bodyTop,
        end = bodyBottom,
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    
    // Arms (extending forward to push)
    val shoulderRight = Offset(centerX - leanOffset * 0.3f, centerY - 8f)
    val handRight = Offset(centerX + armReach - leanOffset * 0.2f, centerY - 5f + effortShake * 0.5f)
    
    val shoulderLeft = Offset(centerX - leanOffset * 0.3f, centerY - 12f)
    val handLeft = Offset(centerX + armReach - leanOffset * 0.2f, centerY - 8f - effortShake * 0.5f)
    
    // Right arm
    drawLine(color = figureColor, start = shoulderRight, end = handRight, strokeWidth = strokeWidth, cap = StrokeCap.Round)
    // Left arm  
    drawLine(color = figureColor, start = shoulderLeft, end = handLeft, strokeWidth = strokeWidth, cap = StrokeCap.Round)
    
    // Legs (positioned for leverage and pushing stance)
    val hipLeft = Offset(centerX - leanOffset * 0.8f, centerY + 5f)
    val hipRight = Offset(centerX - leanOffset * 1.2f, centerY + 5f)
    
    val footLeft = Offset(centerX - leanOffset * 0.5f, centerY + 25f - footSpacing)
    val footRight = Offset(centerX - leanOffset * 1.5f - footSpacing, centerY + 25f)
    
    // Left leg
    drawLine(color = figureColor, start = hipLeft, end = footLeft, strokeWidth = strokeWidth, cap = StrokeCap.Round)
    // Right leg (back leg for leverage)
    drawLine(color = figureColor, start = hipRight, end = footRight, strokeWidth = strokeWidth, cap = StrokeCap.Round)
    
    // Push hands (visual indication of hands on surface)
    if (armExtension > 0.5f) {
        drawCircle(
            color = figureColor,
            radius = 2.dp.toPx(),
            center = handRight,
            style = Stroke(width = strokeWidth / 2)
        )
        drawCircle(
            color = figureColor,
            radius = 2.dp.toPx(), 
            center = handLeft,
            style = Stroke(width = strokeWidth / 2)
        )
    }
}