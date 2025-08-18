package co.kandalabs.comandaai.kitchen.data.repository

import co.kandalabs.comandaai.kitchen.data.api.KitchenApi
import co.kandalabs.comandaai.kitchen.data.api.KitchenSSEClient
import co.kandalabs.comandaai.kitchen.data.api.KitchenEvent
import co.kandalabs.comandaai.kitchen.domain.model.ItemStatus
import co.kandalabs.comandaai.kitchen.domain.model.KitchenOrder
import co.kandalabs.comandaai.kitchen.domain.repository.KitchenRepository
import kotlinx.coroutines.flow.Flow

class KitchenRepositoryImpl(
    private val kitchenApi: KitchenApi,
    private val kitchenSSEClient: KitchenSSEClient
) : KitchenRepository {
    
    override suspend fun getActiveOrders(): Result<List<KitchenOrder>> {
        return try {
            val orders = kitchenApi.getActiveOrders()
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateItemUnitStatus(
        orderId: Int,
        itemId: Int,
        unitIndex: Int,
        status: ItemStatus
    ): Result<Unit> {
        return try {
            kitchenApi.updateItemUnitStatus(orderId, itemId, unitIndex, status)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun markOrderAsDelivered(orderId: Int): Result<Unit> {
        return try {
            kitchenApi.markOrderAsDelivered(orderId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun markItemAsDelivered(orderId: Int, itemId: Int): Result<Unit> {
        return try {
            kitchenApi.markItemAsDelivered(orderId, itemId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getOrdersRealTime(): Flow<KitchenEvent> {
        return kitchenSSEClient.connectToKitchenEvents()
    }
}