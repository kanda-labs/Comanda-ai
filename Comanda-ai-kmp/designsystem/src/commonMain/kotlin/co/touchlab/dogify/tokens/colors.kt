package co.touchlab.dogify.tokens

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

/**
 * Tokens de cores para o design system Dogify
 */
enum class ComandaAiColors(override val rawColor: Long) : ComandaAiColor {
    // Cores primárias e secundárias
    Primary(0xFF3F51B5),                 // Azul índigo
    PrimaryVariant(0xFF303F9F),          // Azul índigo escuro
    Secondary(0xFFFF9800),               // Laranja
    SecondaryVariant(0xFFF57C00),        // Laranja escuro

    // Cores de fundo e superfície
    Background(0xFFFFFFFF),              // Branco
    Surface(0xFFFFFFFF),                 // Branco
    Error(0xFFB00020),                   // Vermelho

    // Cores para uso sobre outras cores
    OnPrimary(0xFFFFFFFF),               // Branco
    OnSecondary(0xFF000000),             // Preto
    OnBackground(0xFF000000),            // Preto
    OnSurface(0xFF000000),               // Preto
    OnError(0xFFFFFFFF),                 // Branco

    // Cores neutras
    Gray50(0xFFFAFAFA),
    Gray100(0xFFF5F5F5),
    Gray200(0xFFEEEEEE),
    Gray300(0xFFE0E0E0),
    Gray400(0xFFBDBDBD),
    Gray500(0xFF9E9E9E),
    Gray600(0xFF757575),
    Gray700(0xFF616161),
    Gray800(0xFF424242),
    Gray900(0xFF212121),

    // Cores verdes
    Green50(0xFFE8F5E9),
    Green100(0xFFC8E6C9),
    Green200(0xFFA5D6A7),
    Green300(0xFF81C784),
    Green400(0xFF66BB6A),
    Green500(0xFF4CAF50),
    Green600(0xFF43A047),
    Green700(0xFF388E3C),
    Green800(0xFF2E7D32),
    Green900(0xFF1B5E20),

    // Cores amarelas
    Yellow50(0xFFFFFDE7),
    Yellow100(0xFFFFF9C4),
    Yellow200(0xFFFFF59D),
    Yellow300(0xFFFFF176),
    Yellow400(0xFFFFEE58),
    Yellow500(0xFFFFEB3B),
    Yellow600(0xFFFDD835),
    Yellow700(0xFFFBC02D),
    Yellow800(0xFFF9A825),
    Yellow900(0xFFF57F17),

    // Cores azuis
    Blue50(0xFFE3F2FD),
    Blue100(0xFFBBDEFB),
    Blue200(0xFF90CAF9),
    Blue300(0xFF64B5F6),
    Blue400(0xFF42A5F5),
    Blue500(0xFF2196F3),
    Blue600(0xFF1E88E5),
    Blue700(0xFF1976D2),
    Blue800(0xFF1565C0),
    Blue900(0xFF0D47A1)
}

interface ComandaAiColor {
    val rawColor: Long

    @Stable
    val value: Color get() = Color(rawColor)
}
