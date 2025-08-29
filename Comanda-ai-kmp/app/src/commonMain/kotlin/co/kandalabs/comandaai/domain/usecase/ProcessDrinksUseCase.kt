package co.kandalabs.comandaai.domain.usecase

import co.kandalabs.comandaai.core.coroutinesResult.ComandaAiResult
import co.kandalabs.comandaai.domain.repository.CreateOrderItemRequest
import co.kandalabs.comandaai.domain.models.model.Order

interface ProcessDrinksUseCase {
    suspend fun processDrinks(
        selectedItems: List<CreateOrderItemRequest>,
        tableId: Int,
        billId: Int,
        userName: String
    ): ComandaAiResult<ProcessDrinksResult>
}

data class ProcessDrinksResult(
    val kitchenItems: List<CreateOrderItemRequest>, // Items remaining for kitchen
    val drinkOrder: Order? // Created drink order (null if no drinks found)
)