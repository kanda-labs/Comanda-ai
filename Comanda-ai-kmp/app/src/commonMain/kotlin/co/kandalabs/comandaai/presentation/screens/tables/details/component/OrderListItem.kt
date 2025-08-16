package co.kandalabs.comandaai.presentation.screens.tables.details.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import co.kandalabs.comandaai.presentation.designSystem.components.ComandaAiListItem
import co.kandalabs.comandaai.theme.ComandaAiTheme
import co.kandalabs.comandaai.presentation.screens.tables.details.OrdersDetailsItemState
import co.kandalabs.comandaai.tokens.ComandaAiColors
import co.kandalabs.comandaai.tokens.ComandaAiSpacing
import kandalabs.commander.domain.model.Order
import kandalabs.commander.domain.model.OrderStatus
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun OrderListItem(
    order: OrdersDetailsItemState,
    isLastItem: Boolean = false,
    onClick: () -> Unit,
) {
    ComandaAiListItem(
        modifier = Modifier
            .fillMaxWidth()
            .background(ComandaAiColors.Surface.value)
            .clickable(onClick = onClick),
        showDivider = isLastItem.not(),
        contentSlot = {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = order.id,
                        style = MaterialTheme.typography.titleMedium,
                        color = ComandaAiColors.OnSurface.value
                    )
                    Text(
                        text = order.time,
                        style = MaterialTheme.typography.bodySmall,
                        color = ComandaAiColors.OnSurface.value
                    )
                }

                order.status.let { status ->
                    CommandaBadge(
                        text = status.text,
                        containerColor = status.color.value,
                        contentColor = status.textColor.value
                    )
                }

                Spacer(modifier = Modifier.width(ComandaAiSpacing.Small.value))

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Ver detalhes",
                    modifier = Modifier.size(ComandaAiSpacing.Large.value),
                    tint = ComandaAiColors.Gray400.value
                )
            }
        }
    )
}

@Composable
private fun CommandaBadge(
    text: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color
) {
    androidx.compose.material3.Badge(
        containerColor = containerColor,
        contentColor = contentColor
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = ComandaAiSpacing.Small.value)
        )
    }
}

@Preview
@Composable
internal fun OrderListItemPreview() {
    ComandaAiTheme {
        OrderListItem(
            order = OrdersDetailsItemState(
                id = "Nº 1",
                time = "Realizado às 12:00",
                status = co.kandalabs.comandaai.presentation.screens.tables.details.TableDetailsScreenBadge(
                    text = "Aberto",
                    color = ComandaAiColors.Green500,
                    textColor = ComandaAiColors.Surface,
                ),
                order = Order(
                    id = 1,
                    billId = 1,
                    tableNumber = 1,
                    items = emptyList(),
                    status = OrderStatus.OPEN,
                    createdAt = LocalDateTime(2025, 1, 28, 20, 0, 0),
                ),
            ),
            onClick = {}
        )
    }
}
