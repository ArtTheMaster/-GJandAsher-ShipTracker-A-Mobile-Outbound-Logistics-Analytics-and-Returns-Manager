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
    primary = Color(0xFF38BDF8),
    onPrimary = Color(0xFF082F49),
    primaryContainer = LogisticsBlueContainerDark,
    onPrimaryContainer = LogisticsBlueLight,
    secondary = AccentAmber,
    onSecondary = Slate900,
    secondaryContainer = AccentAmberContainerDark,
    onSecondaryContainer = AccentAmberLight,
    tertiary = StatusGreen,
    onTertiary = Color.White,
    background = Slate900,
    surface = Slate800,
    surfaceVariant = Slate700,
    onBackground = Slate50,
    onSurface = Slate50,
    onSurfaceVariant = Slate300,
    outline = Slate500,
    outlineVariant = Slate700
)

private val LightColorScheme = lightColorScheme(
    primary = LogisticsBlue,
    onPrimary = Color.White,
    primaryContainer = LogisticsBlueLight,
    onPrimaryContainer = LogisticsBlueDark,
    secondary = AccentAmber,
    onSecondary = Color.White,
    secondaryContainer = AccentAmberLight,
    onSecondaryContainer = AccentAmberDark,
    tertiary = StatusGreen,
    onTertiary = Color.White,
    background = Slate50,
    surface = Color.White,
    surfaceVariant = Slate100,
    onBackground = Slate900,
    onSurface = Slate900,
    onSurfaceVariant = Slate600,
    outline = Slate300,
    outlineVariant = Slate200
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
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
