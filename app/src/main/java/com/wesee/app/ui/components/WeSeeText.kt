package com.wesee.app.ui.components

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.wesee.app.ui.theme.HostGrotesk

@Composable
fun WeSeeText(
    text: String, color: Color, size: Int, weight: FontWeight = FontWeight.Normal,
    modifier: Modifier = Modifier, lineHeight: Int? = null, textAlign: TextAlign = TextAlign.Center,
    fontFamily: FontFamily = HostGrotesk, letterSpacing: TextUnit = TextUnit.Unspecified,
) = BasicText(
    text, modifier,
    style = TextStyle(
        color = color, fontSize = size.sp, fontWeight = weight, fontFamily = fontFamily,
        textAlign = textAlign, lineHeight = (lineHeight ?: (size * 1.25f).toInt()).sp,
        letterSpacing = letterSpacing,
    ),
)
