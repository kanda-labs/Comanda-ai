package co.touchlab.dogify.presentation.designSystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import co.touchlab.dogify.theme.LocalMyTypography
import co.touchlab.dogify.theme.backgroundDarkHighContrast
import co.touchlab.dogify.theme.backgroundLightHighContrast
import co.touchlab.dogify.theme.errorContainerDarkHighContrast
import co.touchlab.dogify.theme.errorContainerLightHighContrast
import co.touchlab.dogify.theme.errorDarkHighContrast
import co.touchlab.dogify.theme.errorLightHighContrast
import co.touchlab.dogify.theme.inverseOnSurfaceDarkHighContrast
import co.touchlab.dogify.theme.inverseOnSurfaceLightHighContrast
import co.touchlab.dogify.theme.inversePrimaryDarkHighContrast
import co.touchlab.dogify.theme.inversePrimaryLightHighContrast
import co.touchlab.dogify.theme.inverseSurfaceDarkHighContrast
import co.touchlab.dogify.theme.inverseSurfaceLightHighContrast
import co.touchlab.dogify.theme.onBackgroundDarkHighContrast
import co.touchlab.dogify.theme.onBackgroundLightHighContrast
import co.touchlab.dogify.theme.onErrorContainerDarkHighContrast
import co.touchlab.dogify.theme.onErrorContainerLightHighContrast
import co.touchlab.dogify.theme.onErrorDarkHighContrast
import co.touchlab.dogify.theme.onErrorLightHighContrast
import co.touchlab.dogify.theme.onPrimaryContainerDarkHighContrast
import co.touchlab.dogify.theme.onPrimaryContainerLightHighContrast
import co.touchlab.dogify.theme.onPrimaryDarkHighContrast
import co.touchlab.dogify.theme.onPrimaryLightHighContrast
import co.touchlab.dogify.theme.onSecondaryContainerDarkHighContrast
import co.touchlab.dogify.theme.onSecondaryContainerLightHighContrast
import co.touchlab.dogify.theme.onSecondaryDarkHighContrast
import co.touchlab.dogify.theme.onSecondaryLightHighContrast
import co.touchlab.dogify.theme.onSurfaceDarkHighContrast
import co.touchlab.dogify.theme.onSurfaceLightHighContrast
import co.touchlab.dogify.theme.onSurfaceVariantDarkHighContrast
import co.touchlab.dogify.theme.onSurfaceVariantLightHighContrast
import co.touchlab.dogify.theme.onTertiaryContainerDarkHighContrast
import co.touchlab.dogify.theme.onTertiaryContainerLightHighContrast
import co.touchlab.dogify.theme.onTertiaryDarkHighContrast
import co.touchlab.dogify.theme.onTertiaryLightHighContrast
import co.touchlab.dogify.theme.outlineDarkHighContrast
import co.touchlab.dogify.theme.outlineLightHighContrast
import co.touchlab.dogify.theme.outlineVariantDarkHighContrast
import co.touchlab.dogify.theme.outlineVariantLightHighContrast
import co.touchlab.dogify.theme.primaryContainerDarkHighContrast
import co.touchlab.dogify.theme.primaryContainerLightHighContrast
import co.touchlab.dogify.theme.primaryDarkHighContrast
import co.touchlab.dogify.theme.primaryLightHighContrast
import co.touchlab.dogify.theme.scrimDarkHighContrast
import co.touchlab.dogify.theme.scrimLightHighContrast
import co.touchlab.dogify.theme.secondaryContainerDarkHighContrast
import co.touchlab.dogify.theme.secondaryContainerLightHighContrast
import co.touchlab.dogify.theme.secondaryDarkHighContrast
import co.touchlab.dogify.theme.secondaryLightHighContrast
import co.touchlab.dogify.theme.surfaceBrightDarkHighContrast
import co.touchlab.dogify.theme.surfaceBrightLightHighContrast
import co.touchlab.dogify.theme.surfaceContainerDarkHighContrast
import co.touchlab.dogify.theme.surfaceContainerHighDarkHighContrast
import co.touchlab.dogify.theme.surfaceContainerHighLightHighContrast
import co.touchlab.dogify.theme.surfaceContainerHighestDarkHighContrast
import co.touchlab.dogify.theme.surfaceContainerHighestLightHighContrast
import co.touchlab.dogify.theme.surfaceContainerLightHighContrast
import co.touchlab.dogify.theme.surfaceContainerLowDarkHighContrast
import co.touchlab.dogify.theme.surfaceContainerLowLightHighContrast
import co.touchlab.dogify.theme.surfaceContainerLowestDarkHighContrast
import co.touchlab.dogify.theme.surfaceContainerLowestLightHighContrast
import co.touchlab.dogify.theme.surfaceDarkHighContrast
import co.touchlab.dogify.theme.surfaceDimDarkHighContrast
import co.touchlab.dogify.theme.surfaceDimLightHighContrast
import co.touchlab.dogify.theme.surfaceLightHighContrast
import co.touchlab.dogify.theme.surfaceVariantDarkHighContrast
import co.touchlab.dogify.theme.surfaceVariantLightHighContrast
import co.touchlab.dogify.theme.tertiaryContainerDarkHighContrast
import co.touchlab.dogify.theme.tertiaryContainerLightHighContrast
import co.touchlab.dogify.theme.tertiaryDarkHighContrast
import co.touchlab.dogify.theme.tertiaryLightHighContrast

private val highContrastLightColorScheme = lightColorScheme(
    primary = primaryLightHighContrast,
    onPrimary = onPrimaryLightHighContrast,
    primaryContainer = primaryContainerLightHighContrast,
    onPrimaryContainer = onPrimaryContainerLightHighContrast,
    secondary = secondaryLightHighContrast,
    onSecondary = onSecondaryLightHighContrast,
    secondaryContainer = secondaryContainerLightHighContrast,
    onSecondaryContainer = onSecondaryContainerLightHighContrast,
    tertiary = tertiaryLightHighContrast,
    onTertiary = onTertiaryLightHighContrast,
    tertiaryContainer = tertiaryContainerLightHighContrast,
    onTertiaryContainer = onTertiaryContainerLightHighContrast,
    error = errorLightHighContrast,
    onError = onErrorLightHighContrast,
    errorContainer = errorContainerLightHighContrast,
    onErrorContainer = onErrorContainerLightHighContrast,
    background = backgroundLightHighContrast,
    onBackground = onBackgroundLightHighContrast,
    surface = surfaceLightHighContrast,
    onSurface = onSurfaceLightHighContrast,
    surfaceVariant = surfaceVariantLightHighContrast,
    onSurfaceVariant = onSurfaceVariantLightHighContrast,
    outline = outlineLightHighContrast,
    outlineVariant = outlineVariantLightHighContrast,
    scrim = scrimLightHighContrast,
    inverseSurface = inverseSurfaceLightHighContrast,
    inverseOnSurface = inverseOnSurfaceLightHighContrast,
    inversePrimary = inversePrimaryLightHighContrast,
    surfaceDim = surfaceDimLightHighContrast,
    surfaceBright = surfaceBrightLightHighContrast,
    surfaceContainerLowest = surfaceContainerLowestLightHighContrast,
    surfaceContainerLow = surfaceContainerLowLightHighContrast,
    surfaceContainer = surfaceContainerLightHighContrast,
    surfaceContainerHigh = surfaceContainerHighLightHighContrast,
    surfaceContainerHighest = surfaceContainerHighestLightHighContrast,
)


private val highContrastDarkColorScheme = darkColorScheme(
    primary = primaryDarkHighContrast,
    onPrimary = onPrimaryDarkHighContrast,
    primaryContainer = primaryContainerDarkHighContrast,
    onPrimaryContainer = onPrimaryContainerDarkHighContrast,
    secondary = secondaryDarkHighContrast,
    onSecondary = onSecondaryDarkHighContrast,
    secondaryContainer = secondaryContainerDarkHighContrast,
    onSecondaryContainer = onSecondaryContainerDarkHighContrast,
    tertiary = tertiaryDarkHighContrast,
    onTertiary = onTertiaryDarkHighContrast,
    tertiaryContainer = tertiaryContainerDarkHighContrast,
    onTertiaryContainer = onTertiaryContainerDarkHighContrast,
    error = errorDarkHighContrast,
    onError = onErrorDarkHighContrast,
    errorContainer = errorContainerDarkHighContrast,
    onErrorContainer = onErrorContainerDarkHighContrast,
    background = backgroundDarkHighContrast,
    onBackground = onBackgroundDarkHighContrast,
    surface = surfaceDarkHighContrast,
    onSurface = onSurfaceDarkHighContrast,
    surfaceVariant = surfaceVariantDarkHighContrast,
    onSurfaceVariant = onSurfaceVariantDarkHighContrast,
    outline = outlineDarkHighContrast,
    outlineVariant = outlineVariantDarkHighContrast,
    scrim = scrimDarkHighContrast,
    inverseSurface = inverseSurfaceDarkHighContrast,
    inverseOnSurface = inverseOnSurfaceDarkHighContrast,
    inversePrimary = inversePrimaryDarkHighContrast,
    surfaceDim = surfaceDimDarkHighContrast,
    surfaceBright = surfaceBrightDarkHighContrast,
    surfaceContainerLowest = surfaceContainerLowestDarkHighContrast,
    surfaceContainerLow = surfaceContainerLowDarkHighContrast,
    surfaceContainer = surfaceContainerDarkHighContrast,
    surfaceContainerHigh = surfaceContainerHighDarkHighContrast,
    surfaceContainerHighest = surfaceContainerHighestDarkHighContrast,
)


@Composable
fun ComandaAiTheme(
    isDarkThemeOn: Boolean = isSystemInDarkTheme(),
    lightTheme: ColorScheme = highContrastLightColorScheme,
    darkTheme: ColorScheme = highContrastDarkColorScheme,
    content: @Composable() () -> Unit,
) {
    val colorScheme = when {
        isDarkThemeOn -> darkTheme
        else -> lightTheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
        typography = LocalMyTypography.current,
    )
}

