package co.kandalabs.comandaai.presentation.designSystem.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import co.kandalabs.comandaai.theme.ComandaAiTheme
import co.kandalabs.comandaai.theme.LocalComandaAiTypography
import co.kandalabs.comandaai.tokens.ComandaAiColors
import co.kandalabs.comandaai.tokens.ComandaAiSpacing
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Um componente Badge genérico para exibir labels de status.
 * 
 * @param text O texto a ser exibido no badge
 * @param containerColor A cor de fundo do badge (padrão: ComandaAiColors.Primary)
 * @param contentColor A cor do texto no badge (padrão: ComandaAiColors.OnPrimary)
 * @param textStyle O estilo do texto no badge (padrão: ComandaAiTypography.bodyMedium)
 * @param modifier Modificador opcional para personalização adicional
 */
@Composable
fun CommandaBadge(
    text: String,
    containerColor: Color = ComandaAiColors.Primary.value,
    contentColor: Color = ComandaAiColors.OnPrimary.value,
    textStyle: TextStyle = LocalComandaAiTypography.current.bodyMedium,
    modifier: Modifier = Modifier
) {
    Badge(
        containerColor = containerColor,
        contentColor = contentColor,
        modifier = modifier
    ) {
        CompositionLocalProvider(
            LocalContentColor provides contentColor,
            LocalTextStyle provides textStyle
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = ComandaAiSpacing.Small.value)
            )
        }
    }
}

@Preview
@Composable
private fun CommandaBadgePreview() {
    ComandaAiTheme {
        CommandaBadge(
            text = "Aberto",
            containerColor = ComandaAiColors.Green500.value,
            contentColor = ComandaAiColors.Surface.value
        )
    }
}

@Preview
@Composable
private fun CommandaBadgeDefaultColorsPreview() {
    ComandaAiTheme {
        CommandaBadge(
            text = "Novo"
        )
    }
}

@Preview
@Composable
private fun CommandaBadgeWarningPreview() {
    ComandaAiTheme {
        CommandaBadge(
            text = "Pendente",
            containerColor = ComandaAiColors.Yellow600.value,
            contentColor = ComandaAiColors.Gray900.value
        )
    }
}

@Preview
@Composable
private fun CommandaBadgeErrorPreview() {
    ComandaAiTheme {
        CommandaBadge(
            text = "Erro",
            containerColor = ComandaAiColors.Error.value,
            contentColor = ComandaAiColors.OnError.value
        )
    }
}
