package co.kandalabs.comandaai.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import co.kandalabs.comandaai.theme.ComandaAiTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import co.kandalabs.comandaai.tokens.ComandaAiSpacing
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class ComandaAiButtonVariant {
    Primary,
    Secondary,
    Destructive
}


@Composable
fun ComandaAiButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    variant: ComandaAiButtonVariant = ComandaAiButtonVariant.Primary
) {
    val buttonColors: ButtonColors =
        when (variant) {
            ComandaAiButtonVariant.Primary -> ButtonDefaults.buttonColors(
                containerColor = ComandaAiTheme.colorScheme.primary,
                contentColor = ComandaAiTheme.colorScheme.onPrimary,
                disabledContainerColor = ComandaAiTheme.colorScheme.surfaceVariant,
                disabledContentColor = ComandaAiTheme.colorScheme.onSurfaceVariant,
            )

            ComandaAiButtonVariant.Secondary -> ButtonDefaults.buttonColors(
                containerColor = ComandaAiTheme.colorScheme.blue200,
                contentColor = ComandaAiTheme.colorScheme.onSecondary,
                disabledContainerColor = ComandaAiTheme.colorScheme.surfaceVariant,
                disabledContentColor = ComandaAiTheme.colorScheme.onSurfaceVariant,
            )
            
            ComandaAiButtonVariant.Destructive -> ButtonDefaults.buttonColors(
                containerColor = ComandaAiTheme.colorScheme.error,
                contentColor = ComandaAiTheme.colorScheme.onError,
                disabledContainerColor = ComandaAiTheme.colorScheme.surfaceVariant,
                disabledContentColor = ComandaAiTheme.colorScheme.onSurfaceVariant,
            )
        }

    val textColor = when (variant) {
        ComandaAiButtonVariant.Primary -> ComandaAiTheme.colorScheme.onPrimary
        ComandaAiButtonVariant.Secondary -> ComandaAiTheme.colorScheme.onSecondary
        ComandaAiButtonVariant.Destructive -> ComandaAiTheme.colorScheme.onError
    }

    Button(
        modifier = modifier
            .fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(ComandaAiSpacing.Large.value),
        colors = buttonColors,
        enabled = isEnabled,
        content = {
            Text(text, color = textColor, modifier = Modifier.padding(ComandaAiSpacing.xXSmall.value))
        }
    )
}

@Composable
fun ComandaAiTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true
) {
    TextButton(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        enabled = isEnabled,
        content = {
            Text(
                text = text,
                color = ComandaAiTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(ComandaAiSpacing.xXSmall.value)
            )
        }
    )
}

@Preview
@Composable
fun ComandaAiButtonPreview() {
    ComandaAiButton(text = "Teste", onClick = {}, isEnabled = true, variant = ComandaAiButtonVariant.Secondary)
}