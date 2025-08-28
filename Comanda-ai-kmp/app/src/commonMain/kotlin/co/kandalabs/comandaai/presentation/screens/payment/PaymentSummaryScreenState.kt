package co.kandalabs.comandaai.presentation.screens.payment

import co.kandalabs.comandaai.core.error.ComandaAiException
import co.kandalabs.comandaai.domain.models.model.PaymentSummaryResponse
import co.kandalabs.comandaai.domain.models.model.PartialPayment
import co.kandalabs.comandaai.tokens.ComandaAiColors

internal data class PaymentSummaryScreenState(
    val isLoading: Boolean = true,
    val error: ComandaAiException? = null,
    val tableNumber: String = "",
    val isProcessingPayment: Boolean = false,
    val paymentSummary: PaymentSummaryResponse? = null,
    val showPartialPaymentDialog: Boolean = false,
    val showFinishPaymentConfirmation: Boolean = false,
) {
    val appBarTitle = "Resumo do Pagamento"
    val contentTitle: String = "Mesa $tableNumber"
    
    // Propriedades derivadas diretamente do PaymentSummaryResponse
    val totalAmountPresentation: String = paymentSummary?.totalAmountFormatted ?: "R$ 0,00"
    val totalPaidPresentation: String = paymentSummary?.totalPaidFormatted ?: "R$ 0,00"
    val remainingAmountPresentation: String = paymentSummary?.remainingAmountFormatted ?: "R$ 0,00"
    val hasPartialPayments: Boolean = paymentSummary?.partialPayments?.isNotEmpty() ?: false
    val isFullyPaid: Boolean = paymentSummary?.remainingAmountInCentavos == 0L
    
    // Compilado de todos os itens de todos os pedidos (itens cancelados já foram filtrados no backend)
    val compiledItems: List<PaymentItemState> = paymentSummary?.orders
        ?.flatMap { order -> order.items }
        ?.groupBy { it.name }
        ?.map { (itemName, items) ->
            val totalQuantity = items.sumOf { it.quantity }
            val totalPrice = items.sumOf { it.totalInCentavos }
            val firstItem = items.first()
            
            PaymentItemState(
                name = itemName,
                quantity = totalQuantity,
                price = firstItem.priceFormatted,
                total = formatCurrency(totalPrice),
                observation = items.mapNotNull { it.observation }.distinct().joinToString("; ").takeIf { it.isNotBlank() }
            )
        } ?: emptyList()

    // Lista de pagamentos parciais para mostrar histórico
    val partialPaymentsList: List<PartialPaymentState> = paymentSummary?.partialPayments
        ?.map { payment ->
            PartialPaymentState(
                id = payment.id!!,
                paidBy = payment.paidBy,
                amount = payment.amountFormatted,
                description = payment.description,
                paymentMethod = payment.paymentMethod,
                createdAt = payment.createdAt
            )
        } ?: emptyList()
    
    private fun formatCurrency(amountInCentavos: Long): String {
        val reais = amountInCentavos / 100
        val centavos = amountInCentavos % 100
        return "R$ $reais,${centavos.toString().padStart(2, '0')}"
    }
    
    private fun parseColor(colorHex: String): ComandaAiColors {
        return when (colorHex) {
            "#4CAF50" -> ComandaAiColors.Green500
            "#2196F3" -> ComandaAiColors.Blue500
            "#F44336" -> ComandaAiColors.Error
            else -> ComandaAiColors.OnSurface
        }
    }
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

internal data class PartialPaymentState(
    val id: Int,
    val paidBy: String,
    val amount: String,
    val description: String?,
    val paymentMethod: String?,
    val createdAt: kotlinx.datetime.LocalDateTime
) {
    val displayDescription: String = description ?: "Pagamento parcial de $paidBy"
    val timeAgo: String = formatTimeAgo(createdAt)
    
    private fun formatTimeAgo(dateTime: kotlinx.datetime.LocalDateTime): String {
        // Implementação simples - em uma implementação real, seria melhor usar uma biblioteca
        return "Hoje" // Placeholder - implementar formatação de tempo adequada
    }
}