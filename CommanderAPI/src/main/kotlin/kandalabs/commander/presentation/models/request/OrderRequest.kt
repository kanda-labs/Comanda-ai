package kandalabs.commander.presentation.models.request

import kandalabs.commander.domain.model.ItemOrder
import kandalabs.commander.domain.model.ItemStatus
import kotlinx.serialization.Serializable

@Serializable
data class CreateOrderRequest(
    val billId: Int,
    val tableId: Int,
    val items: List<CreateOrderItemRequest>
)

@Serializable
data class CreateOrderItemRequest(
    val itemId: Int,
    val name: String,
    val count: Int,
    val observation: String? = null,
) {

    fun toOrderItemResponse(orderId: Int) = ItemOrder(
        itemId = itemId,
        orderId = orderId,
        name = name,
        count = count,
        observation = observation,
        status = ItemStatus.OPEN,
    )
}


@Serializable
data class UpdateOrderRequest(
    val status: String
)
