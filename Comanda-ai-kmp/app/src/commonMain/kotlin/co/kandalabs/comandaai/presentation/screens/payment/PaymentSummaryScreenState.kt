package co.kandalabs.comandaai.presentation.screens.payment

import co.kandalabs.comandaai.core.error.ComandaAiException
import co.kandalabs.comandaai.domain.models.model.PaymentSummaryResponse
import co.kandalabs.comandaai.tokens.ComandaAiColors

internal data class PaymentSummaryScreenState(
    val isLoading: Boolean = true,
    val error: ComandaAiException? = null,
    val tableNumber: String = "",
    val isProcessingPayment: Boolean = false,
    val paymentSummary: PaymentSummaryResponse? = null,
) {
    val appBarTitle = "Resumo do Pagamento"
    val contentTitle: String = "Mesa $tableNumber"
    
    // Propriedades derivadas diretamente do PaymentSummaryResponse
    val totalAmountPresentation: String = paymentSummary?.totalAmountFormatted ?: "R$ 0,00"
    val ordersPresentation: List<PaymentOrderItemState> = paymentSummary?.orders?.map { order ->
        PaymentOrderItemState(
            id = order.id,
            items = order.items.map { item ->
                PaymentItemState(
                    name = item.name,
                    quantity = item.quantity,
                    price = item.priceFormatted,
                    total = item.totalFormatted,
                    observation = item.observation
                )
            },
            orderTotal = order.orderTotalFormatted,
            status = PaymentOrderBadge(
                text = order.status.text,
                color = parseColor(order.status.colorHex)
            )
        )
    } ?: emptyList()
    
    private fun parseColor(colorHex: String): ComandaAiColors {
        return when (colorHex) {
            "#4CAF50" -> ComandaAiColors.Green500
            "#2196F3" -> ComandaAiColors.Blue500
            "#F44336" -> ComandaAiColors.Error
            else -> ComandaAiColors.OnSurface
        }
    }
}

internal data class PaymentOrderItemState(
    val id: String,
    val items: List<PaymentItemState>,
    val orderTotal: String, // Already formatted from backend
    val status: PaymentOrderBadge
) {
    val formattedOrderTotal: String = orderTotal
}

internal data class PaymentItemState(
    val name: String,
    val quantity: Int,
    val price: String, // Already formatted from backend
    val total: String, // Already formatted from backend
    val observation: String?
) {
    val formattedPrice: String = price
    val formattedTotal: String = total
    val quantityText: String = "${quantity}x"
}

internal data class PaymentOrderBadge(
    val text: String,
    val color: ComandaAiColors,
    val textColor: ComandaAiColors = ComandaAiColors.OnSurface
)