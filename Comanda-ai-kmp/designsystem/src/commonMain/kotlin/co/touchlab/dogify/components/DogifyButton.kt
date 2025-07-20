package co.touchlab.dogify.components

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
import co.touchlab.dogify.tokens.ComandaAiSpacing
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class DogifyButtonVariant {
    Primary,
    Secondary
}


@Composable
fun DogifyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    variant: DogifyButtonVariant = DogifyButtonVariant.Primary
) {
    val buttonColors: ButtonColors =
        when (variant) {
            DogifyButtonVariant.Primary -> ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            DogifyButtonVariant.Secondary -> ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

    val textColor = when (variant) {
        DogifyButtonVariant.Primary -> MaterialTheme.colorScheme.onPrimary
        DogifyButtonVariant.Secondary -> MaterialTheme.colorScheme.onSecondary
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
fun DogifyButtonPreview() {
    DogifyButton(text = "Teste", onClick = {}, isEnabled = true, variant = DogifyButtonVariant.Secondary)
}