package kandalabs.commander.domain.service

import kandalabs.commander.domain.model.ItemStatus
import kandalabs.commander.domain.model.KitchenOrder

interface KitchenService {
    suspend fun getActiveOrdersForKitchen(): Result<List<KitchenOrder>>
    suspend fun updateItemUnitStatus(
        orderId: Int,
        itemId: Int,
        unitIndex: Int,
        status: ItemStatus,
        updatedBy: String
    ): Result<Boolean>
    suspend fun getItemStatusBreakdown(orderItemId: Int): Result<List<ItemStatus>>
    suspend fun markOrderAsDelivered(orderId: Int, updatedBy: String): Result<Boolean>
    suspend fun markItemAsDelivered(orderId: Int, itemId: Int, updatedBy: String): Result<Boolean>
}