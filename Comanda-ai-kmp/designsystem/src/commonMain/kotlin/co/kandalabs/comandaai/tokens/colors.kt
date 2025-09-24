package co.kandalabs.comandaai.tokens

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

/**
 * Tokens de cores para o design system ComandaAi
 * Paleta de cores baseada em tons verdes naturais com acentos laranja e amarelo
 */
enum class ComandaAiColors(override val rawColor: Long) : ComandaAiColor {
    // Cores primárias (Verde)
    Primary(0xFF3A693B),                 // Verde principal
    OnPrimary(0xFFFFFFFF),               // Branco sobre primário
    PrimaryContainer(0xFFBBF0B5),        // Container primário claro (corrigido)
    OnPrimaryContainer(0xFF225025),      // Texto sobre container primário

    // Cores secundárias (Verde acinzentado)
    Secondary(0xFF52634F),               // Verde acinzentado
    OnSecondary(0xFFFFFFFF),             // Branco sobre secundário
    SecondaryContainer(0xFFD5E8CF),      // Container secundário claro
    OnSecondaryContainer(0xFF3B4B39),    // Texto sobre container secundário

    // Cores terciárias (Verde azulado)
    Tertiary(0xFF39656B),               // Verde azulado
    OnTertiary(0xFFFFFFFF),             // Branco sobre terciário
    TertiaryContainer(0xFFBCEBF1),      // Container terciário claro
    OnTertiaryContainer(0xFF1F4D53),    // Texto sobre container terciário

    // Cores de erro
    Error(0xFFBA1A1A),                  // Vermelho de erro
    OnError(0xFFFFFFFF),                // Branco sobre erro
    ErrorContainer(0xFFFFDAD6),         // Container de erro claro
    OnErrorContainer(0xFF93000A),       // Texto sobre container de erro

    // Cores laranja
    Orange(0xFF86521A),                 // Laranja principal
    OnOrange(0xFFFFFFFF),               // Branco sobre laranja
    OrangeContainer(0xFFFFDCBF),        // Container laranja claro
    OnOrangeContainer(0xFF6A3B02),      // Texto sobre container laranja

    // Cores amarelas
    Yellow(0xFF6F5D0D),                 // Amarelo principal
    OnYellow(0xFFFFFFFF),               // Branco sobre amarelo
    YellowContainer(0xFFFBE186),        // Container amarelo claro
    OnYellowContainer(0xFF554500),      // Texto sobre container amarelo

    // Cores de fundo e superfície (Light)
    Background(0xFFF7FBF1),             // Fundo principal claro
    OnBackground(0xFF181D17),           // Texto sobre fundo
    Surface(0xFFF7FBF1),                // Superfície principal
    OnSurface(0xFF181D17),              // Texto sobre superfície
    SurfaceVariant(0xFFDEE5D9),         // Variante de superfície
    OnSurfaceVariant(0xFF424940),       // Texto sobre variante de superfície

    // Outline e contornos
    Outline(0xFF72796F),                // Contorno padrão
    OutlineVariant(0xFFC2C9BD),         // Variante de contorno
    Scrim(0xFF000000),                  // Scrim (overlay)

    // Cores inversas
    InverseSurface(0xFF2D322C),         // Superfície inversa
    InverseOnSurface(0xFFEEF2E9),       // Texto sobre superfície inversa
    InversePrimary(0xFF9FD49B),         // Primário inverso

    // Superfícies com diferentes elevações
    SurfaceDim(0xFFD7DBD2),             // Superfície escurecida
    SurfaceBright(0xFFF7FBF1),          // Superfície brilhante
    SurfaceContainerLowest(0xFFFFFFFF), // Container mais baixo
    SurfaceContainerLow(0xFFF1F5EC),    // Container baixo
    SurfaceContainer(0xFFEBEFE6),       // Container padrão
    SurfaceContainerHigh(0xFFE6E9E0),   // Container alto
    SurfaceContainerHighest(0xFFE0E4DB), // Container mais alto

    // Cores para modo escuro
    PrimaryDark(0xFFA0D49B),            // Verde principal escuro (corrigido)
    OnPrimaryDark(0xFF073910),          // Texto sobre primário escuro
    PrimaryContainerDark(0xFF225025),   // Container primário escuro
    OnPrimaryContainerDark(0xFFBBF0B5), // Texto sobre container primário escuro (corrigido)

    SecondaryDark(0xFFB9CCB4),          // Secundário escuro
    OnSecondaryDark(0xFF253423),        // Texto sobre secundário escuro
    SecondaryContainerDark(0xFF3B4B39), // Container secundário escuro
    OnSecondaryContainerDark(0xFFD5E8CF), // Texto sobre container secundário escuro

    TertiaryDark(0xFFA1CED5),          // Terciário escuro
    OnTertiaryDark(0xFF00363C),        // Texto sobre terciário escuro
    TertiaryContainerDark(0xFF1F4D53), // Container terciário escuro
    OnTertiaryContainerDark(0xFFBCEBF1), // Texto sobre container terciário escuro

    ErrorDark(0xFFFFB4AB),             // Erro escuro
    OnErrorDark(0xFF690005),           // Texto sobre erro escuro
    ErrorContainerDark(0xFF93000A),    // Container erro escuro
    OnErrorContainerDark(0xFFFFDAD6),  // Texto sobre container erro escuro

    // Cores laranja para modo escuro
    OrangeDark(0xFFFEB876),            // Laranja escuro
    OnOrangeDark(0xFF4B2800),          // Texto sobre laranja escuro
    OrangeContainerDark(0xFF6A3B02),   // Container laranja escuro
    OnOrangeContainerDark(0xFFFFDCBF), // Texto sobre container laranja escuro

    // Cores amarelas para modo escuro
    YellowDark(0xFFDEC56E),            // Amarelo escuro
    OnYellowDark(0xFF3B2F00),          // Texto sobre amarelo escuro
    YellowContainerDark(0xFF554500),   // Container amarelo escuro
    OnYellowContainerDark(0xFFFBE186), // Texto sobre container amarelo escuro

    BackgroundDark(0xFF10140F),        // Fundo escuro
    OnBackgroundDark(0xFFE0E4DB),      // Texto sobre fundo escuro
    SurfaceDark(0xFF10140F),           // Superfície escura
    OnSurfaceDark(0xFFE0E4DB),         // Texto sobre superfície escura
    SurfaceVariantDark(0xFF424940),    // Variante superfície escura
    OnSurfaceVariantDark(0xFFC2C9BD),  // Texto sobre variante superfície escura

    OutlineDark(0xFF8C9388),           // Contorno escuro
    OutlineVariantDark(0xFF424940),    // Variante contorno escuro

    InverseSurfaceDark(0xFFE0E4DB),    // Superfície inversa escura
    InverseOnSurfaceDark(0xFF2D322C),  // Texto sobre superfície inversa escura
    InversePrimaryDark(0xFF3A693B),    // Primário inverso escuro

    SurfaceDimDark(0xFF10140F),        // Superfície escurecida escura
    SurfaceBrightDark(0xFF363A34),     // Superfície brilhante escura
    SurfaceContainerLowestDark(0xFF0B0F0A), // Container mais baixo escuro
    SurfaceContainerLowDark(0xFF181D17),     // Container baixo escuro
    SurfaceContainerDark(0xFF1C211B),        // Container padrão escuro
    SurfaceContainerHighDark(0xFF272B25),    // Container alto escuro
    SurfaceContainerHighestDark(0xFF323630)  // Container mais alto escuro
}

interface ComandaAiColor {
    val rawColor: Long

    @Stable
    val value: Color get() = Color(rawColor)
}
