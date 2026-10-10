package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Extended color system for dark/light mode support
data class ExtendedColors(
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val glassSurface: Color,
    val glassBorder: Color,
    val background: Color,
    val surface: Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        textPrimary = Color.Unspecified,
        textSecondary = Color.Unspecified,
        textMuted = Color.Unspecified,
        glassSurface = Color.Unspecified,
        glassBorder = Color.Unspecified,
        background = Color.Unspecified,
        surface = Color.Unspecified
    )
}

// Dark theme extended colors
val DarkExtendedColors = ExtendedColors(
    textPrimary = TextPrimary,
    textSecondary = TextSecondary,
    textMuted = TextMuted,
    glassSurface = GlassSurface,
    glassBorder = GlassBorder,
    background = ObsidianDeep,
    surface = ObsidianSurface
)

// Light theme extended colors
val LightExtendedColors = ExtendedColors(
    textPrimary = Color(0xFF000000),
    textSecondary = Color(0xFF1E293B),
    textMuted = Color(0xFF475569),
    glassSurface = Color(0xCCFFFFFF),
    glassBorder = Color(0x4094A3B8),
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFE2E8F0)
)

// AppTheme object for accessing extended colors
object AppTheme {
    val colors: ExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalExtendedColors.current
}

// Material color schemes
private val MusicsDarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = Color(0xFF003848),
    onPrimaryContainer = Color(0xFFBAEDFD),
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = Color(0xFF0B3A64),
    onSecondaryContainer = Color(0xFFCCE4FF),
    tertiary = TertiaryDark,
    onTertiary = Color(0xFFFFFFFF),
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    outline = GlassBorder
)

private val MusicsLightColorScheme = lightColorScheme(
    primary = PrimaryDark,
    onPrimary = Color.White,
    secondary = SecondaryDark,
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF1F5F9),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun MyApplicationTheme(
    isDarkMode: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val extendedColors = if (isDarkMode) DarkExtendedColors else LightExtendedColors
    val colorScheme = if (isDarkMode) MusicsDarkColorScheme else MusicsLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = android.graphics.Color.TRANSPARENT
                window.navigationBarColor = android.graphics.Color.TRANSPARENT
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !isDarkMode
                insetsController.isAppearanceLightNavigationBars = !isDarkMode
            }
        }
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}