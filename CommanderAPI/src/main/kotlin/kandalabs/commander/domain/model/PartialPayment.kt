package kandalabs.commander.domain.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import java.security.MessageDigest

@Serializable
data class PartialPayment(
    val id: Int? = null,
    val billId: Int,
    val tableId: Int,
    val paidBy: String, // Nome da pessoa que pagou
    val amountInCentavos: Long,
    val amountFormatted: String,
    val description: String? = null, // Ex: "João pagou sua parte", "Pagamento parcial"
    val paymentMethod: String? = null,
    val createdAt: LocalDateTime
)
