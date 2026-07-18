package com.example.patrick.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = VertTapis,
    secondary = OrAccent,
    tertiary = BleuSelection,
    background = CremeCarteFond,
    surface = CremeCarteFond,
    onPrimary = CremeCarteFond,
    onSecondary = NoirCarte,
    onTertiary = CremeCarteFond,
    onBackground = NoirCarte,
    onSurface = NoirCarte
)

private val DarkColorScheme = darkColorScheme(
    primary = VertTapisClair,
    secondary = OrAccent,
    tertiary = BleuSelection,
    background = NoirCarte,
    surface = NoirCarte,
    onPrimary = CremeCarteFond,
    onSecondary = NoirCarte,
    onTertiary = CremeCarteFond,
    onBackground = CremeCarteFond,
    onSurface = CremeCarteFond
)

@Composable
fun PatrickTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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