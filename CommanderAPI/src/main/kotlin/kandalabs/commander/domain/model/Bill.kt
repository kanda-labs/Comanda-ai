package kandalabs.commander.domain.model

import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalDateTime

@Serializable
data class Bill(
    val id: Int? = null,
    val tableId: Int?,
    val tableNumber: Int?,
    val orders: List<OrderResponse>,
    val status: BillStatus,
    val createdAt: LocalDateTime,
    val finalizedByUserId: Int? = null
)

enum class BillStatus { PAID, PARTIALLY_PAID, SCAM, OPEN, CANCELED }
