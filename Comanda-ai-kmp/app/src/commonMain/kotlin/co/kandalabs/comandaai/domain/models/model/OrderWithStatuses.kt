package kandalabs.commander.domain.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class OrderWithStatuses(
    val id: Int? = null,
    val billId: Int,
    val tableNumber: Int,
    val items: List<ItemOrder>,
    val status: OrderStatus,
    val createdAt: LocalDateTime,
    val individualStatuses: Map<String, ItemStatus>
)