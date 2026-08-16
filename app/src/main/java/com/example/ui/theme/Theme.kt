package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SaffronPrimaryDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3B1D0E),
    onPrimaryContainer = SaffronLight,
    secondary = TrustTealDarkPrimary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF0F3935),
    onSecondaryContainer = TrustTealContainer,
    tertiary = AlertAmber,
    onTertiary = Color.White,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceSubtle,
    onSurfaceVariant = DarkTextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = SaffronPrimary,
    onPrimary = Color.White,
    primaryContainer = SaffronLight,
    onPrimaryContainer = SaffronDark,
    secondary = TrustTealPrimary,
    onSecondary = Color.White,
    secondaryContainer = TrustTealContainer,
    onSecondaryContainer = TrustTealDark,
    tertiary = AlertAmber,
    onTertiary = Color.White,
    background = BackgroundWarmLight,
    surface = CardBackgroundLight,
    onBackground = NeighborhoodNavy,
    onSurface = NeighborhoodNavy,
    surfaceVariant = SurfaceSubtle,
    onSurfaceVariant = NeighborhoodSlate
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep Door Dost custom brand vibrancy
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
