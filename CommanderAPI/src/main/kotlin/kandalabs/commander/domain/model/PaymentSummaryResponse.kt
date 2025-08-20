package kandalabs.commander.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PaymentSummaryResponse(
    val tableNumber: String,
    val totalAmountInCentavos: Long,
    val totalAmountFormatted: String,
    val orders: List<PaymentOrderSummary>
)

@Serializable
data class PaymentOrderSummary(
    val id: String,
    val items: List<PaymentItemSummary>,
    val orderTotalInCentavos: Long,
    val orderTotalFormatted: String,
    val status: PaymentOrderStatus
)

@Serializable
data class PaymentItemSummary(
    val name: String,
    val quantity: Int,
    val priceInCentavos: Int,
    val priceFormatted: String,
    val totalInCentavos: Long,
    val totalFormatted: String,
    val observation: String?
)

@Serializable
data class PaymentOrderStatus(
    val text: String,
    val colorHex: String
)