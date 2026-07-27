package app.lasttime.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import app.lasttime.domain.ThemeMode

private val LightColors =
    lightColorScheme(
        primary = Color(0xFF256D4A),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFACEBC8),
        onPrimaryContainer = Color(0xFF0B5134),
        secondary = Color(0xFF4E6357),
        surface = Color(0xFFF8FAF7),
        surfaceVariant = Color(0xFFE0E4DF),
    )

private val DarkColors =
    darkColorScheme(
        primary = Color(0xFF91D5AE),
        onPrimary = Color(0xFF003921),
        primaryContainer = Color(0xFF0B5134),
        onPrimaryContainer = Color(0xFFACEBC8),
        secondary = Color(0xFFB5CCBD),
    )

private val ExpressiveShapes =
    Shapes(
        extraSmall = RoundedCornerShape(8.dp),
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(20.dp),
        large = RoundedCornerShape(28.dp),
        extraLarge = RoundedCornerShape(36.dp),
    )

@Composable
fun LastTimeTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val darkTheme =
        when (themeMode) {
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }
    val context = LocalContext.current
    val view = LocalView.current
    val colorScheme =
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme ->
                dynamicDarkColorScheme(context)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
                dynamicLightColorScheme(context)
            darkTheme -> DarkColors
            else -> LightColors
        }
    if (!view.isInEditMode) {
        SideEffect {
            context.findActivity()?.window?.let { window ->
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = LastTimeTypography,
        shapes = ExpressiveShapes,
        content = content,
    )
}

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
