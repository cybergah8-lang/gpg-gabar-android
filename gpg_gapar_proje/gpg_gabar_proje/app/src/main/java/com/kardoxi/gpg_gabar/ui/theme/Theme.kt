package com.kardoxi.gpg_gabar.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color.Black,
    primaryContainer = CyanContainer,
    onPrimaryContainer = OnCyan,

    secondary = NeonMagenta,
    onSecondary = Color.White,
    secondaryContainer = MagentaContainer,
    onSecondaryContainer = OnMagenta,

    tertiary = ElectricPurple,
    onTertiary = Color.White,
    tertiaryContainer = PurpleContainer,
    onTertiaryContainer = OnPurple,

    background = DeepSpace900,
    onBackground = OnPurple,
    surface = DeepSpace800,
    onSurface = OnPurple,
    surfaceVariant = DeepSpace700,
    onSurfaceVariant = OnPurple,

    outline = OnPurple.copy(alpha = 0.3f),
    outlineVariant = OnPurple.copy(alpha = 0.15f)
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricPurple,
    onPrimary = Color.White,
    primaryContainer = PurpleContainer,
    onPrimaryContainer = OnPurple,

    secondary = NeonCyan,
    onSecondary = Color.Black,
    secondaryContainer = CyanContainer,
    onSecondaryContainer = OnCyan,

    tertiary = NeonMagenta,
    onTertiary = Color.White,
    tertiaryContainer = MagentaContainer,
    onTertiaryContainer = OnMagenta,

    background = Color(0xFFF7F6FB),
    onBackground = Color(0xFF17151F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF17151F),
    surfaceVariant = Color(0xFFF0ECFF),
    onSurfaceVariant = Color(0xFF3A3550),

)

@Composable
fun AzadiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Use dynamic color by default for a globally adaptive palette
    dynamicColor: Boolean = true,
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

    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalElevations provides Elevations()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
