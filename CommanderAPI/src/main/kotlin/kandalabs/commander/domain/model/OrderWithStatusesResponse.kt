package kandalabs.commander.domain.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class OrderWithStatusesResponse(
    val id: Int? = null,
    val billId: Int,
    val tableNumber: Int,
    val userName: String,
    val items: List<ItemOrder>,
    val status: OrderStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime? = null,
    val individualStatuses: Map<String, ItemStatus>
)