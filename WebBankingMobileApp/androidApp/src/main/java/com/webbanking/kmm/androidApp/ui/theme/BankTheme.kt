package com.webbanking.kmm.androidApp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

/**
 * Entry point for the app design system.
 */
@Composable
fun BankTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        // For now we mirror the light colours; swap with proper dark palette later
        lightColorScheme(
            primary = ColorTokens.Primary,
            onPrimary = ColorTokens.OnPrimary,
            primaryContainer = ColorTokens.PrimaryContainer,
            onPrimaryContainer = ColorTokens.OnPrimaryContainer,
            secondary = ColorTokens.Secondary,
            onSecondary = ColorTokens.OnSecondary,
            secondaryContainer = ColorTokens.SecondaryContainer,
            onSecondaryContainer = ColorTokens.OnSecondaryContainer,
            tertiary = ColorTokens.Tertiary,
            onTertiary = ColorTokens.OnTertiary,
            tertiaryContainer = ColorTokens.TertiaryContainer,
            onTertiaryContainer = ColorTokens.OnTertiaryContainer,
            error = ColorTokens.Error,
            onError = ColorTokens.OnError,
            errorContainer = ColorTokens.ErrorContainer,
            onErrorContainer = ColorTokens.OnErrorContainer,
            background = ColorTokens.Background,
            onBackground = ColorTokens.OnBackground,
            surface = ColorTokens.Surface,
            onSurface = ColorTokens.OnSurface,
            surfaceVariant = ColorTokens.SurfaceVariant,
            onSurfaceVariant = ColorTokens.OnSurfaceVariant,
        )
    } else {
        lightColorScheme(
            primary = ColorTokens.Primary,
            onPrimary = ColorTokens.OnPrimary,
            primaryContainer = ColorTokens.PrimaryContainer,
            onPrimaryContainer = ColorTokens.OnPrimaryContainer,
            secondary = ColorTokens.Secondary,
            onSecondary = ColorTokens.OnSecondary,
            secondaryContainer = ColorTokens.SecondaryContainer,
            onSecondaryContainer = ColorTokens.OnSecondaryContainer,
            tertiary = ColorTokens.Tertiary,
            onTertiary = ColorTokens.OnTertiary,
            tertiaryContainer = ColorTokens.TertiaryContainer,
            onTertiaryContainer = ColorTokens.OnTertiaryContainer,
            error = ColorTokens.Error,
            onError = ColorTokens.OnError,
            errorContainer = ColorTokens.ErrorContainer,
            onErrorContainer = ColorTokens.OnErrorContainer,
            background = ColorTokens.Background,
            onBackground = ColorTokens.OnBackground,
            surface = ColorTokens.Surface,
            onSurface = ColorTokens.OnSurface,
            surfaceVariant = ColorTokens.SurfaceVariant,
            onSurfaceVariant = ColorTokens.OnSurfaceVariant,
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TypographyTokens.AppTypography,
        content = content
    )
} 