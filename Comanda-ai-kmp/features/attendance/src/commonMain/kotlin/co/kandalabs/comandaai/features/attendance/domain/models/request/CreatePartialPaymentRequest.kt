package co.kandalabs.comandaai.features.attendance.domain.models.request

import co.kandalabs.comandaai.features.attendance.domain.models.enum.PaymentMethod
import kotlinx.serialization.Serializable

@Serializable
data class CreatePartialPaymentRequest(
    val paidBy: String,
    val amountInCentavos: Long,
    val description: String? = null,
    val paymentMethod: PaymentMethod? = null,
    val receivedBy: String? = null
)