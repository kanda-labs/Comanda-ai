package co.kandalabs.comandaai.features.attendance.domain.usecase

import co.kandalabs.comandaai.core.coroutinesResult.ComandaAiResult
import co.kandalabs.comandaai.features.attendance.domain.models.model.Order
import co.kandalabs.comandaai.features.attendance.domain.models.request.CreateOrderItemRequest

interface ProcessPromotionalItemsUseCase {
    suspend fun processPromotionalItems(selectedItems: List<CreateOrderItemRequest>): ProcessPromotionalItemsResult
    suspend fun markPromotionalItemsAsDelivered(order: Order, promotionalItemIds: List<Int>, updatedBy: String): ComandaAiResult<Order>
}

data class ProcessPromotionalItemsResult(
    val processedItems: List<CreateOrderItemRequest>,
    val promotionalItemIds: List<Int>
)