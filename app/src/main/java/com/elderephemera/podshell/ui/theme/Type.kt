package com.elderephemera.podshell.ui.theme

import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Typography = Typography(
    subtitle1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp
    ),
    subtitle2 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp
    ),
    body1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp
    ),
)

@Composable
fun Typography.overrideTextSize(): Typography = copy(
    h1 = h1.overrideTextSize(),
    h2 = h2.overrideTextSize(),
    h3 = h3.overrideTextSize(),
    h4 = h4.overrideTextSize(),
    h5 = h5.overrideTextSize(),
    h6 = h6.overrideTextSize(),
    subtitle1 = subtitle1.overrideTextSize(),
    subtitle2 = subtitle2.overrideTextSize(),
    body1 = body1.overrideTextSize(),
    body2 = body2.overrideTextSize(),
    button = button.overrideTextSize(),
    caption = caption.overrideTextSize(),
    overline = overline.overrideTextSize(),
)

@Composable
fun TextStyle.overrideTextSize(): TextStyle = copy(fontSize = fontSize.overrideTextSize())

@Composable
fun TextUnit.overrideTextSize(): TextUnit =
    if (isSp) with(LocalDensity.current) { value.dp.toSp() } else this