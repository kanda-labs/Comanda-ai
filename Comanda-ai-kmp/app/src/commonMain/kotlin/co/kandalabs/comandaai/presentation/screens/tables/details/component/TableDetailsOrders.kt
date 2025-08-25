package co.kandalabs.comandaai.presentation.screens.tables.details.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.kandalabs.comandaai.theme.ComandaAiTheme
import co.kandalabs.comandaai.presentation.screens.tables.details.OrdersDetailsItemState
import co.kandalabs.comandaai.presentation.screens.tables.details.TableDetailsScreenBadge
import co.kandalabs.comandaai.tokens.ComandaAiColors
import co.kandalabs.comandaai.tokens.ComandaAiSpacing
import co.kandalabs.comandaai.domain.models.model.Order
import co.kandalabs.comandaai.domain.models.model.OrderStatus
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun TableDetailsOrders(
    orders: List<OrdersDetailsItemState>,
    onOrderClick: (Order) -> Unit,
    modifier: Modifier = Modifier
) {
    if (orders.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Nenhum pedido",
                    tint = ComandaAiColors.Gray300.value,
                    modifier = Modifier.size(40.dp)
                        .padding(bottom = ComandaAiSpacing.Medium.value)
                )
                Text(
                    text = "Não encontramos pedidos para essa mesa",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = ComandaAiSpacing.Small.value)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Top
        ) {
            items(orders) { order ->
                OrderListItem(
                    order = order,
                    isLastItem = order == orders.last(),
                    onClick = { onOrderClick(order.order) }
                )
            }
        }
    }
}

@Preview
@Composable
private fun TableDetailsOrdersComponentEmptyPreview() {
    ComandaAiTheme {
        TableDetailsOrders(
            orders = emptyList(),
            onOrderClick = {},
            modifier = Modifier
        )
    }
}

@Preview
@Composable
private fun TableDetailsOrdersComponentFilledPreview() {
    ComandaAiTheme {
        TableDetailsOrders(
            orders = listOf(
                OrdersDetailsItemState(
                    id = "Nº 1",
                    time = "Realizado às 12:00",
                    status = TableDetailsScreenBadge(
                        text = "Aberto",
                        color = ComandaAiColors.Green500,
                        textColor = ComandaAiColors.Surface,
                    ),
                    order = Order(
                        id = 1,
                        billId = 1,
                        tableNumber = 1,
                        userName = "usuario-teste",
                        items = emptyList(),
                        status = OrderStatus.OPEN,
                        createdAt = LocalDateTime(2025, 1, 28, 20, 0, 0),
                    ),
                ),
                OrdersDetailsItemState(
                    id = "Nº 2",
                    time = "Realizado às 13:00",
                    status = TableDetailsScreenBadge(
                        text = "Cancelado",
                        color = ComandaAiColors.Error,
                        textColor = ComandaAiColors.OnError,
                    ),
                    order = Order(
                        id = 2,
                        billId = 2,
                        tableNumber = 2,
                        userName = "usuario-teste2",
                        items = emptyList(),
                        status = OrderStatus.CANCELED,
                        createdAt = LocalDateTime(2025, 1, 28, 21, 0, 0),
                    ),
                )
            ),
            onOrderClick = {},
            modifier = Modifier
        )
    }
}
