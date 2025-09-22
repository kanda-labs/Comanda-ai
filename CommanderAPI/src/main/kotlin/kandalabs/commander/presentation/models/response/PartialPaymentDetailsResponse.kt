package kandalabs.commander.presentation.models.response

import kotlinx.serialization.Serializable

@Serializable
data class PartialPaymentDetailsResponse(
    val id: Int,
    val tableId: Int,
    val paidBy: String,
    val amountInCentavos: Long,
    val amountFormatted: String,
    val description: String?,
    val paymentMethod: String?,
    val receivedBy: String?,
    val status: String,
    val createdAt: String
)