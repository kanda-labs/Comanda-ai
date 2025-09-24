package co.kandalabs.comandaai.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import co.kandalabs.comandaai.tokens.ComandaAiColors

data class ComandaAiColorScheme(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val outlineVariant: Color,
    val gray50: Color,
    val gray100: Color,
    val gray200: Color,
    val gray300: Color,
    val gray400: Color,
    val gray500: Color,
    val gray600: Color,
    val gray700: Color,
    val gray800: Color,
    val gray900: Color,
    val green50: Color,
    val green100: Color,
    val green200: Color,
    val green300: Color,
    val green400: Color,
    val green500: Color,
    val green600: Color,
    val green700: Color,
    val green800: Color,
    val green900: Color,
    val yellow50: Color,
    val yellow100: Color,
    val yellow200: Color,
    val yellow300: Color,
    val yellow400: Color,
    val yellow500: Color,
    val yellow600: Color,
    val yellow700: Color,
    val yellow800: Color,
    val yellow900: Color,
    val blue50: Color,
    val blue100: Color,
    val blue200: Color,
    val blue300: Color,
    val blue400: Color,
    val blue500: Color,
    val blue600: Color,
    val blue700: Color,
    val blue800: Color,
    val blue900: Color,
    // Cores laranja
    val orange: Color,
    val onOrange: Color,
    val orangeContainer: Color,
    val onOrangeContainer: Color,
    // Cores amarelas
    val yellow: Color,
    val onYellow: Color,
    val yellowContainer: Color,
    val onYellowContainer: Color
)

val lightComandaAiColorScheme = ComandaAiColorScheme(
    primary = ComandaAiColors.Primary.value,
    onPrimary = ComandaAiColors.OnPrimary.value,
    primaryContainer = ComandaAiColors.PrimaryContainer.value,
    onPrimaryContainer = ComandaAiColors.OnPrimaryContainer.value,
    secondary = ComandaAiColors.Secondary.value,
    onSecondary = ComandaAiColors.OnSecondary.value,
    secondaryContainer = ComandaAiColors.SecondaryContainer.value,
    onSecondaryContainer = ComandaAiColors.OnSecondaryContainer.value,
    tertiary = ComandaAiColors.Tertiary.value,
    error = ComandaAiColors.Error.value,
    onError = ComandaAiColors.OnError.value,
    errorContainer = ComandaAiColors.ErrorContainer.value,
    onErrorContainer = ComandaAiColors.OnErrorContainer.value,
    background = ComandaAiColors.Background.value,
    onBackground = ComandaAiColors.OnBackground.value,
    surface = ComandaAiColors.Surface.value,
    onSurface = ComandaAiColors.OnSurface.value,
    surfaceVariant = ComandaAiColors.SurfaceVariant.value,
    onSurfaceVariant = ComandaAiColors.OnSurfaceVariant.value,
    outline = ComandaAiColors.Outline.value,
    outlineVariant = ComandaAiColors.OutlineVariant.value,
    gray50 = ComandaAiColors.SurfaceContainerLowest.value,  // Branco
    gray100 = ComandaAiColors.SurfaceContainerLow.value,    // F1F5EC
    gray200 = ComandaAiColors.SurfaceContainer.value,       // EBEFE6
    gray300 = ComandaAiColors.SurfaceContainerHigh.value,   // E6E9E0
    gray400 = ComandaAiColors.SurfaceVariant.value,         // DEE5D9
    gray500 = ComandaAiColors.OutlineVariant.value,         // C2C9BD
    gray600 = ComandaAiColors.Outline.value,                // 72796F
    gray700 = ComandaAiColors.OnSurfaceVariant.value,       // 424940
    gray800 = ComandaAiColors.InverseSurface.value,         // 2D322C
    gray900 = ComandaAiColors.OnBackground.value,           // 181D17
    green50 = ComandaAiColors.SurfaceContainerLowest.value,
    green100 = ComandaAiColors.SurfaceContainerLow.value,
    green200 = ComandaAiColors.SurfaceContainer.value,
    green300 = ComandaAiColors.SurfaceContainerHigh.value,
    green400 = ComandaAiColors.SurfaceContainerHighest.value,
    green500 = ComandaAiColors.Primary.value,
    green600 = ComandaAiColors.OnPrimaryContainer.value,
    green700 = ComandaAiColors.Secondary.value,
    green800 = ComandaAiColors.OnSecondaryContainer.value,
    green900 = ComandaAiColors.OnSurface.value,
    yellow50 = ComandaAiColors.YellowContainer.value,    // FBE186
    yellow100 = Color(0xFFF5DB81),                      // Amarelo mais claro
    yellow200 = ComandaAiColors.Yellow.value,           // 6F5D0D
    yellow300 = ComandaAiColors.OnYellowContainer.value, // 554500
    yellow400 = ComandaAiColors.OnYellow.value,         // Branco
    yellow500 = ComandaAiColors.Orange.value,           // 86521A (tom mais quente)
    yellow600 = ComandaAiColors.OnOrangeContainer.value, // 6A3B02
    yellow700 = ComandaAiColors.OrangeContainer.value,  // FFDCBF
    yellow800 = ComandaAiColors.OnOrange.value,         // Branco
    yellow900 = Color(0xFF3B2F00),                      // Amarelo muito escuro
    blue50 = ComandaAiColors.TertiaryContainer.value,
    blue100 = Color(0xFFA1CED5),
    blue200 = Color(0xFF39656B),
    blue300 = Color(0xFF1F4D53),
    blue400 = Color(0xFF00363C),
    blue500 = ComandaAiColors.Tertiary.value,
    blue600 = ComandaAiColors.OnTertiary.value,
    blue700 = ComandaAiColors.OnTertiaryContainer.value,
    blue800 = Color(0xFF003237),
    blue900 = Color(0xFF002B2F),
    // Cores laranja
    orange = ComandaAiColors.Orange.value,
    onOrange = ComandaAiColors.OnOrange.value,
    orangeContainer = ComandaAiColors.OrangeContainer.value,
    onOrangeContainer = ComandaAiColors.OnOrangeContainer.value,
    // Cores amarelas
    yellow = ComandaAiColors.Yellow.value,
    onYellow = ComandaAiColors.OnYellow.value,
    yellowContainer = ComandaAiColors.YellowContainer.value,
    onYellowContainer = ComandaAiColors.OnYellowContainer.value
)

val darkComandaAiColorScheme = ComandaAiColorScheme(
    primary = ComandaAiColors.PrimaryDark.value,
    onPrimary = ComandaAiColors.OnPrimaryDark.value,
    primaryContainer = ComandaAiColors.PrimaryContainerDark.value,
    onPrimaryContainer = ComandaAiColors.OnPrimaryContainerDark.value,
    secondary = ComandaAiColors.SecondaryDark.value,
    onSecondary = ComandaAiColors.OnSecondaryDark.value,
    secondaryContainer = ComandaAiColors.SecondaryContainerDark.value,
    onSecondaryContainer = ComandaAiColors.OnSecondaryContainerDark.value,
    tertiary = ComandaAiColors.TertiaryDark.value,
    error = ComandaAiColors.ErrorDark.value,
    onError = ComandaAiColors.OnErrorDark.value,
    errorContainer = ComandaAiColors.ErrorContainerDark.value,
    onErrorContainer = ComandaAiColors.OnErrorContainerDark.value,
    background = ComandaAiColors.BackgroundDark.value,
    onBackground = ComandaAiColors.OnBackgroundDark.value,
    surface = ComandaAiColors.SurfaceDark.value,
    onSurface = ComandaAiColors.OnSurfaceDark.value,
    surfaceVariant = ComandaAiColors.SurfaceVariantDark.value,
    onSurfaceVariant = ComandaAiColors.OnSurfaceVariantDark.value,
    outline = ComandaAiColors.OutlineDark.value,
    outlineVariant = ComandaAiColors.OutlineVariantDark.value,
    gray50 = ComandaAiColors.SurfaceContainerHighestDark.value,
    gray100 = ComandaAiColors.SurfaceContainerHighDark.value,
    gray200 = ComandaAiColors.SurfaceContainerDark.value,
    gray300 = ComandaAiColors.SurfaceContainerLowDark.value,
    gray400 = ComandaAiColors.SurfaceContainerLowestDark.value,
    gray500 = ComandaAiColors.OutlineVariantDark.value,
    gray600 = ComandaAiColors.OutlineDark.value,
    gray700 = ComandaAiColors.OnSurfaceVariantDark.value,
    gray800 = ComandaAiColors.OnBackgroundDark.value,
    gray900 = Color(0xFFFFFFFF),
    green50 = ComandaAiColors.OnPrimaryContainerDark.value,
    green100 = ComandaAiColors.PrimaryDark.value,
    green200 = ComandaAiColors.PrimaryContainerDark.value,
    green300 = ComandaAiColors.OnPrimaryDark.value,
    green400 = ComandaAiColors.SecondaryDark.value,
    green500 = ComandaAiColors.OnSecondaryDark.value,
    green600 = ComandaAiColors.SecondaryContainerDark.value,
    green700 = ComandaAiColors.OnSecondaryContainerDark.value,
    green800 = ComandaAiColors.OnSurfaceDark.value,
    green900 = ComandaAiColors.BackgroundDark.value,
    yellow50 = Color(0xFFFFF9C4),
    yellow100 = Color(0xFFFFF59D),
    yellow200 = Color(0xFFFFF176),
    yellow300 = Color(0xFFFFEE58),
    yellow400 = Color(0xFFFFEB3B),
    yellow500 = Color(0xFFFDD835),
    yellow600 = Color(0xFFFBC02D),
    yellow700 = Color(0xFFF9A825),
    yellow800 = Color(0xFFF57F17),
    yellow900 = Color(0xFFE65100),
    blue50 = ComandaAiColors.OnTertiaryContainerDark.value,
    blue100 = ComandaAiColors.TertiaryDark.value,
    blue200 = ComandaAiColors.TertiaryContainerDark.value,
    blue300 = ComandaAiColors.OnTertiaryDark.value,
    blue400 = Color(0xFF00363C),
    blue500 = Color(0xFF003237),
    blue600 = Color(0xFF002B2F),
    blue700 = Color(0xFF001F23),
    blue800 = Color(0xFF00141A),
    blue900 = Color(0xFF000A0D),
    // Cores laranja
    orange = ComandaAiColors.OrangeDark.value,
    onOrange = ComandaAiColors.OnOrangeDark.value,
    orangeContainer = ComandaAiColors.OrangeContainerDark.value,
    onOrangeContainer = ComandaAiColors.OnOrangeContainerDark.value,
    // Cores amarelas
    yellow = ComandaAiColors.YellowDark.value,
    onYellow = ComandaAiColors.OnYellowDark.value,
    yellowContainer = ComandaAiColors.YellowContainerDark.value,
    onYellowContainer = ComandaAiColors.OnYellowContainerDark.value
)

val LocalComandaAiColorScheme = staticCompositionLocalOf {
    lightComandaAiColorScheme
}

object ComandaAiTheme {
    val colorScheme: ComandaAiColorScheme
        @Composable get() = LocalComandaAiColorScheme.current

    val typography: androidx.compose.material3.Typography
        @Composable get() = LocalComandaAiTypography.current
}

@Composable
fun ComandaAiThemeProvider(
    isDark: Boolean = false,
    colorScheme: ComandaAiColorScheme? = null,
    content: @Composable () -> Unit
) {
    val colors = colorScheme ?: if (isDark) {
        darkComandaAiColorScheme
    } else {
        lightComandaAiColorScheme
    }

    CompositionLocalProvider(
        LocalComandaAiColorScheme provides colors,
        LocalComandaAiTypography provides ComandaAiTypography,
        content = content
    )
}