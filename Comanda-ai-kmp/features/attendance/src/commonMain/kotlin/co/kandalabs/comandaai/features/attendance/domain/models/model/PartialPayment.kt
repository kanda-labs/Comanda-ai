package co.kandalabs.comandaai.features.attendance.domain.models.model

import co.kandalabs.comandaai.features.attendance.domain.models.enum.PaymentMethod
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
    val paymentMethod: PaymentMethod? = null,
    val receivedBy: String? = null,
    val createdAt: LocalDateTime
)