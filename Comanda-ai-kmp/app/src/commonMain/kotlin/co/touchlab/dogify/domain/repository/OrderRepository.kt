package co.touchlab.dogify.domain.repository

import co.touchlab.dogify.core.coroutinesResult.DogifyResult
import kandalabs.commander.domain.model.Order

interface OrderRepository {
    suspend fun createOrder(
        tableId: Int,
        billId: Int,
        items: List<CreateOrderItemRequest>
    ): DogifyResult<Order>
}

data class CreateOrderItemRequest(
    val itemId: Int,
    val count: Int,
    val observation: String? = null
)