package co.kandalabs.comandaai.domain.usecase

import co.kandalabs.comandaai.core.coroutinesResult.ComandaAiResult
import co.kandalabs.comandaai.core.error.ComandaAiException
import co.kandalabs.comandaai.domain.repository.CreateOrderItemRequest
import co.kandalabs.comandaai.domain.repository.ItemsRepository
import co.kandalabs.comandaai.domain.repository.OrderRepository
import co.kandalabs.comandaai.domain.models.model.Order
import co.kandalabs.comandaai.domain.ItemCategory
import co.kandalabs.comandaai.domain.ItemStatus

class ProcessDrinksUseCaseImpl(
    private val itemsRepository: ItemsRepository,
    private val orderRepository: OrderRepository
) : ProcessDrinksUseCase {

    private val choppPromotionalId = 18 // ID do item promocional Chopp

    override suspend fun processDrinks(
        selectedItems: List<CreateOrderItemRequest>,
        tableId: Int,
        billId: Int,
        userName: String
    ): ComandaAiResult<ProcessDrinksResult> {
        try {
            // Buscar todos os itens para verificar categorias
            val allItemsResult = itemsRepository.getItems(itemStatus = null)
            if (allItemsResult !is ComandaAiResult.Success) {
                return ComandaAiResult.Failure(ComandaAiException.UnknownException("Erro ao buscar itens"))
            }

            val allItems = allItemsResult.data
            val drinkItems = mutableListOf<CreateOrderItemRequest>()
            val kitchenItems = mutableListOf<CreateOrderItemRequest>()

            // Separar itens de bebida dos itens da cozinha
            selectedItems.forEach { selectedItem ->
                val item = allItems.find { it.id == selectedItem.itemId }
                val isDrink = item?.category == ItemCategory.DRINK || selectedItem.itemId == choppPromotionalId
                
                if (isDrink) {
                    drinkItems.add(selectedItem)
                } else {
                    kitchenItems.add(selectedItem)
                }
            }

            // Se não há bebidas, retornar apenas os itens da cozinha
            if (drinkItems.isEmpty()) {
                return ComandaAiResult.Success(
                    ProcessDrinksResult(
                        kitchenItems = selectedItems,
                        drinkOrder = null
                    )
                )
            }

            // Criar pedido separado para bebidas
            val drinkOrderResult = orderRepository.createOrder(
                tableId = tableId,
                billId = billId,
                userName = userName,
                items = drinkItems
            )

            when (drinkOrderResult) {
                is ComandaAiResult.Success -> {
                    val createdDrinkOrder = drinkOrderResult.data
                    
                    // Marcar todos os itens do pedido de bebidas como DELIVERED
                    val drinkIndividualStatuses = mutableMapOf<String, ItemStatus>()
                    
                    createdDrinkOrder.items?.forEach { item ->
                        // Marcar cada unidade do item de bebida como DELIVERED
                        (0 until item.count).forEach { unitIndex ->
                            drinkIndividualStatuses["${item.itemId}_${unitIndex}"] = ItemStatus.DELIVERED
                        }
                    }
                    
                    if (drinkIndividualStatuses.isNotEmpty()) {
                        orderRepository.updateOrderWithIndividualStatuses(
                            orderId = createdDrinkOrder.id!!,
                            order = createdDrinkOrder,
                            individualStatuses = drinkIndividualStatuses,
                            updatedBy = userName
                        )
                    }
                    
                    return ComandaAiResult.Success(
                        ProcessDrinksResult(
                            kitchenItems = kitchenItems,
                            drinkOrder = createdDrinkOrder
                        )
                    )
                }
                is ComandaAiResult.Failure -> {
                    return ComandaAiResult.Failure(
                        ComandaAiException.UnknownException("Erro ao criar pedido de bebidas: ${drinkOrderResult.exception.message}")
                    )
                }
            }
        } catch (e: Exception) {
            return ComandaAiResult.Failure(ComandaAiException.UnknownException("Erro inesperado ao processar bebidas: ${e.message}"))
        }
    }
}