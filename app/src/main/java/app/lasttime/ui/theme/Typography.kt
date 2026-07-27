package app.lasttime.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val AppFontFamily = FontFamily.SansSerif

internal val LastTimeTypography =
    Typography(
        displayLarge =
            textStyle(
                weight = FontWeight.Normal,
                size = 57,
                lineHeight = 64,
                letterSpacing = -0.25f,
            ),
        displayMedium = textStyle(FontWeight.Normal, 45, 52),
        displaySmall = textStyle(FontWeight.Normal, 36, 44),
        headlineLarge = textStyle(FontWeight.Normal, 32, 40),
        headlineMedium = textStyle(FontWeight.SemiBold, 28, 36),
        headlineSmall = textStyle(FontWeight.SemiBold, 24, 32),
        titleLarge = textStyle(FontWeight.SemiBold, 22, 28),
        titleMedium =
            textStyle(
                weight = FontWeight.SemiBold,
                size = 16,
                lineHeight = 24,
                letterSpacing = 0.15f,
            ),
        titleSmall =
            textStyle(
                weight = FontWeight.Medium,
                size = 14,
                lineHeight = 20,
                letterSpacing = 0.1f,
            ),
        bodyLarge =
            textStyle(
                weight = FontWeight.Normal,
                size = 16,
                lineHeight = 24,
                letterSpacing = 0.5f,
            ),
        bodyMedium =
            textStyle(
                weight = FontWeight.Normal,
                size = 14,
                lineHeight = 20,
                letterSpacing = 0.25f,
            ),
        bodySmall =
            textStyle(
                weight = FontWeight.Normal,
                size = 12,
                lineHeight = 16,
                letterSpacing = 0.4f,
            ),
        labelLarge =
            textStyle(
                weight = FontWeight.Medium,
                size = 14,
                lineHeight = 20,
                letterSpacing = 0.1f,
            ),
        labelMedium =
            textStyle(
                weight = FontWeight.Medium,
                size = 12,
                lineHeight = 16,
                letterSpacing = 0.5f,
            ),
        labelSmall =
            textStyle(
                weight = FontWeight.Medium,
                size = 11,
                lineHeight = 16,
                letterSpacing = 0.5f,
            ),
    )

private fun textStyle(
    weight: FontWeight,
    size: Int,
    lineHeight: Int,
    letterSpacing: Float = 0f,
) = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
)
