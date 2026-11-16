package com.timeguess.ui.theme

import android.app.Activity
import android.graphics.Color as AndroidColor
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
    primary = Color.White,
    onPrimary = Color.Black
)

private val LightColorScheme = lightColorScheme(
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    primary = Color.Black,
    onPrimary = Color.White
)

@Composable
fun TimeGuessTheme(
    darkMode: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkMode) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val bar = if (darkMode) AndroidColor.BLACK else AndroidColor.WHITE
            @Suppress("DEPRECATION")
            window.statusBarColor = bar
            @Suppress("DEPRECATION")
            window.navigationBarColor = bar
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkMode
            controller.isAppearanceLightNavigationBars = !darkMode
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
