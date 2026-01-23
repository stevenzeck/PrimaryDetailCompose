package com.example.primarydetailcompose.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * App Theme that adapts to the system theme (Light/Dark) and dynamic colors (Android 12+).
 *
 * @param useDarkTheme Whether to use the dark theme. Defaults to system setting.
 * @param content The content to be displayed within the theme.
 */
@Composable
fun PrimaryDetailTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            when {
                useDarkTheme -> dynamicDarkColorScheme(LocalContext.current)
                else -> dynamicLightColorScheme(LocalContext.current)
            }
        }

        useDarkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    MaterialExpressiveTheme(
        colorScheme = colors,
        typography = AppTypography,
        shapes = AppShapes,
        motionScheme = MotionScheme.expressive(),
        content = content,
    )
}
