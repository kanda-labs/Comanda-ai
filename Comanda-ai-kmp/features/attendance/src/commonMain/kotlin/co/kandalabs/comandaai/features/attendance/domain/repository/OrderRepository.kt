package co.kandalabs.comandaai.features.attendance.domain.repository

import co.kandalabs.comandaai.features.attendance.domain.models.model.Order
import co.kandalabs.comandaai.features.attendance.domain.models.model.OrderWithStatuses
import co.kandalabs.comandaai.features.attendance.domain.models.request.CreateOrderItemRequest
import co.kandalabs.comandaai.core.coroutinesResult.ComandaAiResult
import co.kandalabs.comandaai.domain.ItemStatus

interface OrderRepository {
    suspend fun createOrder(
        tableId: Int,
        billId: Int,
        userName: String,
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

