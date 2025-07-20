package co.touchlab.dogify.presentation.designSystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import co.touchlab.dogify.presentation.designSystem.theme.ComandaAiTheme
import co.touchlab.dogify.tokens.ComandaAiColors
import co.touchlab.dogify.tokens.ComandaAiSpacing
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Um componente de lista configurável com slots para conteúdo inicial, central e final.
 *
 * @param modifier Modificador para personalização adicional
 * @param backgroundColor Cor de fundo do item
 * @param onClick Callback para clique no item (null para desativar o clique)
 * @param showDivider Se deve mostrar o divisor abaixo do item
 * @param dividerColor A cor do divisor
 * @param startSlot Conteúdo para a parte esquerda do item (opcional)
 * @param contentSlot Conteúdo central do item (obrigatório)
 * @param trailingSlot Conteúdo para a parte direita do item (opcional)
 */
@Composable
fun ComandaAiListItem(
    modifier: Modifier = Modifier,
    backgroundColor: Color = ComandaAiColors.Surface.value,
    onClick: (() -> Unit)? = null,
    showDivider: Boolean = true,
    startSlot: @Composable (RowScope.() -> Unit)? = null,
    contentSlot: @Composable RowScope.() -> Unit,
    trailingSlot: @Composable (RowScope.() -> Unit)? = null
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else {
        Modifier
    }

    val dividerColor = remember { ComandaAiColors.Gray300.value }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .then(clickModifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ComandaAiSpacing.Medium.value),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (startSlot != null) {
                startSlot()
            }

            contentSlot()

            if (trailingSlot != null) {
                trailingSlot()
            }
        }

        if (showDivider) {
            HorizontalDivider(
                Modifier.fillMaxWidth()
                    .padding(horizontal = ComandaAiSpacing.Large.value),
                DividerDefaults.Thickness,
                color = dividerColor,
            )
        }
    }
}

@Preview
@Composable
private fun CommandaListItemPreview() {
    ComandaAiTheme {
        ComandaAiListItem(
            onClick = {},
            contentSlot = {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Título do Item",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Subtítulo com descrição",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            trailingSlot = {
                CommandaBadge(
                    text = "Novo",
                    containerColor = ComandaAiColors.Green500.value,
                    contentColor = ComandaAiColors.Surface.value
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Ver detalhes",
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(24.dp),
                    tint = ComandaAiColors.Gray400.value
                )
            }
        )
    }
}

@Preview
@Composable
private fun CommandaListItemWithStartIconPreview() {
    ComandaAiTheme {
        ComandaAiListItem(
            onClick = {},
            startSlot = {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(ComandaAiColors.Primary.value),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "1",
                        color = ComandaAiColors.Surface.value,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            contentSlot = {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = ComandaAiSpacing.Small.value)
                ) {
                    Text(
                        text = "Mesa 1",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "2 pedidos ativos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            trailingSlot = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Ver detalhes",
                    modifier = Modifier.size(24.dp),
                    tint = ComandaAiColors.Gray400.value
                )
            }
        )
    }
}
