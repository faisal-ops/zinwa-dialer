package com.zinwa.dialer.ui.theme

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 34.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Light,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 3.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.2.sp
    )
)

internal data class DialerColorTokens(
    val bgPage: Color,
    val bgSurface: Color,
    val bgElevated: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textHint: Color,
)

internal val LocalDialerColors = staticCompositionLocalOf<DialerColorTokens> {
    DialerColorTokens(
        bgPage = Color.Unspecified,
        bgSurface = Color.Unspecified,
        bgElevated = Color.Unspecified,
        textPrimary = Color.Unspecified,
        textSecondary = Color.Unspecified,
        textHint = Color.Unspecified
    )
}

@Composable
fun DialerTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("zinwa_settings", Context.MODE_PRIVATE)
    var themeChoice by remember { mutableIntStateOf(prefs.getInt("choose_theme", 0)) } // 0=system default, 1=light, 2=dark
    val systemDark = isSystemInDarkTheme()

    // Keep theme in sync with Display Options without forcing an app restart.
    DisposableEffect(prefs) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "choose_theme") {
                themeChoice = prefs.getInt("choose_theme", 0)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    val useDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    // Avoid re-creating the ColorScheme on every recomposition; only rebuild when
    // themeChoice/systemDark/useDynamic changes.
    val colorScheme = remember(themeChoice, useDynamic, systemDark) {
        when {
            useDynamic && themeChoice == 1 -> dynamicLightColorScheme(context)
            useDynamic && themeChoice == 2 -> dynamicDarkColorScheme(context)
            useDynamic -> if (systemDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            themeChoice == 1 -> lightColorScheme()
            themeChoice == 2 -> darkColorScheme()
            else -> if (systemDark) darkColorScheme() else lightColorScheme()
        }
    }

    val tokens = remember(colorScheme) {
        DialerColorTokens(
            bgPage = colorScheme.background,
            bgSurface = colorScheme.surfaceContainerLow,
            bgElevated = colorScheme.surfaceContainer,
            textPrimary = colorScheme.onBackground,
            textSecondary = colorScheme.onSurfaceVariant,
            textHint = colorScheme.outlineVariant,
        )
    }

    CompositionLocalProvider(LocalDialerColors provides tokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
