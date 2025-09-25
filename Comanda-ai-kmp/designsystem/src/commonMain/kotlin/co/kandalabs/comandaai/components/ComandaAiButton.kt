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
    PrimaryAboveSurfaceVariant,
    Secondary,
    Destructive,
    Warning
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

            ComandaAiButtonVariant.PrimaryAboveSurfaceVariant -> ButtonDefaults.buttonColors(
                containerColor = ComandaAiTheme.colorScheme.primary,
                contentColor = ComandaAiTheme.colorScheme.onPrimary,
                disabledContainerColor = ComandaAiTheme.colorScheme.onSurfaceVariant,
                disabledContentColor = ComandaAiTheme.colorScheme.gray50,
            )

            ComandaAiButtonVariant.Secondary -> ButtonDefaults.buttonColors(
                containerColor = ComandaAiTheme.colorScheme.secondaryContainer,
                contentColor = ComandaAiTheme.colorScheme.onSecondaryContainer,
                disabledContainerColor = ComandaAiTheme.colorScheme.surfaceVariant,
                disabledContentColor = ComandaAiTheme.colorScheme.onSurfaceVariant,
            )
            
            ComandaAiButtonVariant.Destructive -> ButtonDefaults.buttonColors(
                containerColor = ComandaAiTheme.colorScheme.error,
                contentColor = ComandaAiTheme.colorScheme.onError,
                disabledContainerColor = ComandaAiTheme.colorScheme.surfaceVariant,
                disabledContentColor = ComandaAiTheme.colorScheme.onSurfaceVariant,
            )

            ComandaAiButtonVariant.Warning -> ButtonDefaults.buttonColors(
                containerColor = ComandaAiTheme.colorScheme.yellow,
                contentColor = ComandaAiTheme.colorScheme.onYellow,
                disabledContainerColor = ComandaAiTheme.colorScheme.surfaceVariant,
                disabledContentColor = ComandaAiTheme.colorScheme.onSurfaceVariant,
            )
        }

    Button(
        modifier = modifier
            .fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(ComandaAiSpacing.Large.value),
        colors = buttonColors,
        enabled = isEnabled,
        content = {
            Text(
                text = text,
                modifier = Modifier.padding(ComandaAiSpacing.xXSmall.value)
            )
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
        colors = ButtonDefaults.textButtonColors(
            contentColor = ComandaAiTheme.colorScheme.primary,
            disabledContentColor = ComandaAiTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        ),
        content = {
            Text(
                text = text,
                modifier = Modifier.padding(ComandaAiSpacing.xXSmall.value)
            )
        }
    )
}

@Preview
@Composable
fun ComandaAiButtonPrimaryPreview() {
    ComandaAiTheme {
        ComandaAiButton(
            text = "Botão Primário",
            onClick = {},
            isEnabled = true,
            variant = ComandaAiButtonVariant.Primary
        )
    }
}

@Preview
@Composable
fun ComandaAiButtonSecondaryPreview() {
    ComandaAiTheme {
        ComandaAiButton(
            text = "Botão Secundário",
            onClick = {},
            isEnabled = true,
            variant = ComandaAiButtonVariant.Secondary
        )
    }
}

@Preview
@Composable
fun ComandaAiButtonDestructivePreview() {
    ComandaAiTheme {
        ComandaAiButton(
            text = "Botão Destrutivo",
            onClick = {},
            isEnabled = true,
            variant = ComandaAiButtonVariant.Destructive
        )
    }
}

@Preview
@Composable
fun ComandaAiButtonDisabledPreview() {
    ComandaAiTheme {
        ComandaAiButton(
            text = "Botão Desabilitado",
            onClick = {},
            isEnabled = false,
            variant = ComandaAiButtonVariant.Primary
        )
    }
}

@Preview
@Composable
fun ComandaAiButtonWarningPreview() {
    ComandaAiTheme {
        ComandaAiButton(
            text = "Botão Warning",
            onClick = {},
            isEnabled = true,
            variant = ComandaAiButtonVariant.Warning
        )
    }
}

@Preview
@Composable
fun ComandaAiTextButtonPreview() {
    ComandaAiTheme {
        ComandaAiTextButton(
            text = "Botão de Texto",
            onClick = {},
            isEnabled = true
        )
    }
}