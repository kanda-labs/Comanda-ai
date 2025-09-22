package kandalabs.commander.domain.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import java.security.MessageDigest

enum class PaymentMethod {
    PIX, CARTAO, DINHEIRO
}

enum class PartialPaymentStatus {
    PAID, CANCELED
}

@Serializable
data class PartialPayment(
    val id: Int? = null,
    val billId: Int,
    val tableId: Int,
    val paidBy: String, // Nome da pessoa que pagou
    val amountInCentavos: Long,
    val amountFormatted: String,
    val description: String? = null, // Ex: "João pagou sua parte", "Pagamento parcial"
    val paymentMethod: PaymentMethod? = null,
    val receivedBy: String? = null, // Nome da pessoa que recebeu o pagamento
    val status: PartialPaymentStatus = PartialPaymentStatus.PAID,
    val createdAt: LocalDateTime
)
