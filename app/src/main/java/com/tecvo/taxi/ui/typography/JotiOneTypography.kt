package com.tecvo.taxi.ui.typography

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.fontResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.tecvo.taxi.R

// Joti One font family
val JotiOneFont = FontFamily(
    Font(R.font.joti_one_regular, FontWeight.Normal)
)

@Composable
fun JotiOneText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontFamily = JotiOneFont,
        fontWeight = fontWeight,
        textAlign = textAlign,
        maxLines = maxLines,
        style = TextStyle(
            fontFamily = JotiOneFont
        )
    )
}

// Predefined text styles using Joti One
object JotiOneTextStyles {
    val Title = TextStyle(
        fontFamily = JotiOneFont,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp
    )

    val Header = TextStyle(
        fontFamily = JotiOneFont,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp
    )

    val Body = TextStyle(
        fontFamily = JotiOneFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )

    val Button = TextStyle(
        fontFamily = JotiOneFont,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp
    )
}