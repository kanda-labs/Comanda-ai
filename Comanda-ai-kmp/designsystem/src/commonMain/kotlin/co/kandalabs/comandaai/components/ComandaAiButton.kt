package co.kandalabs.comandaai.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import co.kandalabs.comandaai.tokens.ComandaAiSpacing
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class ComandaAiButtonVariant {
    Primary,
    Secondary
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
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            ComandaAiButtonVariant.Secondary -> ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

    val textColor = when (variant) {
        ComandaAiButtonVariant.Primary -> MaterialTheme.colorScheme.onPrimary
        ComandaAiButtonVariant.Secondary -> MaterialTheme.colorScheme.onSecondary
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

@Preview
@Composable
fun ComandaAiButtonPreview() {
    ComandaAiButton(text = "Teste", onClick = {}, isEnabled = true, variant = ComandaAiButtonVariant.Secondary)
}