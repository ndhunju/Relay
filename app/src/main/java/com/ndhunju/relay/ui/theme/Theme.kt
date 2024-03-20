package com.ndhunju.relay.ui.theme

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
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun RelayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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

    setStatusBarColor(darkTheme, colorScheme.primary)


    // Provide correct dimens based on smallest width size
    val dimensions = getLocalDimensProvider()
    // Provide correct colors based on current theme
    val colors = getLocalColorProvider(isSystemInDarkTheme())

    CompositionLocalProvider(
        LocalDimens provides dimensions,
        LocalColors provides colors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
private fun getLocalDimensProvider(): Dimensions {
    val configuration = LocalConfiguration.current
    return if (configuration.smallestScreenWidthDp < 600) {
        CompactDimensions
    } else if (configuration.smallestScreenWidthDp < 840) {
        Sw600Dimensions
    } else {
        Sw840Dimensions
    }
}

@Composable
private fun getLocalColorProvider(isDarkTheme: Boolean): Colors {
    return if (isDarkTheme) ColorsForDarkTheme else ColorsForLightTheme
}

@Composable
fun setStatusBarColor(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    color: Color  = MaterialTheme.colorScheme.primary
): Boolean {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = color.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isDarkTheme
        }
    }

    return true
}