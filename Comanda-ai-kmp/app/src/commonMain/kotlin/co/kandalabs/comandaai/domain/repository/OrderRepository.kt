package co.kandalabs.comandaai.domain.repository

import co.kandalabs.comandaai.core.coroutinesResult.ComandaAiResult
import kandalabs.commander.domain.model.Order

interface OrderRepository {
    suspend fun createOrder(
        tableId: Int,
        billId: Int,
        items: List<CreateOrderItemRequest>
    ): ComandaAiResult<Order>
}

data class CreateOrderItemRequest(
    val itemId: Int,
    val name: String,
    val count: Int,
    val observation: String? = null
)