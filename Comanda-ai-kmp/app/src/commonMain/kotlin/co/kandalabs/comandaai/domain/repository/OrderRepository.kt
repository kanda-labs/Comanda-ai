package co.kandalabs.comandaai.domain.repository

import co.kandalabs.comandaai.core.coroutinesResult.ComandaAiResult
import kandalabs.commander.domain.model.Order
import kandalabs.commander.domain.model.OrderWithStatuses
import kandalabs.commander.domain.model.ItemStatus

interface OrderRepository {
    suspend fun createOrder(
        tableId: Int,
        billId: Int,
        items: List<CreateOrderItemRequest>
    ): ComandaAiResult<Order>
    
    suspend fun getAllOrders(): ComandaAiResult<List<Order>>
    
    suspend fun getOrderById(orderId: Int): ComandaAiResult<Order>
    
    suspend fun getOrderByIdWithStatuses(orderId: Int): ComandaAiResult<OrderWithStatuses>
    
    suspend fun updateOrder(orderId: Int, order: Order): ComandaAiResult<Order>
    
    suspend fun updateOrderWithIndividualStatuses(
        orderId: Int,
        order: Order,
        individualStatuses: Map<String, ItemStatus>,
        updatedBy: String
    ): ComandaAiResult<Order>
}

data class CreateOrderItemRequest(
    val itemId: Int,
    val name: String,
    val count: Int,
    val observation: String? = null
)