package com.tecvo.taxi.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A standardized count badge component used to display the number of nearby drivers or passengers.
 *
 * @param count The number to be displayed in the badge
 * @param backgroundColor The background color of the badge
 * @param badgeSize The size of the badge (width and height)
 * @param textSize The font size of the count text
 */
@Composable
fun CountBadge(
    count: Int,
    backgroundColor: Color,
    badgeSize: Dp = 24.dp,
    textSize: TextUnit = 12.sp
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(badgeSize)
            .clip(CircleShape)
            .background(backgroundColor)
    ) {
        Text(
            text = count.toString(),
            color = Color.White,
            fontSize = textSize,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}