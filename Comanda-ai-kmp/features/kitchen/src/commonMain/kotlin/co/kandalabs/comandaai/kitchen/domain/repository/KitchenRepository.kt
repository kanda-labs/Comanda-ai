package co.kandalabs.comandaai.kitchen.domain.repository

import co.kandalabs.comandaai.kitchen.data.api.KitchenEvent
import co.kandalabs.comandaai.domain.ItemStatus
import co.kandalabs.comandaai.kitchen.domain.model.KitchenOrder
import kotlinx.coroutines.flow.Flow

interface KitchenRepository {
    suspend fun getActiveOrders(): Result<List<KitchenOrder>>
    
    suspend fun getDeliveredOrders(): Result<List<KitchenOrder>>
    
    suspend fun updateItemUnitStatus(
        orderId: Int,
        itemId: Int,
        unitIndex: Int,
        status: ItemStatus
    ): Result<Unit>
    
    suspend fun markOrderAsDelivered(orderId: Int): Result<Unit>
    
    suspend fun markItemAsDelivered(orderId: Int, itemId: Int): Result<Unit>
    
    fun getOrdersRealTime(): Flow<KitchenEvent>
}