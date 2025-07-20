package co.touchlab.dogify.tokens

import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.Dp

enum class ComandaAiSpacing(val rawValue: Int) {
    Tiny(2),
    xXSmall(4),
    xSmall(8),
    Small(12),
    Medium(16),
    Large(20),
    xXLarge(40);

    @Stable
    inline val value: Dp get() = Dp(value = rawValue.toFloat())

}