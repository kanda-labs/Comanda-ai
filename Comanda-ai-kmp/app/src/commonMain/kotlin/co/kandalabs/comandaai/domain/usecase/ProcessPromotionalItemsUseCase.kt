package co.kandalabs.comandaai.domain.usecase

import co.kandalabs.comandaai.core.coroutinesResult.ComandaAiResult
import co.kandalabs.comandaai.domain.repository.CreateOrderItemRequest
import co.kandalabs.comandaai.domain.models.model.Order

interface ProcessPromotionalItemsUseCase {
    suspend fun processPromotionalItems(selectedItems: List<CreateOrderItemRequest>): ProcessPromotionalItemsResult
    suspend fun markPromotionalItemsAsDelivered(order: Order, promotionalItemIds: List<Int>, updatedBy: String): ComandaAiResult<Order>
}

data class ProcessPromotionalItemsResult(
    val processedItems: List<CreateOrderItemRequest>,
    val promotionalItemIds: List<Int>
)