package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val HindSiliguriFamily = FontFamily(
    Font(R.font.hind_siliguri, FontWeight.Normal),
    Font(R.font.hind_siliguri, FontWeight.Medium),
    Font(R.font.hind_siliguri, FontWeight.SemiBold),
    Font(R.font.hind_siliguri, FontWeight.Bold)
)

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = HindSiliguriFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = HindSiliguriFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.25).sp
    ),
    displaySmall = TextStyle(
        fontFamily = HindSiliguriFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = HindSiliguriFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = HindSiliguriFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = HindSiliguriFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = HindSiliguriFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    titleSmall = TextStyle(
        fontFamily = HindSiliguriFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = HindSiliguriFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = HindSiliguriFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    bodySmall = TextStyle(
        fontFamily = HindSiliguriFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = HindSiliguriFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = HindSiliguriFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = HindSiliguriFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp
    )
)
