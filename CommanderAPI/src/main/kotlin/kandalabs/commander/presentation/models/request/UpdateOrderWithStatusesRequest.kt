package kandalabs.commander.presentation.models.request

import kandalabs.commander.domain.model.ItemStatus
import kandalabs.commander.domain.model.OrderResponse
import kotlinx.serialization.Serializable

@Serializable
data class UpdateOrderWithStatusesRequest(
    val order: OrderResponse,
    val individualStatuses: Map<String, ItemStatus>,
    val updatedBy: String
)