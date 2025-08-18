package kandalabs.commander.domain.repository

import kandalabs.commander.domain.model.ItemStatus
import kandalabs.commander.domain.model.ItemUnitStatus
import kandalabs.commander.domain.model.KitchenOrder
import kandalabs.commander.domain.model.OrderResponse
import kandalabs.commander.presentation.models.request.CreateOrderRequest

interface OrderRepository {
    suspend fun getAllOrders(): List<OrderResponse>
    suspend fun getOrderById(id: Int): OrderResponse?
    suspend fun createOrder(createOrderRequest: CreateOrderRequest): OrderResponse
    suspend fun updateOrder(id: Int, orderResponse: OrderResponse): OrderResponse?
    suspend fun deleteOrder(id: Int): Boolean
    suspend fun getOrdersByBillId(billId: Int): List<OrderResponse>
    
    // Kitchen-specific methods
    suspend fun getOrdersWithIncompleteItems(): Result<List<KitchenOrder>>
    suspend fun updateItemUnitStatus(
        orderId: Int,
        itemId: Int,
        unitIndex: Int,
        status: ItemStatus,
        updatedBy: String
    ): Result<Boolean>
    suspend fun getItemUnitStatuses(
        orderId: Int,
        itemId: Int
    ): Result<List<ItemUnitStatus>>
    suspend fun markOrderAsDelivered(orderId: Int, updatedBy: String): Result<Boolean>
    suspend fun markItemAsDelivered(orderId: Int, itemId: Int, updatedBy: String): Result<Boolean>
}
