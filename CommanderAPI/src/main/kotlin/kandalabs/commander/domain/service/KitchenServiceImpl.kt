package kandalabs.commander.domain.service

import kandalabs.commander.domain.model.ItemStatus
import kandalabs.commander.domain.model.KitchenOrder
import kandalabs.commander.domain.repository.OrderRepository

class KitchenServiceImpl(
    private val orderRepository: OrderRepository
) : KitchenService {
    
    override suspend fun getActiveOrdersForKitchen(): Result<List<KitchenOrder>> {
        return orderRepository.getOrdersWithIncompleteItems()
    }
    
    override suspend fun getDeliveredOrdersForKitchen(): Result<List<KitchenOrder>> {
        return orderRepository.getOrdersWithDeliveredItems()
    }
    
    override suspend fun updateItemUnitStatus(
        orderId: Int,
        itemId: Int,
        unitIndex: Int,
        status: ItemStatus,
        updatedBy: String
    ): Result<Boolean> {
        // Validar se o status é válido para kitchen operations
        if (!isValidKitchenStatus(status)) {
            return Result.failure(IllegalArgumentException("Invalid status for kitchen operations: $status"))
        }
        
        return orderRepository.updateItemUnitStatus(orderId, itemId, unitIndex, status, updatedBy)
    }
    
    override suspend fun getItemStatusBreakdown(orderItemId: Int): Result<List<ItemStatus>> {
        // Implementation will depend on how we identify order items
        return Result.success(emptyList())
    }
    
    override suspend fun markOrderAsDelivered(orderId: Int, updatedBy: String): Result<Boolean> {
        return orderRepository.markOrderAsDelivered(orderId, updatedBy)
    }
    
    override suspend fun markItemAsDelivered(orderId: Int, itemId: Int, updatedBy: String): Result<Boolean> {
        return orderRepository.markItemAsDelivered(orderId, itemId, updatedBy)
    }
    
    private fun isValidKitchenStatus(status: ItemStatus): Boolean {
        return when (status) {
            ItemStatus.PENDING,
            ItemStatus.DELIVERED,
            ItemStatus.CANCELED -> true
        }
    }
}