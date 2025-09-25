package co.kandalabs.comandaai.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import co.kandalabs.comandaai.theme.LocalComandaAiTypography
import co.kandalabs.comandaai.theme.ComandaAiThemeProvider
import co.kandalabs.comandaai.theme.backgroundDarkHighContrast
import co.kandalabs.comandaai.theme.backgroundLightHighContrast
import co.kandalabs.comandaai.theme.errorContainerDarkHighContrast
import co.kandalabs.comandaai.theme.errorContainerLightHighContrast
import co.kandalabs.comandaai.theme.errorDarkHighContrast
import co.kandalabs.comandaai.theme.errorLightHighContrast
import co.kandalabs.comandaai.theme.inverseOnSurfaceDarkHighContrast
import co.kandalabs.comandaai.theme.inverseOnSurfaceLightHighContrast
import co.kandalabs.comandaai.theme.inversePrimaryDarkHighContrast
import co.kandalabs.comandaai.theme.inversePrimaryLightHighContrast
import co.kandalabs.comandaai.theme.inverseSurfaceDarkHighContrast
import co.kandalabs.comandaai.theme.inverseSurfaceLightHighContrast
import co.kandalabs.comandaai.theme.onBackgroundDarkHighContrast
import co.kandalabs.comandaai.theme.onBackgroundLightHighContrast
import co.kandalabs.comandaai.theme.onErrorContainerDarkHighContrast
import co.kandalabs.comandaai.theme.onErrorContainerLightHighContrast
import co.kandalabs.comandaai.theme.onErrorDarkHighContrast
import co.kandalabs.comandaai.theme.onErrorLightHighContrast
import co.kandalabs.comandaai.theme.onPrimaryContainerDarkHighContrast
import co.kandalabs.comandaai.theme.onPrimaryContainerLightHighContrast
import co.kandalabs.comandaai.theme.onPrimaryDarkHighContrast
import co.kandalabs.comandaai.theme.onPrimaryLightHighContrast
import co.kandalabs.comandaai.theme.onSecondaryContainerDarkHighContrast
import co.kandalabs.comandaai.theme.onSecondaryContainerLightHighContrast
import co.kandalabs.comandaai.theme.onSecondaryDarkHighContrast
import co.kandalabs.comandaai.theme.onSecondaryLightHighContrast
import co.kandalabs.comandaai.theme.onSurfaceDarkHighContrast
import co.kandalabs.comandaai.theme.onSurfaceLightHighContrast
import co.kandalabs.comandaai.theme.onSurfaceVariantDarkHighContrast
import co.kandalabs.comandaai.theme.onSurfaceVariantLightHighContrast
import co.kandalabs.comandaai.theme.onTertiaryContainerDarkHighContrast
import co.kandalabs.comandaai.theme.onTertiaryContainerLightHighContrast
import co.kandalabs.comandaai.theme.onTertiaryDarkHighContrast
import co.kandalabs.comandaai.theme.onTertiaryLightHighContrast
import co.kandalabs.comandaai.theme.outlineDarkHighContrast
import co.kandalabs.comandaai.theme.outlineLightHighContrast
import co.kandalabs.comandaai.theme.outlineVariantDarkHighContrast
import co.kandalabs.comandaai.theme.outlineVariantLightHighContrast
import co.kandalabs.comandaai.theme.primaryContainerDarkHighContrast
import co.kandalabs.comandaai.theme.primaryContainerLightHighContrast
import co.kandalabs.comandaai.theme.primaryDarkHighContrast
import co.kandalabs.comandaai.theme.primaryLightHighContrast
import co.kandalabs.comandaai.theme.scrimDarkHighContrast
import co.kandalabs.comandaai.theme.scrimLightHighContrast
import co.kandalabs.comandaai.theme.secondaryContainerDarkHighContrast
import co.kandalabs.comandaai.theme.secondaryContainerLightHighContrast
import co.kandalabs.comandaai.theme.secondaryDarkHighContrast
import co.kandalabs.comandaai.theme.secondaryLightHighContrast
import co.kandalabs.comandaai.theme.surfaceBrightDarkHighContrast
import co.kandalabs.comandaai.theme.surfaceBrightLightHighContrast
import co.kandalabs.comandaai.theme.surfaceContainerDarkHighContrast
import co.kandalabs.comandaai.theme.surfaceContainerHighDarkHighContrast
import co.kandalabs.comandaai.theme.surfaceContainerHighLightHighContrast
import co.kandalabs.comandaai.theme.surfaceContainerHighestDarkHighContrast
import co.kandalabs.comandaai.theme.surfaceContainerHighestLightHighContrast
import co.kandalabs.comandaai.theme.surfaceContainerLightHighContrast
import co.kandalabs.comandaai.theme.surfaceContainerLowDarkHighContrast
import co.kandalabs.comandaai.theme.surfaceContainerLowLightHighContrast
import co.kandalabs.comandaai.theme.surfaceContainerLowestDarkHighContrast
import co.kandalabs.comandaai.theme.surfaceContainerLowestLightHighContrast
import co.kandalabs.comandaai.theme.surfaceDarkHighContrast
import co.kandalabs.comandaai.theme.surfaceDimDarkHighContrast
import co.kandalabs.comandaai.theme.surfaceDimLightHighContrast
import co.kandalabs.comandaai.theme.surfaceLightHighContrast
import co.kandalabs.comandaai.theme.surfaceVariantDarkHighContrast
import co.kandalabs.comandaai.theme.surfaceVariantLightHighContrast
import co.kandalabs.comandaai.theme.tertiaryContainerDarkHighContrast
import co.kandalabs.comandaai.theme.tertiaryContainerLightHighContrast
import co.kandalabs.comandaai.theme.tertiaryDarkHighContrast
import co.kandalabs.comandaai.theme.tertiaryLightHighContrast

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
    content: @Composable() () -> Unit,
) {
    ComandaAiThemeProvider(
        isDark = isDarkThemeOn,
        content = content
    )
}

