package co.kandalabs.comandaai.domain.models.request

import kotlinx.serialization.Serializable

@Serializable
data class CreatePartialPaymentRequest(
    val paidBy: String,
    val amountInCentavos: Long,
    val description: String? = null,
    val paymentMethod: String? = null
)