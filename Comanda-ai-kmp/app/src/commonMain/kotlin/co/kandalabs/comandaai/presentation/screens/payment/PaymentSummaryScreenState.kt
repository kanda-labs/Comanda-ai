package co.kandalabs.comandaai.presentation.screens.payment

import co.kandalabs.comandaai.core.error.ComandaAiException
import co.kandalabs.comandaai.tokens.ComandaAiColors
import kandalabs.commander.domain.model.Order
import kandalabs.commander.domain.model.OrderStatus
import kandalabs.commander.domain.model.Item
import kandalabs.commander.domain.model.ItemStatus

internal data class PaymentSummaryScreenState(
    val isLoading: Boolean = true,
    val error: ComandaAiException? = null,
    val tableNumber: String = "",
    val orders: List<Order> = emptyList(),
    val isProcessingPayment: Boolean = false,
    internal val totalAmount: Long = 0L,
    val items: List<Item> = emptyList(),
) {
    val totalAmountPresentation: String = formatCurrency(totalAmount)
    val appBarTitle = "Resumo do Pagamento"
    val contentTitle: String = "Mesa $tableNumber"
    
    val ordersPresentation: List<PaymentOrderItemState> = orders
        .filter { order -> order.status != OrderStatus.CANCELED } // Excluir pedidos cancelados
        .mapIndexed { index, order ->
            val validItems = order.items.filter { it.status != ItemStatus.CANCELED } // Excluir itens cancelados
            
            val orderTotal = validItems.sumOf { orderItem ->
                val foundItem = items.firstOrNull { it.id == orderItem.itemId }
                val itemValueInReais = (foundItem?.value?.toDouble() ?: 0.0) / 100.0
                orderItem.count * itemValueInReais
            }
            
            PaymentOrderItemState(
                id = "Pedido Nº ${order.id ?: index + 1}",
                items = validItems.map { orderItem ->
                    val foundItem = items.firstOrNull { it.id == orderItem.itemId }
                    val itemPriceInReais = (foundItem?.value?.toDouble() ?: 0.0) / 100.0
                    val itemTotal = orderItem.count * itemPriceInReais
                    
                    PaymentItemState(
                        name = orderItem.name,
                        quantity = orderItem.count,
                        price = itemPriceInReais,
                        total = itemTotal,
                        observation = orderItem.observation
                    )
                },
                orderTotal = orderTotal,
                status = when (order.status) {
                    OrderStatus.GRANTED -> PaymentOrderBadge(
                        text = "Atendido",
                        color = ComandaAiColors.Green500,
                        textColor = ComandaAiColors.OnSurface
                    )
                    OrderStatus.OPEN -> PaymentOrderBadge(
                        text = "Pendente", 
                        color = ComandaAiColors.Blue500,
                        textColor = ComandaAiColors.OnSurface
                    )
                    OrderStatus.CANCELED -> PaymentOrderBadge(
                        text = "Cancelado",
                        color = ComandaAiColors.Error,
                        textColor = ComandaAiColors.OnError
                    )
                }
            )
        }
}

internal data class PaymentOrderItemState(
    val id: String,
    val items: List<PaymentItemState>,
    val orderTotal: Double,
    val status: PaymentOrderBadge
) {
    val formattedOrderTotal: String = "R$ ${"%.2f".format(orderTotal)}"
}

internal data class PaymentItemState(
    val name: String,
    val quantity: Int,
    val price: Double,
    val total: Double,
    val observation: String?
) {
    val formattedPrice: String = "R$ ${"%.2f".format(price)}"
    val formattedTotal: String = "R$ ${"%.2f".format(total)}"
    val quantityText: String = "${quantity}x"
}

internal data class PaymentOrderBadge(
    val text: String,
    val color: ComandaAiColors,
    val textColor: ComandaAiColors = ComandaAiColors.OnSurface
)

fun formatCurrency(amountInCents: Long): String {
    val reais = amountInCents / 100
    val cents = amountInCents % 100
    return "R$ $reais,${cents.toString().padStart(2, '0')}"
}