package co.kandalabs.comandaai.presentation.screens.payment

import co.kandalabs.comandaai.core.error.ComandaAiException
import co.kandalabs.comandaai.tokens.ComandaAiColors
import kandalabs.commander.domain.model.Order
import kandalabs.commander.domain.model.OrderStatus
import kandalabs.commander.domain.model.Item

internal data class PaymentSummaryScreenState(
    val isLoading: Boolean = true,
    val error: ComandaAiException? = null,
    val tableNumber: String = "",
    val orders: List<Order> = emptyList(),
    val totalAmount: Double = 0.0,
    val isProcessingPayment: Boolean = false
) {
    val appBarTitle = "Resumo do Pagamento"
    val contentTitle: String = "Mesa $tableNumber"
    
    val ordersPresentation: List<PaymentOrderItemState> = orders.mapIndexed { index, order ->
        val orderTotal = order.items.sumOf { item ->
            item.count * 1.0 // Convert to Double for consistent calculation
        } * 1.0 // Placeholder - we need to get the price from somewhere else
        
        PaymentOrderItemState(
            id = "Pedido Nº ${order.id ?: index + 1}",
            items = order.items.map { item ->
                PaymentItemState(
                    name = item.name,
                    quantity = item.count,
                    price = 1.0, // TODO: Need to get price from Item master data
                    total = item.count * 1.0, // TODO: Calculate with real price
                    observation = item.observation
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
    
    val formattedTotalAmount: String = "R$ ${totalAmount.toString()}"
}

internal data class PaymentOrderItemState(
    val id: String,
    val items: List<PaymentItemState>,
    val orderTotal: Double,
    val status: PaymentOrderBadge
) {
    val formattedOrderTotal: String = "R$ ${orderTotal.toString()}"
}

internal data class PaymentItemState(
    val name: String,
    val quantity: Int,
    val price: Double,
    val total: Double,
    val observation: String?
) {
    val formattedPrice: String = "R$ ${price.toString()}"
    val formattedTotal: String = "R$ ${total.toString()}"
    val quantityText: String = "${quantity}x"
}

internal data class PaymentOrderBadge(
    val text: String,
    val color: ComandaAiColors,
    val textColor: ComandaAiColors = ComandaAiColors.OnSurface
)