package co.kandalabs.comandaai.domain.models.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class PartialPayment(
    val id: Int? = null,
    val billId: Int,
    val tableId: Int,
    val paidBy: String,
    val amountInCentavos: Long,
    val amountFormatted: String,
    val description: String? = null,
    val paymentMethod: String? = null,
    val createdAt: LocalDateTime
)