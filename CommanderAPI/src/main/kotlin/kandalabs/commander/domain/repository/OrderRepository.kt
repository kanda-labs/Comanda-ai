package kandalabs.commander.domain.repository

import kandalabs.commander.domain.model.OrderResponse
import kandalabs.commander.presentation.models.request.CreateOrderRequest

interface OrderRepository {
    suspend fun getAllOrders(): List<OrderResponse>
    suspend fun getOrderById(id: Int): OrderResponse?
    suspend fun createOrder(createOrderRequest: CreateOrderRequest): OrderResponse
    suspend fun updateOrder(id: Int, orderResponse: OrderResponse): OrderResponse?
    suspend fun deleteOrder(id: Int): Boolean
    suspend fun getOrdersByBillId(billId: Int): List<OrderResponse>
}
