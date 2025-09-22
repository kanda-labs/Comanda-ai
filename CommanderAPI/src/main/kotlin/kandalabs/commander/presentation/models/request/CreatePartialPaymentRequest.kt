package kandalabs.commander.presentation.models.request

import kandalabs.commander.domain.model.PaymentMethod
import kotlinx.serialization.Serializable

@Serializable
data class CreatePartialPaymentRequest(
    val paidBy: String,
    val amountInCentavos: Long,
    val description: String? = null,
    val paymentMethod: PaymentMethod? = null,
    val receivedBy: String? = null
)