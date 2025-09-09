package co.kandalabs.comandaai.features.attendance.domain.usecase

import co.kandalabs.comandaai.sdk.coroutinesResult.ComandaAiResult
import co.kandalabs.comandaai.features.attendance.domain.models.request.CreateOrderItemRequest
import co.kandalabs.comandaai.features.attendance.domain.models.model.Order

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